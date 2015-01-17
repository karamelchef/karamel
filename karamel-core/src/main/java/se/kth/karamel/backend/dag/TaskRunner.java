/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.dag;

import org.apache.log4j.Logger;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class TaskRunner implements Runnable {

  private static final Logger logger = Logger.getLogger(TaskRunner.class);

  private final Task task;
  private final MachinesMonitor machinesMonitor;

  public TaskRunner(Task task, MachinesMonitor machinesMonitor) {
    this.task = task;
    this.machinesMonitor = machinesMonitor;
  }

  @Override
  public void run() {
    try {
      machinesMonitor.runTask(task);
    } catch (KaramelException ex) {
      logger.error("", ex);
    }
    while (task.getStatus().ordinal() <= Task.Status.ONGOING.ordinal()) {
      try {
        Thread.sleep(Settings.TASK_BUSYWAITING_INTERVALS);
      } catch (InterruptedException ex) {
        logger.error("Someone knocked on my door (-_-)zzz", ex);
      }
    }
  }

  public Task getTask() {
    return task;
  }

  @Override
  public String toString() {
    return task.toString();
  }
  
  

}
