/*
   Copyright 2011 Isaac Dooley

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package se.kth.karamel.backend.dag;

import se.kth.karamel.backend.dag.Dag;
import java.util.concurrent.TimeUnit;

public interface DagExecutor {

	/**
	 * Submit a graph of tasks for execution. The tasks in the specified
	 * taskGraph will be executed in an order that respects the dependencies
	 * specified in the graph. No task will be run until all its dependencies
	 * have finished running successfully (not thrown any exceptions).
	 * 
	 * @param taskGraph
	 *            A graph of tasks to execute
	 * @throws InterruptedException
	 *             The calling thread has been interrupted
	 * @throws DependencyDoesNotExistException
	 *             The specified graph contains dependency tasks that have not
	 *             yet been added to the graph (thus it is not really a graph).
	 */
	public void submit(Dag taskGraph) 
			throws InterruptedException, DependencyDoesNotExistException;

	/**
	 * Blocks until all schedulable tasks have completed execution after a
	 * shutdown request, at least one task's run() method throws an exception,
	 * the timeout occurs, or the current thread is interrupted, whichever
	 * happens first.
	 * 
	 * @retun true if the executor terminated, and false if timed-out before
	 *        completing all tasks.
	 */
	public boolean awaitTermination(long timeout, TimeUnit units)
			throws InterruptedException;

	/**
	 * Gracefully shutdown the executor. New calls to submit() will fail, and
	 * already submitted tasks will be executed.
	 * 
	 * @retun true if the executor terminated, and false if timed-out before
	 *        completing all tasks.
	 */
	public void shutdown();

	/**
	 * Immediately initiate a shutdown of the executor. New calls to submit()
	 * will fail, and all currently executing tasks will be interrupted.
	 */
	public void shutdownNow();

	
	public boolean isShutdown();

	public boolean isTerminated();

}
