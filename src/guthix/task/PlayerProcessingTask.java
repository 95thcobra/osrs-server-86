package guthix.task;

import java.util.Collection;

import guthix.model.World;
import guthix.model.entity.Player;

/**
 * Created by Bart on 5-3-2015.
 */
public class PlayerProcessingTask implements Task {

	@Override
	public void execute(World world) {
		world.players().forEachShuffled(Player::cycle);
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
