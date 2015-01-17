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
  String cmdStr;
  StringBuffer errorStream = new StringBuffer();

  public ShellCommand(String cmdStr) {
    this.cmdStr = cmdStr;
  }

  public void appendLog(String error) {
    errorStream.append(error);
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

  public String getErrorStream() {
    return errorStream.toString();
  }

}
