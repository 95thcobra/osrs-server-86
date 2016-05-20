package guthix.services.serializers.pg;

import com.google.gson.*;
import com.typesafe.config.Config;

import guthix.GameServer;
import guthix.model.entity.Player;
import guthix.model.entity.player.Privilege;
import guthix.model.item.Item;
import guthix.services.login.LoginWorker;
import guthix.services.serializers.PlayerLoadResult;
import guthix.services.serializers.PlayerSerializer;
import guthix.services.serializers.pg.part.*;
import guthix.services.sql.PgSqlService;
import guthix.services.sql.PgSqlWorker;
import guthix.services.sql.SharableStatement;
import guthix.services.sql.SqlTransaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Bart on 8/10/2015.
 */
public class PgSqlPlayerSerializer extends PlayerSerializer {

	private static final Logger logger = LogManager.getLogger(PgSqlPlayerSerializer.class);
	private static SharableStatement credentialStatement = new SharableStatement("SELECT * FROM accounts WHERE username=?;");
	private static SharableStatement characterStatement = new SharableStatement("SELECT * FROM characters WHERE account_id=? AND service_id=1;");
	private static SharableStatement characterUpdateStatement = new SharableStatement("UPDATE characters SET x=?,z=?,level=?," +
			"inventory=?::JSONB,equipment=?::JSONB,skills=?::JSONB,bank=?::JSONB," +
			"pkp=?,varps=?::JSONB WHERE account_id=(SELECT id FROM accounts WHERE username=?) AND service_id=1;");
	private static SharableStatement accountCreationStatement = new SharableStatement("INSERT INTO accounts (username, password, email, displayname) " +
			"VALUES (?,?,?,?) RETURNING id;");
	private static SharableStatement characterCreationStatement = new SharableStatement("INSERT INTO characters (account_id, service_id) VALUES (?,1) RETURNING id;");
	private static SharableStatement checkOnline = new SharableStatement("SELECT * FROM online_characters WHERE account_id=? AND service_id=? AND world_id=?;");
	private static SharableStatement removeOnline = new SharableStatement("DELETE FROM online_characters WHERE account_id=? AND service_id=? AND world_id=?;");
	private static SharableStatement addOnline = new SharableStatement("INSERT INTO online_characters(account_id,service_id,character_id,world_id) VALUES (?,?,?,?);");

	private Gson gson;
	private GameServer server;
	private PgSqlService sqlService;
	private JsonParser parser;
	private Connection connection;
	private PgSqlWorker worker;

	private List<PgJsonPart> parts = new LinkedList<PgJsonPart>(){
		{
			add(new TilePart());
			add(new InventoryPart());
			add(new EquipmentPart());
			add(new SkillsPart());
			add(new BankPart());
			add(new PkpPart());
			add(new VarpsPart());
		}
	};


	public PgSqlPlayerSerializer() {
		super(null);

		gson = new GsonBuilder().create(); //TODO configuration
		parser = new JsonParser();
	}

	@Override
	public void setup(GameServer server, Config serviceConfig) {
		super.setup(server, serviceConfig);
		this.server = server;
	}

	@Override
	public boolean start() {
		sqlService = server.service(PgSqlService.class, false).get();
		worker = new PgSqlWorker(sqlService);
		connection = sqlService.connection();

		// Let the worker run in its own thread
		new Thread(worker).start();

		return true;
	}

	private void renewConnection() {
		connection = sqlService.connection();
	}

	@Override
	public boolean loadPlayer(Player player, Object uid, String password, Consumer<PlayerLoadResult> fn) {
		// Check username prior to processing :)
		if (!validName(player.username())) {
			fn.accept(PlayerLoadResult.INVALID_DETAILS);
			return true;
		}

		// Submit work to the almighty transaction worker!
		worker.submit(new SqlTransaction() {
			public void execute(Connection connection) throws Exception {
				// Is the Netty connection still open? Useless processing is not welcome.
				if (!player.channel().isActive() || !player.channel().isOpen()) {
					return;
				}

				PreparedStatement accountStatement = credentialStatement.using(connection);
				accountStatement.setString(1, player.username().toLowerCase());
				ResultSet accountInfo = accountStatement.executeQuery();

				int characterId = 0;

				// Did we have a match?
				if (accountInfo.next()) {
					// Verify the password. // TODO: 9/18/2015 bcrypt password hashing
					String storedpass = accountInfo.getString("password");
					if (!storedpass.equals(password)) {
						fn.accept(PlayerLoadResult.INVALID_DETAILS);
						connection.rollback();
						return;
					}

					// Basic information
					player.displayName(accountInfo.getString("displayname"));
					player.privilege(Privilege.values()[accountInfo.getInt("rights")]);
					player.id(accountInfo.getInt("id"));

					// Let's proceed; grab the jsonb data =)
					PreparedStatement charStmt = characterStatement.using(connection);
					charStmt.setInt(1, accountInfo.getInt("id"));
					ResultSet characterInfo = charStmt.executeQuery();

					// Were we unable to find the account? // TODO: 9/18/2015 this needs to work with multiple services
					if (!characterInfo.next()) {
						fn.accept(PlayerLoadResult.INVALID_DETAILS);
						connection.rollback();
						return;
					}

					characterId = characterInfo.getInt("id");

					// Serialize all the parts of the account
					for (PgJsonPart part : parts) {
						part.decode(player, characterInfo);
					}
				} else {
					if (illegalName(player.username())) {
						fn.accept(PlayerLoadResult.INVALID_DETAILS);
						connection.rollback();
						return;
					}

					PreparedStatement accStmt = accountCreationStatement.using(connection);
					accStmt.setString(1, player.username().toLowerCase());
					accStmt.setString(2, password);
					accStmt.setString(3, "no@email.com");
					accStmt.setString(4, player.username());
					ResultSet set = accStmt.executeQuery(); // Execute the insert
					set.next();

					int id = set.getInt("id");
					player.id(id);

					// Finally execute the insert :)
					PreparedStatement charStmt = characterCreationStatement.using(connection);
					charStmt.setInt(1, id);
					ResultSet insertedCharacter = charStmt.executeQuery();
					insertedCharacter.next();
					characterId = insertedCharacter.getInt("id");
				}

				// Finally, do one more 'check' and remove the player from the players online.
				try {
					PreparedStatement stmt = addOnline.using(connection);
					stmt.setInt(1, (int) player.id()); // account_id
					stmt.setInt(2, 1); // service_id
					stmt.setInt(3, characterId); // world_id
					stmt.setInt(4, server.world().id()); // world_id

					// Make sure that was valid and stuff. 0 results altered means we failed to remove it.
					if (stmt.executeUpdate() == 0) {
						connection.rollback();
						fn.accept(PlayerLoadResult.ALREADY_ONLINE);
						return;
					}
				} catch (Exception e) { // This kind of comes expected. It's thrown if it violates the uniqueness.
					connection.rollback();
					fn.accept(PlayerLoadResult.ALREADY_ONLINE);
					return;
				}

				connection.commit();

				// If everything worked, we may go on.
				fn.accept(PlayerLoadResult.OK);
			}
		});

		return true;
	}

	private static boolean illegalName(String n) {
		n = n.toLowerCase();
		if (n.contains("mod ") || n.contains("admin") || n.contains("owner") || n.contains("coder") || n.contains("developer") || n.contains("modera")) {
			return true;
		}

		return false;
	}

	@Override
	public void savePlayer(Player player) {
		worker.submit(new SqlTransaction() {
			public void execute(Connection connection) throws Exception {
				try {
					// See if we're online
					PreparedStatement stmt = checkOnline.using(connection);
					stmt.setInt(1, (int) player.id()); // account_id
					stmt.setInt(2, 1); // service_id
					stmt.setInt(3, server.world().id()); // world_id

					// Did it return a result? If it did, we're online. If not, we're in trouble.
					if (!stmt.executeQuery().next()) {
						return;
					}

					// Proceed with adding our new data
					stmt = characterUpdateStatement.using(connection);

					for (PgJsonPart part : parts) {
						part.encode(player, stmt);
					}

					stmt.setString(10, player.username().toLowerCase());
					stmt.executeUpdate();

					// Finally, do one more 'check' and remove the player from the players online.
					stmt = removeOnline.using(connection);
					stmt.setInt(1, (int) player.id()); // account_id
					stmt.setInt(2, 1); // service_id
					stmt.setInt(3, server.world().id()); // world_id

					// Make sure that was valid and stuff. 0 results altered means we failed to remove it.
					if (stmt.executeUpdate() == 0) {
						connection.rollback();
					}

					// Nice, that went perfectly.
					connection.commit();
				} catch (Exception e) {
					connection.rollback();
					logger.error("Could not save player info!", e);
					throw e;
				}
			}
		});
	}

	private static final char[] VALID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789- ".toCharArray();
	static {
		Arrays.sort(VALID_CHARS);
	}
	private static boolean validName(String s) {
		if (s.length() < 3 || s.length() > 12)
			return false;

		for (char c : s.toLowerCase().toCharArray())
			if (Arrays.binarySearch(VALID_CHARS, c) < 0)
				return false;

		return true;
	}

}
