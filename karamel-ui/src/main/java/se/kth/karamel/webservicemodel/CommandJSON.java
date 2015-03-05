package se.kth.karamel.webservicemodel;

/**
 * Created by babbarshaer on 2015-02-09.
 */
public class CommandJSON {

  String command;
  String result;
  String nextCmd;
  String renderer;

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

}
