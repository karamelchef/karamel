/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.command;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kamal
 */
public class CommandResponse {

  public class MenuItem {

    private String label;
    private String command;

    public MenuItem(String label, String command) {
      this.label = label;
      this.command = command;
    }

    public String getCommand() {
      return command;
    }

    public String getLabel() {
      return label;
    }

  }

  public static enum Renderer {

    INFO, YAML, SSH
  };

  private String result;
  private String nextCmd;
  private Renderer renderer = Renderer.INFO;
  private List<MenuItem> menuItems = new ArrayList();
  private String successMessage;

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

  public List<MenuItem> getMenuItems() {
    return menuItems;
  }

  public void addMenuItem(String label, String command) {
    MenuItem menuItem = new MenuItem(label, command);
    menuItems.add(menuItem);
  }

  public String getSuccessMessage() {
    return successMessage;
  }

  public void setSuccessMessage(String successMessage) {
    this.successMessage = successMessage;
  }

}
