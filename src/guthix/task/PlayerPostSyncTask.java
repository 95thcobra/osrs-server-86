package guthix.task;

import java.util.Collection;

import guthix.model.World;
import guthix.model.entity.Player;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public class PlayerPostSyncTask implements Task {

	@Override
	public void execute(World world) {
		world.players().forEach(this::postUpdate);
	}

	private void postUpdate(Player player) {
		player.sync().clear();
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
