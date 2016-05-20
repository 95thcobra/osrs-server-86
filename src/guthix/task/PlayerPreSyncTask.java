package guthix.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import guthix.model.Area;
import guthix.model.Tile;
import guthix.model.World;
import guthix.model.entity.PathQueue;
import guthix.model.entity.Player;
import guthix.net.message.game.DisplayMap;
import guthix.net.message.game.SetItems;

import java.util.Collection;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public class PlayerPreSyncTask implements Task {

	private static final Logger logger = LogManager.getLogger(PlayerPreSyncTask.class);

	@Override
	public void execute(World world) {
		world.players().forEach(this::preUpdate);
	}

	private void preUpdate(Player player) {
		// Sync containers, if dirty
		player.precycle();

		// Send map if necessary
		if (player.activeMap() == null) {
			player.write(new DisplayMap(player));
			player.world().syncMap(player, null);
		} else {
			Area prev = player.activeArea();
			int mapx = player.activeMap().x;
			int mapz = player.activeMap().z;
			int dx = player.tile().x - mapx;
			int dz = player.tile().z - mapz;

			if (dx <= 16 || dz <= 16 || dx >= 88 || dz >= 88) {
				player.write(new DisplayMap(player));
				player.world().syncMap(player, prev);
				player.channel().flush();
			}
		}

		// Process path
		if (!player.pathQueue().empty()) {
			PathQueue.Step walkStep = player.pathQueue().next();
			int walkDirection = PathQueue.calculateDirection(player.tile().x, player.tile().z, walkStep.x, walkStep.z);
			int runDirection = -1;
			player.tile(new Tile(walkStep.x, walkStep.z, player.tile().level));

			if ((walkStep.type == PathQueue.StepType.FORCED_RUN || player.pathQueue().running()) && !player.pathQueue().empty() && walkStep.type != PathQueue.StepType.FORCED_WALK) {
				PathQueue.Step runStep = player.pathQueue().next();
				runDirection = PathQueue.calculateDirection(player.tile().x, player.tile().z, runStep.x, runStep.z);
				player.tile(new Tile(runStep.x, runStep.z, player.tile().level));
			}

			player.sync().step(walkDirection, runDirection);
		}
	}

	@Override
	public Collection<SubTask> createJobs(World world) {
		return null;
	}

	@Override
	public boolean isAsyncSafe() {
		return false;
	}

}
