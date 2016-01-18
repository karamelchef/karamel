/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.dag.DagTask;
import se.kth.karamel.backend.dag.DagTaskCallback;
import se.kth.karamel.backend.machines.MachineInterface;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.Failure;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.stats.TaskStat;

/**
 *
 * @author kamal
 */
public abstract class Task implements DagTask, TaskCallback {

  private static final Logger logger = Logger.getLogger(Task.class);

  public static enum Status {

    WAITING, READY, EXIST, ONGOING, DONE, FAILED;
  }
  private Status status = Status.WAITING;
  private final String name;
  private final String id;
  private final String machineId;
  protected List<ShellCommand> commands;
  private final MachineRuntime machine;
  private final String uuid;
  private final boolean idempotent;
  private DagTaskCallback dagCallback;
  private final TaskSubmitter submitter;
  private final ClusterStats clusterStats;
  private long startTime;
  private long duration = 0;
  private boolean markSkip = false;

  public Task(String name, String id, boolean idempotent, MachineRuntime machine, ClusterStats clusterStats, 
      TaskSubmitter submitter) {
    this.name = name;
    this.id = id;
    this.idempotent = idempotent;
    this.machineId = machine.getId();
    this.machine = machine;
    this.uuid = UUID.randomUUID().toString();
    this.clusterStats = clusterStats;
    this.submitter = submitter;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public long getDuration() {
    return duration;
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

  public String getId() {
    return id;
  }

  public boolean isIdempotent() {
    return idempotent;
  }

  public void markSkip() {
    this.markSkip = true;
  }

  public boolean isMarkSkip() {
    return markSkip;
  }
  
  public abstract List<ShellCommand> getCommands() throws IOException;

  public void setCommands(List<ShellCommand> commands) {
    this.commands = commands;
  }

  @Override
  public String asJson() {
    return String.format("\"name\": \"%s\", \"machine\": \"%s\"", name, machineId);
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

  @Override
  public String dagNodeId() {
    return uniqueId();
  }

  @Override
  public void prepareToStart() {
    try {
      submitter.prepareToStart(this);
    } catch (KaramelException ex) {
      machine.getGroup().getCluster().issueFailure(new Failure(Failure.Type.TASK_FAILED, uuid, ex.getMessage()));
      logger.error("", ex);
      dagCallback.failed(ex.getMessage());
    }
  }

  @Override
  public void submit(DagTaskCallback callback) {
    this.dagCallback = callback;
    try {
      submitter.submitTask(this);
    } catch (KaramelException ex) {
      machine.getGroup().getCluster().issueFailure(new Failure(Failure.Type.TASK_FAILED, uuid, ex.getMessage()));
      logger.error("", ex);
      dagCallback.failed(ex.getMessage());
    }
  }

  @Override
  public void queued() {
    status = Status.READY;
    dagCallback.queued();
  }

  @Override
  public void exists() {
    status = Status.EXIST;
    dagCallback.exists();
  }
  
  @Override
  public void started() {
    status = Status.ONGOING;
    startTime = System.currentTimeMillis();
    dagCallback.started();
  }

  @Override
  public void succeed() {
    status = Status.DONE;
    addStats();
    dagCallback.succeed();
  }

  @Override
  public void failed(String reason) {
    this.status = Status.FAILED;
    machine.setTasksStatus(MachineRuntime.TasksStatus.FAILED, uuid, reason);
    addStats();
    dagCallback.failed(reason);
  }

  public void collectResults(MachineInterface sshMachine) throws KaramelException {
    //override it in the subclasses if needed
  }

  public void downloadExperimentResults(MachineInterface sshMachine) throws KaramelException {
    //override it in the subclasses if needed
  }

  public String getSudoCommand() {
    String password = ClusterService.getInstance().getCommonContext().getSudoAccountPassword();
    return (password == null || password.isEmpty()) ? "sudo" : "echo \"%password_hidden%\" | sudo -S ";
  }

  private void addStats() {
    duration = System.currentTimeMillis() - startTime;
    TaskStat taskStat = new TaskStat(getId(), machine.getMachineType(), status.name(), duration);
    clusterStats.addTask(taskStat);
  }
  
  public void kill() throws KaramelException {
    submitter.killMe(this);
  }
}
