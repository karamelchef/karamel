/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.backend.stats;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kamal
 */
public class ClusterStats {
  
  String id;
  String userId;
  String definition;
  long startTime;
  long endTime;
  List<PhaseStat> phases = new ArrayList<>();
  List<TaskStat> tasks = new ArrayList<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
  
  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public List<TaskStat> getTasks() {
    return tasks;
  }

  public synchronized  void addTask(TaskStat task) {
    this.tasks.add(task);
  }

  public List<PhaseStat> getPhases() {
    return phases;
  }

  public synchronized  void addPhase(PhaseStat phase) {
    this.phases.add(phase);
  }
  
}
