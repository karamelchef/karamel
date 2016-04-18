package se.kth.karamel.backend.running.model.tasks;

import se.kth.karamel.backend.container.ContainerTask;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.NodeRunTime;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shelan on 3/22/16.
 */
public class DockerTask extends Task {

  private static ContainerTask containerTask;

  public DockerTask(NodeRunTime machine, ClusterStats clusterStats, TaskSubmitter submitter, ContainerTask
    containerTask) {
    super("docker task " + containerTask.getTask(), "docker task " + containerTask.getTask(), false, machine,
      clusterStats, submitter);
    this.containerTask = containerTask;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_DOCKER_TASK,
        "docker_command", this.containerTask.getTask(),
        "pid_file", Settings.PID_FILE_NAME,
        "task_id", getId(),
        "succeedtasks_filepath", Settings.SUCCEED_TASKLIST_FILENAME);
    }
    return commands;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }

  public static String makeUniqueId(String machineId) {
    return "docker task " + containerTask.getTask() + machineId;
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    return deps;
  }
}
