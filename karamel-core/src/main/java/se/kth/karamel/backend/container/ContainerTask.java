package se.kth.karamel.backend.container;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shelan on 4/11/16.
 */
public class ContainerTask implements Runnable {

  private String task;
  private List<String> hostList = new ArrayList<>();


  public String getTask() {
    return task;
  }

  public void setTask(String task) {
    this.task = task;
  }

  public List<String> getHostList() {
    return hostList;
  }

  public void setHostList(List<String> hostList) {
    this.hostList = hostList;
  }

  @Override
  public void run() {

  }
}
