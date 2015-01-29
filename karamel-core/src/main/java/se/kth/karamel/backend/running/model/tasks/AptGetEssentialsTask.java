/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.List;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.running.model.MachineEntity;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class AptGetEssentialsTask extends Task {

  public AptGetEssentialsTask(MachineEntity machine) {
    super("apt-get essentials", machine);
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.fileScript2Commands(Settings.SCRIPT_PATH_APTGET_ESSENTIALS);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId) {
    return AptGetEssentialsTask.class.getSimpleName() + machineId;
  }
  
  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }

}
