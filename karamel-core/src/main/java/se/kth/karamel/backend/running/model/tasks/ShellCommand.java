/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

/**
 *
 * @author kamal
 */
public class ShellCommand {

  public static enum Status {

    WAITING, QUEUED, ONGOING, DONE, FAILED;
  }
  Status status = Status.WAITING;
  Task task;
  String cmdStr;

  public ShellCommand(String cmdStr) {
    this.cmdStr = cmdStr;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getCmdStr() {
    return cmdStr;
  }

}
