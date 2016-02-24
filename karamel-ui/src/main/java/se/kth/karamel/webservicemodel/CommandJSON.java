package se.kth.karamel.webservicemodel;

import se.kth.karamel.backend.command.CommandResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by babbarshaer on 2015-02-09.
 */
public class CommandJSON {

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
  String command;
  String result;
  String nextCmd;
  String renderer;
  String errormsg;
  String successmsg;
  String context;
  List<CommandResponse.MenuItem> menuItems = new ArrayList();

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getNextCmd() {
    return nextCmd;
  }

  public void setNextCmd(String nextCmd) {
    this.nextCmd = nextCmd;
  }

  public String getRenderer() {
    return renderer;
  }

  public void setRenderer(String renderer) {
    this.renderer = renderer;
  }

  public String getErrormsg() {
    return errormsg;
  }

  public void setErrormsg(String errormsg) {
    this.errormsg = errormsg;
  }

  public String getSuccessmsg() {
    return successmsg;
  }

  public void setSuccessmsg(String successmsg) {
    this.successmsg = successmsg;
  }

  public List<CommandResponse.MenuItem> getMenuItems() {
    return menuItems;
  }

  public void setMenuItems(List<CommandResponse.MenuItem> menuItems) {
    this.menuItems = menuItems;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }
  
}
