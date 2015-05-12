/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import se.kth.karamel.client.model.json.JsonGroup;

/**
 *
 * @author kamal
 */
public class GroupRuntime implements Serializable {

  public static enum GroupPhase {

    NONE, PRECLEANING, PRECLEANED, FORKING_GROUPS, GROUPS_FORKED, FORKING_MACHINES, MACHINES_FORKED, INSTALLING, INSTALLED, PURGING;
  }

  private final ClusterRuntime cluster;
  private GroupPhase phase = GroupPhase.NONE;
  private String name;
  private String id;
  private List<MachineRuntime> machines = new ArrayList<>();

  public GroupRuntime(ClusterRuntime cluster) {
    this.cluster = cluster;
  }

  public GroupRuntime(ClusterRuntime cluster, JsonGroup definition) {
    this.cluster = cluster;
    this.name = definition.getName();
  }

  public synchronized void setMachines(List<MachineRuntime> machines) {
    this.machines = machines;
  }

  public List<MachineRuntime> getMachines() {
    return machines;
  }

  public String getId() {
    return id;
  }

  public synchronized void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public GroupPhase getPhase() {
    return phase;
  }

  public synchronized void setPhase(GroupPhase phase) {
    this.phase = phase;
  }
  
  public ClusterRuntime getCluster() {
    return cluster;
  }

}
