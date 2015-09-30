/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.stats;

/**
 *
 * @author kamal
 */
public class TaskStat {

  private long id;
  private String taskId;
  private String machineType;
  private String status;
  private long duration;

  public TaskStat(String taskId, String machineType, String status, long duration) {
    this.taskId = taskId;
    this.machineType = machineType;
    this.status = status;
    this.duration = duration;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }
  
  public void setRecipeId(String recipeId) {
    this.taskId = recipeId;
  }

  public String getRecipeId() {
    return taskId;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public long getDuration() {
    return duration;
  }

  public void setMachineType(String machineType) {
    this.machineType = machineType;
  }

  public String getMachineType() {
    return machineType;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
  
}
