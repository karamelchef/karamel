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
import se.kth.karamel.backend.machines.MachineInterface;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class InstallBerkshelfTask extends Task {

  public InstallBerkshelfTask(MachineRuntime machine, TaskSubmitter submitter) {
    super("install berkshelf", machine, submitter);
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.makeSingleFileCommands(
              Settings.SCRIPT_NAME_INSTALL_RUBY_CHEF_BERKSHELF, 
              Settings.SCRIPT_PATH_INSTALL_RUBY_CHEF_BERKSHELF);
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
    String id = AptGetEssentialsTask.makeUniqueId(getMachineId());
    deps.add(id);
    return deps;
  }
  
  
}
