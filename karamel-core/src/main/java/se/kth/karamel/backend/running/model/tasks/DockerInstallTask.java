package se.kth.karamel.backend.running.model.tasks;

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
public class DockerInstallTask extends Task {

  private String kvStoreIp;
  private String hostIp;

  public DockerInstallTask(NodeRunTime machine, ClusterStats clusterStats, TaskSubmitter submitter, String
    kvStoreIp) {
    super("docker install", "docker install", false, machine, clusterStats, submitter);
    this.kvStoreIp = kvStoreIp;
    this.hostIp = machine.getPrivateIp();
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_DOCKER_INSTALL,
        "kv_store_ip", this.kvStoreIp,
        "host_ip",this.hostIp,
        "pid_file", Settings.PID_FILE_NAME,
        "task_id", getId(),
        "succeedtasks_filepath", Settings.SUCCEED_TASKLIST_FILENAME,
        "ostype_filename", Settings.OSTYPE_FILE_NAME);
    }
    return commands;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }

  public static String makeUniqueId(String machineId) {
    return "install docker " + machineId;
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String findOsTypeTask = FindOsTypeTask.makeUniqueId(getMachineId());
    deps.add(findOsTypeTask);
    return deps;
  }
}
