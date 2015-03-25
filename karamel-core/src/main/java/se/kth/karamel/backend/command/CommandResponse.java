/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.backend.command;

/**
 *
 * @author kamal
 */
public class CommandResponse {
  
  public static enum Renderer {
    INFO, YAML, SSH
  };
  
  private String result;
  private String nextCmd;
  private Renderer renderer = Renderer.INFO;

  public String getNextCmd() {
    return nextCmd;
  }

  public void setNextCmd(String nextCmd) {
    this.nextCmd = nextCmd;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public Renderer getRenderer() {
    return renderer;
  }

  public void setRenderer(Renderer renderer) {
    this.renderer = renderer;
  }
  
}
