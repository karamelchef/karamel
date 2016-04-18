package se.kth.karamel.backend.container;

import org.apache.log4j.Logger;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.NodeRunTime;
import se.kth.karamel.backend.running.model.tasks.DockerInstallTask;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;

import java.util.ArrayList;

/**
 * Created by shelan on 3/16/16.
 */
public class ContainerLauncher {

  private static final Logger logger = Logger.getLogger(ContainerLauncher.class);
  private ClusterRuntime clusterRuntime;
  private ArrayList<NodeRunTime> hostMachines = new ArrayList<>();
  private ArrayList<Task> taskList = new ArrayList<>();
  private ClusterStats clusterStats;
  private TaskSubmitter taskSubmitter;
  private boolean taskFailed;

  public ContainerLauncher(ClusterRuntime clusterRuntime, ClusterStats clusterStats, TaskSubmitter tasksubmitter) {
    this.clusterRuntime = clusterRuntime;
    this.extractHostMachines(clusterRuntime);
    this.clusterStats = clusterStats;
    this.taskSubmitter = tasksubmitter;
  }

  public void configureHosts() throws InterruptedException, KaramelException {
    if (hostMachines.isEmpty()) {
      logger.error("Should have atleast one host. There is no hosts to start docker");
    }

    for (NodeRunTime hostMachine : hostMachines) {
      Task dockerInstallationTask = new DockerInstallTask(hostMachine, clusterStats, taskSubmitter,"kvip");
      hostMachine.addTask(dockerInstallationTask);
      taskSubmitter.submitTask(dockerInstallationTask);
      taskList.add(dockerInstallationTask);
    }
    logger.info("Startig configuring docker hosts");
    while (!tasksCompleted()) {
      Thread.sleep(1000);
    }
  }

  private void extractHostMachines(ClusterRuntime clusterRuntime) {
    for (GroupRuntime groupRuntime : clusterRuntime.getGroups()) {
      hostMachines.addAll(groupRuntime.getMachines());
    }
  }

  private boolean tasksCompleted() {
    boolean done = true;
    for (Task task : taskList) {
      if (!task.getStatus().equals(Task.Status.DONE)) {
        done = false;
      }
      // if one of the tasks is faile we need to fail this.
      if (task.getStatus().equals(Task.Status.FAILED)) {
        this.taskFailed = false;
        break;
      }
    }
    return done;
  }
}
