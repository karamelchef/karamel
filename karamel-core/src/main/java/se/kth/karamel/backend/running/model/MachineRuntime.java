/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model;

import se.kth.karamel.backend.launcher.OsType;
import se.kth.karamel.backend.running.model.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kamal
 */
public class MachineRuntime {

  public static enum LifeStatus {

    FORKED, CONNECTED, UNREACHABLE, DESTROYED
  }

  public static enum TasksStatus {

    ONGOING, FAILED, PAUSING, PAUSED, EMPTY
  }

  private final GroupRuntime group;
  private LifeStatus lifeStatus = LifeStatus.FORKED;
  private TasksStatus tasksStatus = TasksStatus.EMPTY;
  private String name;
  private String vmId;
  private String privateIp;
  private String publicIp;
  private int sshPort;
  private String sshUser;
  private String machineType;
  private OsType osType;
  private String uniqueName;

  private final List<Task> tasks = new ArrayList<>();

  public MachineRuntime(GroupRuntime group) {
    this.group = group;
  }

  public GroupRuntime getGroup() {
    return group;
  }

  public String getVmId() {
    return vmId;
  }

  public void setVmId(String vmId) {
    this.vmId = vmId;
  }

  public void setOsType(OsType osType) {
    this.osType = osType;
  }

  public OsType getOsType() {
    return osType;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPublicIp() {
    return publicIp;
  }

  public synchronized void setPublicIp(String publicIp) {
    this.publicIp = publicIp;
  }

  public String getPrivateIp() {
    return privateIp;
  }

  public synchronized void setPrivateIp(String privateIp) {
    this.privateIp = privateIp;
  }

  public int getSshPort() {
    return sshPort;
  }

  public synchronized void setSshPort(int sshPort) {
    this.sshPort = sshPort;
  }

  public String getSshUser() {
    return sshUser;
  }

  public synchronized void setSshUser(String sshUser) {
    this.sshUser = sshUser;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public String getMachineType() {
    return machineType;
  }

  public void setMachineType(String machineType) {
    this.machineType = machineType;
  }

  public void addTask(Task task) {
    tasks.add(task);
  }

  public void removeTask(Task task) {
    tasks.remove(task);
  }

  public LifeStatus getLifeStatus() {
    return lifeStatus;
  }

  public synchronized void setLifeStatus(LifeStatus lifeStatus) {
    this.lifeStatus = lifeStatus;
  }

  public TasksStatus getTasksStatus() {
    return tasksStatus;
  }

  public synchronized void setTasksStatus(TasksStatus tasksStatus, String taskId, String failureMessage) {
    this.tasksStatus = tasksStatus;
    if (tasksStatus == TasksStatus.FAILED) {
      group.getCluster().issueFailure(new Failure(Failure.Type.TASK_FAILED, taskId, failureMessage));
    }
  }

  public String getId() {
    return publicIp;
  }

  public String getUniqueName() {
    return uniqueName;
  }

  public void setUniqueName(String uniqueName) {
    this.uniqueName = uniqueName;
  }
}
