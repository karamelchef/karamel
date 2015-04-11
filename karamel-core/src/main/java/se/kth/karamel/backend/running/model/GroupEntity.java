/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model;

import java.util.ArrayList;
import java.util.List;
import se.kth.karamel.client.model.json.JsonGroup;

/**
 *
 * @author kamal
 */
public class GroupEntity {

  public static enum GroupPhase {

    NONE, PRECLEANING, PRECLEANED, FORKING_GROUPS, GROUPS_FORKED, FORKING_MACHINES, MACHINES_FORKED, INSTALLING, INSTALLED, PURGING;
  }

  private final ClusterEntity cluster;
  private GroupPhase phase = GroupPhase.NONE;
  private boolean failed = false;
  private String name;
  private String id;
  private List<MachineEntity> machines = new ArrayList<>();

  public GroupEntity(ClusterEntity cluster) {
    this.cluster = cluster;
  }

  public GroupEntity(ClusterEntity cluster, JsonGroup definition) {
    this.cluster = cluster;
    this.name = definition.getName();
  }

  public void setMachines(List<MachineEntity> machines) {
    this.machines = machines;
  }

  public List<MachineEntity> getMachines() {
    return machines;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public GroupPhase getPhase() {
    return phase;
  }

  public void setPhase(GroupPhase phase) {
    this.phase = phase;
  }

  public boolean isFailed() {
    return failed;
  }

  public void setFailed(boolean failed) {
    this.failed = failed;
    if (failed) {
      cluster.setFailed(failed);
    }
  }

  public ClusterEntity getCluster() {
    return cluster;
  }

}
