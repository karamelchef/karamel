/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.launcher.OsType;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class InstallBerkshelfTask extends Task {

  public InstallBerkshelfTask(MachineRuntime machine, ClusterStats clusterStats, TaskSubmitter submitter) {
    super("install berkshelf", "install berkshelf", true, machine, clusterStats, submitter);
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    OsType osType = getMachine().getOsType();
    if (commands == null) {
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_INSTALL_RUBY_CHEF_BERKSHELF,
          "sudo_command", getSudoCommand(),
          "task_id", getId(),
          "osfamily", osType.family.toString(),
          "succeedtasks_filepath", Settings.SUCCEED_TASKLIST_FILENAME,
          "pid_file", Settings.PID_FILE_NAME);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId) {
    return "install berkshelf on " + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String aptget = AptGetEssentialsTask.makeUniqueId(getMachineId());
    String findos = FindOsTypeTask.makeUniqueId(getMachineId());
    deps.add(aptget);
    deps.add(findos);
    return deps;
  }

}
