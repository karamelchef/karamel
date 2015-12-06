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
public class PhaseStat {

  long id;
  String name;
  String status;
  long duration;

  public PhaseStat(String name, String status, long duration) {
    this.name = name;
    this.status = status;
    this.duration = duration;
  }

  public PhaseStat() {
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
