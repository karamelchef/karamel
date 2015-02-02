/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model;

import java.util.ArrayList;
import java.util.List;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonGroup;

/**
 *
 * @author kamal
 */
public class ClusterEntity {

  public static enum ClusterPhases {
    NONE, PRECLEANING, PRECLEANED, FORKING_GROUPS, GROUPS_FORKED, FORKING_MACHINES, MACHINES_FORKED, INSTALLING, INSTALLED, PURGING ;
  }
  
  private final String name;
  
  private ClusterPhases phase = ClusterPhases.NONE;
  
  private boolean failed = false;
  
  private boolean paused = false;

  private List<GroupEntity> groups = new ArrayList<>();

  public ClusterEntity(String name) {
    this.name = name;
  }

  public ClusterEntity(JsonCluster definition) {
    this.name = definition.getName();
    List<JsonGroup> definedGroups = definition.getGroups();
    for (JsonGroup jg : definedGroups) {
      GroupEntity group = new GroupEntity(this, jg);
      groups.add(group);
    }
  }

  public String getName() {
    return name;
  }
  
  public void setGroups(List<GroupEntity> groups) {
    this.groups = groups;
  }

  public List<GroupEntity> getGroups() {
    return groups;
  }

  public ClusterPhases getPhase() {
    return phase;
  }

  public void setPhase(ClusterPhases phase) {
    this.phase = phase;
  }

  public boolean isFailed() {
    return failed;
  }

  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  public boolean isPaused() {
    return paused;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

}
