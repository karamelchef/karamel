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


/** A DAGExecutor that runs tasks one at a time sequentially in the thread that calls submit(). */
public class SingleThreadedDagExecutor implements DagExecutor {

	@Override
	public boolean awaitTermination(long timeout, TimeUnit units) {
		return true;
	}

	@Override
	public boolean isShutdown() {
		return true;
	}

	@Override
	public boolean isTerminated() {
		return true;
	}

	@Override
	public void shutdown() {
		// do nothing
	}

	@Override
	public void shutdownNow() {
	}

	@Override
	public void submit(Dag taskGraph) {
		while (taskGraph.hasNextRunnableTask()) {
			Runnable t = taskGraph.nextRunnableTask();
			boolean hadError = false;

			try {
				t.run();
			} catch (Throwable err) {
				hadError = true;
				taskGraph.notifyError(t, err);
			}

			if (!hadError) {
				taskGraph.notifyDone(t);
			}
		}
	}

	
	
}
