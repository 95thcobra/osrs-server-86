package guthix.task;

import java.util.Collection;

import guthix.model.World;
import guthix.model.entity.Npc;

/**
 * Created by Bart on 8/26/2015.
 */
public class NpcProcessingTask implements Task {

	@Override
	public void execute(World world) {
		world.npcs().forEach(Npc::cycle);
	}

	@Override
	public boolean isAsyncSafe() {
		return false;
	}

	@Override
	public Collection<SubTask> createJobs(World world) {
		return null;
	}

}
