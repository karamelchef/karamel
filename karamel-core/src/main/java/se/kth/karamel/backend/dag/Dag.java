 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.dag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import se.kth.karamel.common.exception.DagConstructionException;

/**
 *
 * @author kamal
 */
public class Dag {

  private static final Logger logger = Logger.getLogger(Dag.class);
  private final Map<String, DagNode> allNodes = new HashMap<>();

  public void addNode(String nodeId) throws DagConstructionException {
    if (allNodes.containsKey(nodeId)) {
      throw new DagConstructionException(String.format("Node '%s' has been created once", nodeId));
    }

    allNodes.put(nodeId, new DagNode(nodeId));
  }

  public void addTask(DagTask task) throws DagConstructionException {
    logger.debug("Adding task: " + task.dagNodeId());
    DagNode node = null;
    if (!allNodes.containsKey(task.dagNodeId())) {
      node = new DagNode(task.dagNodeId(), task);
      allNodes.put(task.dagNodeId(), node);
    } else {
      node = allNodes.get(task.dagNodeId());
      if (node.getTask() != null) {
        throw new DagConstructionException(String.format("Task '%s' already exist.", task.dagNodeId()));
      }
      node.setTask(task);
    }
    for (String first : task.dagDependencies()) {
      addDependency(first, task.dagNodeId());
    }
  }

  public boolean addDependency(String first, String next) throws DagConstructionException {
    if (first == null || first.isEmpty() || next == null || next.isEmpty()) {
      throw new DagConstructionException(String.format("Dependencies cannot be null or empty: %s -> %s", first, next));
    }

    if (first.equals(next)) {
      throw new DagConstructionException(String.format("Cyrcular dependency is not allowed: %s -> %s", first, next));
    }

    logger.debug("Adding dependency: " + first + " -> " + next);
    DagNode firstNode;
    if (allNodes.containsKey(first)) {
      firstNode = allNodes.get(first);
    } else {
      firstNode = new DagNode(first);
      allNodes.put(first, firstNode);
    }

    DagNode nextNode;
    if (allNodes.containsKey(next)) {
      nextNode = allNodes.get(next);
    } else {
      nextNode = new DagNode(next);
      allNodes.put(next, nextNode);
    }
    return firstNode.addSuccessor(nextNode);
  }

  public void start() throws DagConstructionException {
    validate();
    logger.info("Dag is starting: \n" + print());
    String prob = UUID.randomUUID().toString();
    for (DagNode node : findRootNodes()) {
      node.prepareToStart(prob);
    }
    for (DagNode node : findRootNodes()) {
      node.start();
    }
  }

  public void termiante() {
    for (DagNode node : findRootNodes()) {
      node.terminate();
    }
  }

  public boolean isFailed() {
    for (DagNode node : allNodes.values()) {
      if (node.getStatus() == DagNode.Status.FAILED) {
        return true;
      }
    }
    return false;
  }

  public boolean isDone() {
    for (DagNode node : allNodes.values()) {
      if (node.getStatus() != DagNode.Status.DONE && node.getStatus() != DagNode.Status.EXIST) {
        return false;
      }
    }
    return true;
  }

  public void validate() throws DagConstructionException {
    detectCycles();
    for (DagNode node : allNodes.values()) {
      if (node.getTask() == null) {
        throw new DagConstructionException(String.format("No task assigned to '%s' while it appreard in dependencies.. "
            + "predecessors: %s successors: %s", node.getId(), node.getPredecessors().toString(),
            node.getSuccessors().toString()));
      }
    }
  }

  public void detectCycles() throws DagConstructionException {
    String probe = UUID.randomUUID().toString();
    for (DagNode node : allNodes.values()) {
      node.detectCylcles(probe, Collections.EMPTY_LIST);
    }
  }

  public Set<DagNode> findRootNodes() {
    HashSet<DagNode> roots = new HashSet<>();
    for (DagNode s : allNodes.values()) {
      if (s.getPredecessors().isEmpty()) {
        roots.add(s);
      }
    }
    return roots;
  }

  public String print() throws DagConstructionException {
    detectCycles();
    for (DagNode node : findRootNodes()) {
      node.findMaxIndentionLevel(2);
    }
    String prob = UUID.randomUUID().toString();
    StringBuilder builder = new StringBuilder();
    for (DagNode node : findRootNodes()) {
      builder.append("\n").append(node.printBfs(prob, "1|", 2));
    }
    return builder.toString();
  }

  public boolean isRoot(String nodeId) {
    if (allNodes.containsKey(nodeId)) {
      DagNode node = allNodes.get(nodeId);
      return (node.getPredecessors().isEmpty());
    }
    return false;
  }

  public boolean hasDependency(String first, String next) {
    if (allNodes.containsKey(first) && allNodes.containsKey(next)) {
      DagNode firstNode = allNodes.get(first);
      DagNode nextNode = allNodes.get(next);
      if (firstNode.getSuccessors().contains(nextNode) && nextNode.getPredecessors().contains(firstNode)) {
        return true;
      }
    }
    return false;
  }

  public String asJson() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    Collection<DagNode> values = allNodes.values();
    int i = 0;
    for (DagNode dagNode : values) {
      i++;
      builder.append(dagNode.toJson());
      if (i != allNodes.size()) {
        builder.append(",");
      }
    }
    builder.append("]");
    return builder.toString();
  }

}
