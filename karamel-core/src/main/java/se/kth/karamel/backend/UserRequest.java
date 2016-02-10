/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

/**
 *
 * @author kamal
 */
public class UserRequest {

  public static enum Command {
    LAUNCH, PAUSE, RESUME, TERMINATE
  }

  private final Command command;
  private final String clusterName;

  public UserRequest(Command command, String clusterName) {
    this.command = command;
    this.clusterName = clusterName;
  }

  public Command getCommand() {
    return command;
  }

  public String getClusterName() {
    return clusterName;
  }
  
}
