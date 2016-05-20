package guthix.migration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;

import guthix.model.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Bart on 8/1/2015.
 *
 * Manages known migrations and helps in applying migrations to players.
 */
public class MigrationRepository {

	private static final Logger logger = LogManager.getLogger(MigrationRepository.class);

	/**
	 * A linked list of migrations in sorted order from lowest ID to highest ID.
	 */
	private List<Migration> migrations = new LinkedList<>();

	public MigrationRepository() {
		logger.info("Scanning for migrations...");

		Set<Class<? extends Migration>> migrationClasses = new Reflections("nl.bartpelle.veteres",
				new SubTypesScanner(false)).getSubTypesOf(Migration.class);

		// Add all the migrations to the migration list
		for (Class<? extends Migration> mClass : migrationClasses) {
			try {
				migrations.add(mClass.newInstance());
			} catch (Exception e) {
				logger.fatal("Could not create migration with class {}; halting server for safety reasons!", e);
				System.exit(0);
			}
		}

		// Sort the migrations in incremental order based on the ID
		Collections.sort(migrations, (o1, o2) -> o1.id() - o2.id());

		if (migrations.size() == 0)
			logger.info("No migrations found.");
		else
			logger.info("Registered {} migrations, with last ID being {}.", migrations.size(), migrations.get(migrations.size() - 1).id());
	}

	/**
	 * Processes migrations for a player. Only migrations that are not yet processed are applied to the player.
	 * The last migration ID of the player is then set and the next one in line is processed.
	 *
	 * @param player The player who we apply the migrations to.
	 * @return <code>true</code> if everything went well, <code>false</code> if shit hit the fan and we need to block login.
	 */
	public boolean process(Player player) {
		for (Migration migration : migrations) {
			if (migration.id() >= player.migration()) { // Does this require applying?
				if (!migration.apply(player)) {
					return false; // Ah shit, that went wrong.
				}

				player.migration(migration.id()); // Update the last successful migration.
			}
		}

		return true;
	}

}
