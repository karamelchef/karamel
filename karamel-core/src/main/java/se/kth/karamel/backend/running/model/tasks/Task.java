/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import se.kth.karamel.backend.running.model.MachineRuntime;

/**
 *
 * @author kamal
 */
public abstract class Task {

  public static enum Status {

    WAITING, READY, ONGOING, DONE, FAILED;
  }
  private Status status = Status.WAITING;
  private final String name;
  private final String machineId;
  protected List<ShellCommand> commands;
  private final MachineRuntime machine;
  private final String uuid;

  public Task(String name, MachineRuntime machine) {
    this.name = name;
    this.machineId = machine.getId();
    this.machine = machine;
    this.uuid = UUID.randomUUID().toString();
  }

  public void setStatus(Status status, String message) {
    this.status = status;
    if (status == Status.FAILED) {
      machine.setTasksStatus(MachineRuntime.TasksStatus.FAILED, uuid, message);
    }
  }

  public Status getStatus() {
    return status;
  }

  public String getMachineId() {
    return machineId;
  }

  public String getName() {
    return name;
  }

  public abstract List<ShellCommand> getCommands() throws IOException;

  public void setCommands(List<ShellCommand> commands) {
    this.commands = commands;
  }

  @Override
  public String toString() {
    return name + " on " + machineId;
  }

  public abstract String uniqueId();

  public MachineRuntime getMachine() {
    return machine;
  }

  public String getUuid() {
    return uuid;
  }
  
}
