package guthix.task;

import java.util.Collection;

import guthix.model.World;
import guthix.model.entity.Npc;
import guthix.model.entity.PathQueue;

/**
 * Created by Bart Pelle on 8/10/2015.
 */
public class NpcPostSyncTask implements Task {

	@Override
	public void execute(World world) {
		world.npcs().forEach(this::postUpdate);
	}

	private void postUpdate(Npc npc) {
		npc.sync().clear();
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
