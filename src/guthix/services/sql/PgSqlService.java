package guthix.services.sql;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.typesafe.config.Config;

import guthix.GameServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by Bart on 8/1/2015.
 *
 * Sql service for PostgreSQL. Veteres is built towards PgSql but may or may not provide housing for other database
 * services soon.
 */
public class PgSqlService implements SqlService {

	private static final Logger logger = LogManager.getLogger(PgSqlService.class);

	/**
	 * The connection pool we acquire connections from.
	 */
	private ComboPooledDataSource connectionPool;

	private GameServer server;

	@Override
	public void setup(GameServer server, Config serviceConfig) {
		this.server = server;

		// Read configuration from the conf file
		String user = serviceConfig.getString("user");
		String pass = serviceConfig.getString("pass");
		String host = serviceConfig.getString("host");
		int port = serviceConfig.getInt("port");
		String database = serviceConfig.getString("database");

		// Configure the connection pool
		connectionPool = new ComboPooledDataSource();
		connectionPool.setMinPoolSize(4);
		connectionPool.setInitialPoolSize(4);
		connectionPool.setMaxPoolSize(40);
		connectionPool.setAcquireIncrement(4);
		connectionPool.setUser(user);
		connectionPool.setPassword(pass);
		connectionPool.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", host, port, database));
	}

	@Override
	public Connection connection() {
		try {
			return connectionPool.getConnection();
		} catch (SQLException e) {
			logger.error("Error acquiring connection from psql pool", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean start() {
		try {
			// Create a simple query to check for the version we use.
			try (Connection connection = connectionPool.getConnection()) {
				PreparedStatement version = connection.prepareStatement("SHOW SERVER_VERSION;");
				version.execute();
				version.getResultSet().next();

				// .. and print it to the console, cos we all love info. Yeah.
				logger.info("PostgreSQL service active; connected to version {}.", version.getResultSet().getString(1));

				// Also quickly remove the world online counter. Just in case.
				PreparedStatement removeOnline = connection.prepareStatement("DELETE FROM online_characters WHERE world_id=? AND service_id=?;");
				removeOnline.setInt(1, server.world().id());
				removeOnline.setInt(2, 1); // Service id
				int removed = removeOnline.executeUpdate();

				// If the last shutdown wasn't graceful we need to inform.
				if (removed > 0) {
					logger.warn("Removed {} online user(s). Previous shutdown was most likely not graceful.", removed);
				}
			}
		} catch (Exception e) {
			logger.error("Could not connect to the database.", e);
			return false;
		}

		return true;
	}

	@Override
	public boolean stop() {
		connectionPool.close();
		connectionPool = null;
		return true;
	}

	@Override
	public boolean isAlive() {
		return connectionPool != null;
	}

}
