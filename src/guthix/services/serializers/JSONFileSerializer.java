package guthix.services.serializers;

import com.google.gson.*;

import guthix.model.Tile;
import guthix.model.entity.Player;
import guthix.model.entity.player.Privilege;
import guthix.model.entity.player.Skills;
import guthix.model.uid.UIDProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.function.Consumer;

/**
 * Created by Bart on 5-3-2015.
 * <p>
 * Simple default serializer to <b>only</b> use in single-server setups because it uses a local file to serialize the
 * player data to by means of GSON.
 */
public class JSONFileSerializer extends PlayerSerializer {

    private static final Logger logger = LogManager.getLogger(JSONFileSerializer.class);

    private Gson gson;

    /**
     * The folder containing the character files.
     */
    private File characterFolder = new File("data/characters");

    public JSONFileSerializer(UIDProvider provider) {
        super(provider);

        gson = new GsonBuilder().create(); //TODO configuration

		/* Create folder if missing */
        characterFolder.mkdirs();
    }

    @Override
    public boolean loadPlayer(Player player, Object uid, String password, Consumer<PlayerLoadResult> fn) {
        File characterFile = new File(characterFolder, player.name() + ".json");

        // If the file does not exist, let the caller know.
        if (!characterFile.exists()) {
            fn.accept(PlayerLoadResult.OK);
            return true;
        }

        try {
            fn.accept(loadPlayer(player, new FileInputStream(characterFile), password));
        } catch (FileNotFoundException e) {
            logger.error("Could not decode JSON player data for {} because the file was missing!", player.name(), e);
            fn.accept(PlayerLoadResult.INVALID_DETAILS);
        }

        return true;
    }

    public static PlayerLoadResult loadPlayer(Player player, InputStream inputStream, String password) {
        // The reason we use this 'weird' approach instead of decode-and-cast with Gson is that refactoring
        // or field removal will create issues, and here we can simply selectively pick which fields we save, and how.
        JsonElement element = new JsonParser().parse(new InputStreamReader(inputStream));

        if (element.isJsonObject()) {
            JsonObject rootObject = element.getAsJsonObject();

            // Check password
            if (!rootObject.get("password").getAsString().equals(password))
                return PlayerLoadResult.INVALID_DETAILS;

			/* Basic information */
            String displayName = rootObject.get("displayName").getAsString();
            Privilege privilege = Privilege.valueOf(rootObject.get("privilege").getAsString());
            JsonObject t = rootObject.get("tile").getAsJsonObject();
            Tile tile = new Tile(t.get("x").getAsInt(), t.get("z").getAsInt(), t.get("level").getAsInt());
            int migration = rootObject.get("migration").getAsInt();

			/* Construct the player */
            player.displayName(displayName);
            player.privilege(privilege);
            player.migration(migration);
            player.teleport(tile);

            player.tile(tile);

			/* Skill information */
            JsonObject skills = rootObject.get("skills").getAsJsonObject();
            JsonArray levels = skills.get("lvl").getAsJsonArray();
            JsonArray xp = skills.get("xp").getAsJsonArray();

            for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
                player.skills().xp()[skill] = xp.get(skill).getAsDouble();
                player.skills().levels()[skill] = levels.get(skill).getAsInt();
            }
            player.skills().recalculateCombat();

            return PlayerLoadResult.OK;
        }

        return PlayerLoadResult.INVALID_DETAILS;
    }

    @Override
    public void savePlayer(Player player) {

    }

}
