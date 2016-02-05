/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class KillSessionTask extends Task {

  public KillSessionTask() {
    super("kill session", "kill session", false, null, null, null);
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.fileScript2LinebyLineCommands(Settings.SCRIPT_PATH_KILL_RUNNING_SESSION,
          "sudo_command", getSudoCommand(),
          "pid_file", Settings.PID_FILE_NAME);
    }
    return commands;
  }

  @Override
  public String uniqueId() {
    return null;
  }

  @Override
  public Set<String> dagDependencies() {
    return null;
  }

}
