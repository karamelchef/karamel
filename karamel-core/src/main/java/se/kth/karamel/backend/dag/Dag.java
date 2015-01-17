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

import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;
import com.google.common.base.Joiner;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonObject;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * A class that can represent a directed-acyclic-graph (DAG) consisting of tasks (Runnable objects) and their
 * dependencies (on other Runnable objects in the graph).
 */
public class Dag {

  private static final Logger logger = Logger.getLogger(Dag.class);
  private final HashSet<Runnable> tasks = new HashSet<>();
  private final ArrayListMultimap<Runnable, Runnable> editableDeps = ArrayListMultimap
          .create();
  private final Map<Runnable, Set<? extends Runnable>> nonEditableDeps = new HashMap<>();
  private final Map<Runnable, Throwable> errors = new HashMap<>();

  public enum Status {

    /**
     * All tasks were successfully scheduled.
     */
    COMPLETED_ALL_TASKS,
    /**
     * Some tasks resulted in errors. Check getErrors() for the errors.
     */
    ERRORS,
    /**
     * The dependencies formed a cycle resulting in some tasks not being able to be scheduled.
     */
    INVALID_DEPENDENCIES
  }

  /**
   * Determines the status of this graph. Call this only after the DAG has been been executed by a DAGExecutor.
   *
   * @return
   */
  public synchronized Status status() {
    if (tasks.isEmpty()) {
      return Status.COMPLETED_ALL_TASKS;
    }
    if (!errors.isEmpty()) {
      return Status.ERRORS;
    }
    if (tasks.size() > 0) {
      return Status.INVALID_DEPENDENCIES;
    }
    throw new RuntimeException("entered unknown state");
  }

  /**
   * Returns a mapping from failed tasks to the exceptions each threw.
   *
   * @return
   */
  public synchronized Map<Runnable, Throwable> getErrors() {
    return errors;
  }

  /**
   * Find the next runnable task, without removing it from _tasks
   */
  private synchronized Runnable peekNextRunnableTask() {
    for (Runnable t : tasks) {
      if (editableDeps.containsKey(t)) {
        List<Runnable> v = editableDeps.get(t);
        if (v.isEmpty()) {
          return t;
        }
      } else {
        return t;
      }
    }
    return null;
  }

  /**
   * Determine if there is a task that can now be run, because it has no outstanding unfinished dependencies
   *
   * @return
   */
  public synchronized boolean hasNextRunnableTask() {
    return peekNextRunnableTask() != null;
  }

  /**
   * Determine if there are any remaining tasks in this executor. If hasNextRunnableTask() has returned true, then these
   * remaining tasks cannot be scheduled due to failed dependencies or cycles in the graph.
   *
   * @return
   */
  public synchronized boolean hasTasks() {
    return tasks.size() > 0;
  }

  /**
   * Add an in-degree-zero task to this graph.
   *
   * @param task
   */
  public synchronized void insert(Runnable task) {
    logger.debug(String.format("Task[%s] no dependency.", task));
    tasks.add(task);
  }

  /**
   * Add a task that depends upon another specified task to this DAG.
   *
   *
   * @param task
   * @param dependency
   */
  public synchronized void insert(Runnable task, Runnable dependency) {
    logger.debug(String.format("Task[%s] depends on:", task));
    logger.debug(String.format("    [%s]", dependency));
    tasks.add(task);
    editableDeps.put(task, dependency);
//    nonEditableDeps.put(task, dependency);
  }

  /**
   * Add a task that depends upon a set of tasks to this DAG.
   *
   *
   * @param task
   * @param deps
   */
  public synchronized void insert(Runnable task, Set<? extends Runnable> deps) {
    logger.debug(String.format("Task[%s] depends on:", task));
    for (Runnable dep : deps) {
      logger.debug(String.format("    [%s]", dep));
    }
    tasks.add(task);
    editableDeps.putAll(task, deps);
    nonEditableDeps.put(task, deps);
  }

  public synchronized Runnable nextRunnableTask() {
    Runnable r = peekNextRunnableTask();
    tasks.remove(r);
    return r;
  }

  public synchronized void notifyDone(Runnable task) {
    // Remove t from the list of remaining dependencies for any other tasks.
    Set<Runnable> keySet = new HashSet<>();
    keySet.addAll(editableDeps.keySet());
    for (Runnable key : keySet) {
      if (editableDeps.get(key).contains(task)) {
        editableDeps.remove(key, task);
      }
    }
  }

  public synchronized void notifyError(Runnable r, Throwable error) {
    errors.put(r, error);
  }

  public int numTasks() {
    return tasks.size();
  }

  /**
   * Verify the validity of the DAG, throwing exceptions if invalid dependencies are found.
   *
   * @throws se.kth.karamel.backend.dag.DependencyDoesNotExistException
   */
  public void verifyValidGraph() throws DependencyDoesNotExistException {
    for (Runnable d : editableDeps.values()) {
      if (!tasks.contains(d)) {
        throw new DependencyDoesNotExistException(d);
      }
    }
  }

  public String d3Json() {
    JsonObject root = new JsonObject();
    Set<Runnable> keys = nonEditableDeps.keySet();
    for (Runnable key : keys) {
      RunRecipeTask r = (RunRecipeTask) key;
      Set<? extends Runnable> deps = nonEditableDeps.get(key);
      String depsString = Joiner.on(",").join(deps);
      JsonObject child = new JsonObject();
      //TODO: uncomment
//      child.addProperty("recipe", r.getRecipeName());
//      child.addProperty("machine", r.getMachine().getPublicIps().get(0));
//      child.addProperty("status", r.getStatus().toString());
//      child.addProperty("input", depsString);
//      root.add(r.hash(), child);
    }
    return root.toString();
  }

}
