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

import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.backend.dag.Dag;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A DAGExecutor that runs tasks in separate threads as part of a
 * CachedThreadExecutor.
 */
public class MultiThreadedDagExecutor implements DagExecutor {

  /**
   * A pool of threads for running tasks in the DAG itself.
   */
  final private ExecutorService _taskPool;

  /**
   * A pool of threads for use in managing the execution of the DAG
   */
  final private ExecutorService _managePool;

  /**
   * Create a DAGExecutor that schedules tasks in a CachedThreadPool consisting
   * of as many threads as needed at a time to schedule all available tasks in
   * the DAG.
   */
  public MultiThreadedDagExecutor() {
    _taskPool = Executors.newCachedThreadPool();
    _managePool = Executors.newCachedThreadPool();
  }

  /**
   * Create a DAGExecutor that schedules tasks in a FixedThreadPool consisting
   * of at most the specified number of threads.
   */
  public MultiThreadedDagExecutor(int maxNumWorkerThreads) {
    _taskPool = Executors.newFixedThreadPool(maxNumWorkerThreads);
    _managePool = Executors.newCachedThreadPool();
  }

  @Override
  public final boolean awaitTermination(long timeout, TimeUnit unit)
          throws InterruptedException {
    return _managePool.awaitTermination(timeout, unit);
  }

  @Override
  public final boolean isShutdown() {
    return _managePool.isShutdown();
  }

  @Override
  public final boolean isTerminated() {
    return _managePool.isTerminated();
  }

  @Override
  public final void shutdown() {
    _managePool.shutdown();
  }

  @Override
  public final void shutdownNow() {
    _managePool.shutdownNow();
    _taskPool.shutdownNow();
  }

  @Override
  public final void submit(Dag taskGraph)
          throws InterruptedException, DependencyDoesNotExistException {
    // Verify task graph is valid
    taskGraph.verifyValidGraph();

    _managePool.execute(new Runner(taskGraph));
  }

  /**
   * The management thread that schedules available tasks from the DAG as they
   * become runnable
   */
  private class Runner implements Runnable {

    final Dag _taskGraph;
    final CountDownLatch _completed = new CountDownLatch(1);

    public Runner(Dag taskGraph) {
      _taskGraph = taskGraph;
    }

    @Override
    public void run() {

      try {
        ArrayBlockingQueue<RunnableWrapper> completionQueue = new ArrayBlockingQueue<RunnableWrapper>(
                _taskGraph.numTasks());

        long currentlyExecuting = 0;

        while (true) {
          while (_taskGraph.hasNextRunnableTask()) {
            Runnable t = _taskGraph.nextRunnableTask();
            RunnableWrapper wrapper = new RunnableWrapper(t,
                    completionQueue);
            currentlyExecuting++;
            _taskPool.execute(wrapper);
          }

          // Wait for one or more of the tasks to complete
          if (currentlyExecuting > 0) {
            do {
              RunnableWrapper rw = completionQueue.take();
              currentlyExecuting--;
              if (rw._err == null) {
                _taskGraph.notifyDone(rw._innerTask);
              } else {
                _taskGraph.notifyError(rw._innerTask, rw._err);
              }
            } while (!completionQueue.isEmpty());
          }

          // Stop if we encountered any exceptions
          if (!_taskGraph.getErrors().isEmpty()) {
            return;
          }

					// Stop if we have no runnable tasks (perhaps a cycle of
          // non-schedulable tasks remains)
          if (!_taskGraph.hasNextRunnableTask()
                  && currentlyExecuting == 0) {
            return;
          }

        }

      } catch (InterruptedException e) {
        // do nothing
      } finally {
        _completed.countDown();
      }
    }
  }

  /**
   * A wrapper Runnable object that calls run() on a provided Runnable object,
   * providing notification of the completion of the other object's run()
   * method. Also records anything thrown by the other object's run() method.
   */
  private class RunnableWrapper implements Runnable {

    private final Runnable _innerTask;
    private final ArrayBlockingQueue<RunnableWrapper> _completionQueue;
    private Throwable _err = null;

    RunnableWrapper(Runnable r,
            ArrayBlockingQueue<RunnableWrapper> completionQueue) {
      _innerTask = r;
      _completionQueue = completionQueue;
    }

    @Override
    public void run() {
      try {
        _innerTask.run();
      } catch (Throwable err) {
        _err = err;
      } finally {
        _completionQueue.add(this);
      }
    }

  }

}
