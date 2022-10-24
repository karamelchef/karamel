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
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class MakeSoloRbTask extends Task {

  private final String vendorPath;

  public MakeSoloRbTask(MachineRuntime machine, String vendorPath, ClusterStats clusterStats,
      TaskSubmitter submitter) {
    super("make solo.rb", "make solo.rb", false, machine, clusterStats, submitter);
    this.vendorPath = vendorPath;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      String rubygemsUrlConf = "";
      String rubygemsUrl = conf.getProperty(Settings.CHEF_RUBYGEMS_URL);
      if (rubygemsUrl != null) {
        rubygemsUrlConf = String.format("rubygems_url \"%s\"", rubygemsUrl);
      }
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_MAKE_SOLO_RB,
          "install_dir_path", Settings.REMOTE_INSTALL_DIR_PATH(getSshUser()),
          "cookbooks_path", vendorPath,
          "sudo_command", getSudoCommand(),
          "pid_file", Settings.PID_FILE_NAME,
          "file_cache_path", conf.getProperty(Settings.CHEF_FILE_CACHE_PATH),
          "rubygems_url", rubygemsUrlConf);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId) {
    return "make solo.rb on " + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String id = InstallChefdkTask.makeUniqueId(getMachineId());
    deps.add(id);
    return deps;
  }
}
