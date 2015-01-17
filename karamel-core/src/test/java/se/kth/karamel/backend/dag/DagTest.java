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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;



public class DagTest extends TestCase {

	List<String> _result = Collections.synchronizedList(new ArrayList<String>());
	
	/** Create a DAG and pretend to schedule some tests  */
	public void testDagPretendSchedule() throws DependencyDoesNotExistException {

		Dag dag = new Dag();
		assertFalse(dag.hasTasks());

		Task t0 = new Task("t0");
		Task t1 = new Task("t1");
		Task t2 = new Task("t2");
		Task t3 = new Task("t3");

		dag.insert(t3);
		assertTrue(dag.hasTasks());

		dag.insert(t2, t3);
		dag.insert(t1, t2);
		dag.insert(t0, t1);
	
		assertTrue(dag.getErrors().isEmpty());
		assertTrue(dag.hasNextRunnableTask());
		assertEquals(t3,dag.nextRunnableTask());
		dag.notifyDone(t3);

		assertTrue(dag.hasNextRunnableTask());
		assertEquals(t2,dag.nextRunnableTask());
		dag.notifyDone(t2);

		assertTrue(dag.hasNextRunnableTask());
		assertEquals(t1,dag.nextRunnableTask());
		dag.notifyDone(t1);
		
		assertTrue(dag.hasNextRunnableTask());
		assertEquals(t0,dag.nextRunnableTask());
		dag.notifyDone(t0);
		
		assertFalse(dag.hasNextRunnableTask());
		assertTrue(dag.getErrors().isEmpty());
		assertEquals(Dag.Status.COMPLETED_ALL_TASKS,dag.status());

	}

	public void testSinglethreaded() 
	throws InterruptedException, DependencyDoesNotExistException {
		SingleThreadedDagExecutor executor = new SingleThreadedDagExecutor();
		testExecutor(executor);
	}

	public void testMultithreaded() 
	throws InterruptedException, DependencyDoesNotExistException {
		MultiThreadedDagExecutor executor = new MultiThreadedDagExecutor();
		testExecutor(executor);
	}
	

	public void testSinglethreadedCycle() 
	throws InterruptedException, DependencyDoesNotExistException {
		SingleThreadedDagExecutor executor = new SingleThreadedDagExecutor();
		testCycleExecutor(executor);
	}

	public void testMultithreadedCycle() 
	throws InterruptedException, DependencyDoesNotExistException {
		MultiThreadedDagExecutor executor = new MultiThreadedDagExecutor();
		testCycleExecutor(executor);
	}

	/** Create a DAG and pretend to schedule some tests  */
	public void testVariableLengthTasks() throws InterruptedException, DependencyDoesNotExistException {
		MultiThreadedDagExecutor executor = new MultiThreadedDagExecutor();

		_result = new ArrayList<String>();
		Dag dag = new Dag();
		assertFalse(dag.hasTasks());

		Task t0 = new Task("t0", 000);
		Task t1 = new Task("t1", 100);
		Task t2 = new Task("t2", 100);
		Task t3 = new Task("t3", 150);

		dag.insert(t0);
		dag.insert(t1, t0);
		dag.insert(t2, t1);
		dag.insert(t3);
		
		executor.submit(dag);
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.SECONDS);
		
		assertFalse(dag.hasNextRunnableTask());
		assertTrue(dag.getErrors().isEmpty());
		assertEquals(Dag.Status.COMPLETED_ALL_TASKS,dag.status());
		
		String[] expecteds = {"t0", "t1", "t3", "t2"};
		assertTrue(Arrays.equals(expecteds, _result.toArray()));
	}
	
	/** Create a DAG and pretend to schedule some tests  */
	public void testExecutor(DagExecutor executor) throws InterruptedException, DependencyDoesNotExistException {

		_result = new ArrayList<String>();
		Dag dag = new Dag();
		assertFalse(dag.hasTasks());

		Task t0 = new Task("t0");
		Task t1 = new Task("t1");
		Task t2 = new Task("t2");
		Task t3 = new Task("t3");

		dag.insert(t3);
		dag.insert(t2, t3);
		dag.insert(t1, t2);
		dag.insert(t0, t1);
		
		executor.submit(dag);
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.SECONDS);
		
		assertFalse(dag.hasNextRunnableTask());
		assertTrue(dag.getErrors().isEmpty());
		assertEquals(Dag.Status.COMPLETED_ALL_TASKS,dag.status());
		
		String[] expecteds = {"t3", "t2", "t1", "t0"};
		assertTrue(Arrays.equals(expecteds, _result.toArray()));
		
	}
	

	/** Create a DAG and pretend to schedule some tests  */
	public void testCycleExecutor(DagExecutor executor) 
	throws InterruptedException, DependencyDoesNotExistException {

		_result = new ArrayList<String>();
		Dag dag = new Dag();
		assertFalse(dag.hasTasks());

		int numTasks = 10;
		Task[] tasks = new Task[numTasks];
		for(int i=0; i< numTasks; i++){
			tasks[i] = new Task("t" + i);
		}

		for(int i=0; i< numTasks; i++){
			dag.insert(tasks[i], tasks[(i+1)%numTasks]);
		}
		
		executor.submit(dag);
	
		assertFalse(dag.hasNextRunnableTask());
		assertTrue(dag.getErrors().isEmpty());
		assertEquals(Dag.Status.INVALID_DEPENDENCIES,dag.status());
		
		String[] expecteds = {};
		assertTrue(Arrays.equals(expecteds, _result.toArray()));
		
	}
	
	

	public void testDeadlock() 
	throws InterruptedException, DependencyDoesNotExistException {

		_result = new ArrayList<String>();
		
		// Build DAG that is a cycle
		Dag dag = new Dag();
		int numTasks = 10;
		Task[] tasks = new Task[numTasks];
		for(int i=0; i< numTasks; i++){
			tasks[i] = new Task("t" + i);
		}
		for(int i=0; i< numTasks; i++){
			dag.insert(tasks[i], tasks[(i+1)%numTasks]);
		}
		
		MultiThreadedDagExecutor executor = new MultiThreadedDagExecutor();
		executor.submit(dag);
		executor.shutdown();
		assertTrue(executor.awaitTermination(500, TimeUnit.MILLISECONDS));
		
		assertEquals(Dag.Status.INVALID_DEPENDENCIES,dag.status());
		
		String[] expecteds = {};
		assertTrue(Arrays.equals(expecteds, _result.toArray()));
		
	}
	
	public class Task implements Runnable {

		private final String _name;
		private final long _sleepMillis;
		
		public Task(String name){
			_name = name;
			_sleepMillis = 0;
		}
		
		public Task(String name, long sleepMillis) { 
			_name = name;
			_sleepMillis = sleepMillis;
		}
		
		@Override
		public void run() {
			if(_sleepMillis>0){
				try {
					Thread.sleep(_sleepMillis);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			_result.add(_name);
		}
		
	}

}
