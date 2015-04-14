/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.List;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class MakeSoloRbTask extends Task {

  private final String vendorPath;

  public MakeSoloRbTask(MachineRuntime machine, String vendorPath) {
    super("make solo.rb", machine);
    this.vendorPath = vendorPath;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.fileScript2Commands(Settings.SCRIPT_PATH_MAKE_SOLO_RB, "cookbooks_path", vendorPath);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId) {
    return MakeSoloRbTask.class.getSimpleName() + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }
}
