/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kamal
 */
public class ClusterStats {

  //Only for karamel-core usage
  boolean updated = true;
  long localId;
  String id;
  String userId;
  String definition;
  long startTime;
  long endTime;
  List<PhaseStat> phases = new ArrayList<>();
  List<TaskStat> tasks = new ArrayList<>();

  public ClusterStats() {
    localId = System.currentTimeMillis();
  }

  public ClusterStats(long localId) {
    this.localId = localId;
  }

  public boolean isUpdated() {
    return updated;
  }

  public synchronized void setUpdated(boolean updated) {
    this.updated = updated;
  }

  public long getLocalId() {
    return localId;
  }

  public void setLocalId(long localId) {
    this.localId = localId;
  }

  public String getId() {
    return id;
  }

  public synchronized void setId(String id) {
    this.id = id;
    updated = true;
  }

  public String getDefinition() {
    return definition;
  }

  public synchronized void setDefinition(String definition) {
    this.definition = definition;
    updated = true;
  }

  public synchronized void setUserId(String userId) {
    this.userId = userId;
    updated = true;
  }

  public String getUserId() {
    return userId;
  }

  public long getStartTime() {
    return startTime;
  }

  public synchronized void setStartTime(long startTime) {
    this.startTime = startTime;
    updated = true;
  }

  public long getEndTime() {
    return endTime;
  }

  public synchronized void setEndTime(long endTime) {
    this.endTime = endTime;
    updated = true;
  }

  public List<TaskStat> getTasks() {
    return tasks;
  }

  public synchronized void addTask(TaskStat task) {
    task.setId(tasks.size());
    this.tasks.add(task);
    setEndTime(System.currentTimeMillis());
    updated = true;
  }

  public List<PhaseStat> getPhases() {
    return phases;
  }

  public synchronized void addPhase(PhaseStat phase) {
    phase.setId(phases.size());
    this.phases.add(phase);
    setEndTime(System.currentTimeMillis());
    updated = true;
  }

  public synchronized String toJsonAndMarkNotUpdated() {
    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();
    Gson gson = builder.setPrettyPrinting().create();
    String json = gson.toJson(this);
    updated = false;
    return json;
  }

}
