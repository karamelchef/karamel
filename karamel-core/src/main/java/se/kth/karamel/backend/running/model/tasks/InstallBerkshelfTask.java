/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.List;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class InstallBerkshelfTask extends Task {

  public InstallBerkshelfTask(String machineId) {
    super("install berkshelf", machineId);
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
    return InstallBerkshelfTask.class.getSimpleName() + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }
}
