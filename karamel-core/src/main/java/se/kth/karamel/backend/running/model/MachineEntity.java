/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model;

import java.util.ArrayList;
import java.util.List;
import se.kth.karamel.backend.running.model.tasks.Task;

/**
 *
 * @author kamal
 */
public class MachineEntity {

  public static enum LifeStatus {

    FORKED, CONNECTED, UNREACHABLE, DESTROYED
  }

  public static enum TasksStatus {

    ONGOING, FAILED
  }

  private LifeStatus lifeStatus = LifeStatus.FORKED;
  private TasksStatus tasksStatus = TasksStatus.ONGOING;
  private String privateIp;
  private String publicIp;
  private int sshPort;
  private String sshUser;

  private List<Task> tasks = new ArrayList<>();

  public String getPublicIp() {
    return publicIp;
  }

  public void setPublicIp(String publicIp) {
    this.publicIp = publicIp;
  }

  public String getPrivateIp() {
    return privateIp;
  }

  public void setPrivateIp(String privateIp) {
    this.privateIp = privateIp;
  }

  public int getSshPort() {
    return sshPort;
  }

  public void setSshPort(int sshPort) {
    this.sshPort = sshPort;
  }

  public String getSshUser() {
    return sshUser;
  }

  public void setSshUser(String sshUser) {
    this.sshUser = sshUser;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public void addTask(Task task) {
    tasks.add(task);
  }

  public LifeStatus getLifeStatus() {
    return lifeStatus;
  }

  public void setLifeStatus(LifeStatus lifeStatus) {
    this.lifeStatus = lifeStatus;
  }

  public TasksStatus getTasksStatus() {
    return tasksStatus;
  }

  public void setTasksStatus(TasksStatus tasksStatus) {
    this.tasksStatus = tasksStatus;
  }

  public String getId() {
    return sshUser + "@" + publicIp;
  }
}
