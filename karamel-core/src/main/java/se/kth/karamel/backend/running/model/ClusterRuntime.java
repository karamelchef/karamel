/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;

/**
 *
 * @author kamal
 */
public class ClusterRuntime {

  public static enum ClusterPhases {
    NOT_STARTED, PRECLEANING, PRECLEANED, FORKING_GROUPS, GROUPS_FORKED, FORKING_MACHINES, MACHINES_FORKED, 
    RUNNING_DAG, DAG_DONE, TERMINATING ;
  }
  
  private final String name;
  
  private ClusterPhases phase = ClusterPhases.NOT_STARTED;
  
  private boolean paused = false;

  private List<GroupRuntime> groups = new ArrayList<>();
  
  private final Map<String, Failure> failures = new HashMap<>();
  
  public ClusterRuntime(String name) {
    this.name = name;
  }

  public ClusterRuntime(JsonCluster definition) {
    this.name = definition.getName();
    List<JsonGroup> definedGroups = definition.getGroups();
    for (JsonGroup jg : definedGroups) {
      GroupRuntime group = new GroupRuntime(this, jg);
      groups.add(group);
    }
    Collections.sort(groups, new Comparator<GroupRuntime>() {
      @Override
      public int compare(GroupRuntime o1, GroupRuntime o2) {
        String group1str = o1.getName().replaceAll("[0-9]", "").trim();
        String group1number = o1.getName().replaceAll("[^0-9]", "").trim();
        String group2str = o2.getName().replaceAll("[0-9]", "").trim();
        String group2number = o2.getName().replaceAll("[^0-9]", "").trim();
  
        if (group1str.equalsIgnoreCase(group2str)) {
          if(!group1number.isEmpty() && !group2number.isEmpty()) {
            return Integer.compare(Integer.parseInt(group1number),
                Integer.parseInt(group2number));
          }
        }
        return group1str.compareTo(group2str);
      }
    });
  }

  public String getName() {
    return name;
  }
  
  public synchronized void setGroups(List<GroupRuntime> groups) {
    this.groups = groups;
  }

  public List<GroupRuntime> getGroups() {
    return groups;
  }

  public ClusterPhases getPhase() {
    return phase;
  }

  public synchronized void setPhase(ClusterPhases phase) {
    this.phase = phase;
  }

  public boolean isFailed() {
    return !failures.isEmpty();
  }

  public synchronized void issueFailure(Failure failure) {
    failures.put(failure.hash(), failure);
  }
  
  public synchronized void resolveFailure(String hash) {
    failures.remove(hash);
  }
  
  public synchronized void resolveFailures() {
    failures.clear();
  }

  public Map<String, Failure> getFailures() {
    return failures;
  }

  public boolean isPaused() {
    return paused;
  }

  public synchronized void setPaused(boolean paused) {
    this.paused = paused;
  }

}
