/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model;

import se.kth.karamel.common.clusterdef.json.JsonGroup;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author kamal
 */
public class GroupRuntime {

  public static enum GroupPhase {

    NONE, PRECLEANING, PRECLEANED, FORKING_GROUPS, GROUPS_FORKED, FORKING_MACHINES, MACHINES_FORKED, 
    RUNNING_DAG, DAG_DONE, TERMINATING, SCALING_UP_MACHINES, SCALED_UP, SCALING_DOWN_MACHINES, SCALED_DOWN;
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
    for (MachineRuntime machine : machines) {
      addMachine(machine);
    }
  }

  public List<MachineRuntime> getMachines() {
    return machines;
  }

  public synchronized void addMachine(MachineRuntime machineRuntime) {
    this.machines.add(machineRuntime);
    //this.uniqueMachineNames.add(machineRuntime.getUniqueName());
  }
  
  public synchronized void removeMachineWithId(String id) {
    for (Iterator<MachineRuntime> iterator = machines.iterator(); iterator.hasNext();) {
      MachineRuntime machine = iterator.next();
      if (machine.getVmId().equals(id)) {
        iterator.remove();
      }
    }
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
