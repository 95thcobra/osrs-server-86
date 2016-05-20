package guthix.task;

import java.util.Collection;

import guthix.model.Tile;
import guthix.model.World;
import guthix.model.entity.Npc;
import guthix.model.entity.PathQueue;
import guthix.model.entity.Player;
import guthix.net.message.game.DisplayMap;

/**
 * Created by Bart Pelle on 8/10/2015.
 */
public class NpcPreSyncTask implements Task {

	@Override
	public void execute(World world) {
		world.npcs().forEach(this::preUpdate);
	}

	private void preUpdate(Npc npc) {
		// Process path
		if (!npc.pathQueue().empty()) {
			PathQueue.Step walkStep = npc.pathQueue().next();
			int walkDirection = PathQueue.calculateDirection(npc.tile().x, npc.tile().z, walkStep.x, walkStep.z);
			int runDirection = -1;
			npc.tile(new Tile(walkStep.x, walkStep.z, npc.tile().level));

			if ((walkStep.type == PathQueue.StepType.FORCED_RUN || npc.pathQueue().running()) && !npc.pathQueue().empty() && walkStep.type != PathQueue.StepType.FORCED_WALK) {
				PathQueue.Step runStep = npc.pathQueue().next();
				runDirection = PathQueue.calculateDirection(npc.tile().x, npc.tile().z, runStep.x, runStep.z);
				npc.tile(new Tile(walkStep.x, walkStep.z, npc.tile().level));
			}

			npc.sync().step(walkDirection, runDirection);
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
