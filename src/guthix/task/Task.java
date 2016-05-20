package guthix.task;

import java.util.Collection;
import java.util.concurrent.Callable;

import guthix.model.World;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public interface Task {

	public void execute(World world);

	public boolean isAsyncSafe();

	public Collection<SubTask> createJobs(World world);

}
