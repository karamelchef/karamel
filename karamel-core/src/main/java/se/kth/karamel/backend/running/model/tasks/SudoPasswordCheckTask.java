/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.dag.DagParams;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.Settings;

public class SudoPasswordCheckTask extends Task {

  public SudoPasswordCheckTask(MachineRuntime machine, TaskSubmitter submitter) {
    super("test sudo password", machine, submitter);
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.fileScript2Commands(Settings.SCRIPT_PATH_SUDO_PASSWORD_CHECK);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId) {
    return  "test sudo password on "+ machineId;
  }
  
  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }

  @Override
  public Set<String> dagDependencies() {
    return Collections.EMPTY_SET;
  }

  @Override
  public void failed(String msg) {
    super.failed(msg); 
    DagParams.setSudoPasswordRequired();
  }

    
}
