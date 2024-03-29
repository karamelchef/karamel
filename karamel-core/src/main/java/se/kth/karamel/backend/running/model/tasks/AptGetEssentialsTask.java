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
public class AptGetEssentialsTask extends Task {

  public AptGetEssentialsTask(MachineRuntime machine, ClusterStats clusterStats, TaskSubmitter submitter) {
    super("apt-get essentials", "apt-get essentials", true, machine, clusterStats, submitter);
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    OsType osType = getMachine().getOsType();
    String sudocommand = getSudoCommand();
    String osfamily = osType.family.toString().toLowerCase();
    if (commands == null) {
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_APTGET_ESSENTIALS,
          "sudo_command", sudocommand,
          "osfamily", osfamily,
          "task_id", getId(),
          "install_dir_path", Settings.REMOTE_INSTALL_DIR_PATH(getSshUser()),
          "succeedtasks_filepath", Settings.SUCCEED_TASKLIST_FILENAME,
          "pid_file", Settings.PID_FILE_NAME);
    }
    return commands;
  }

  @Override
  public boolean isSudoTerminalReqd() {
    OsType osType = getMachine().getOsType();
    return (osType != null && osType.family == OsType.LinuxFamily.REDHAT);
  }
  
  

  public static String makeUniqueId(String machineId) {
    return "apt-get essentials on " + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String findOsId = FindOsTypeTask.makeUniqueId(getMachineId());
    deps.add(findOsId);
    return deps;
  }

}
