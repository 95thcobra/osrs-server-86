package guthix.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import guthix.ServerProcessor;
import guthix.model.World;

import java.util.Collection;

/**
 * Created by Bart on 3-2-2015.
 */
public class ScriptProcessingTask implements Task {

	private static final Logger logger = LogManager.getLogger(ServerProcessor.class);

	@Override
	public void execute(World world) {
		world.cycle();
		//world.server().scriptExecutor().cycle();
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
