/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks.tablespoon;

import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author te27
 */
public class StopTablespoonTask extends TablespoonTask{
    
  private static final Logger logger = Logger.getLogger(StopTablespoonTask.class);
  
  public StopTablespoonTask(MachineRuntime machine, ClusterStats clusterStats, TaskSubmitter submitter) {
    super("start tablespoon", "start tablespoon", true, machine, clusterStats, submitter);
  }
  
  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_PATH_STOP_TABLESPOON_AGENT,
          "sudo_command", getSudoCommand(),
          "pid_file", Settings.PID_FILE_NAME,
          "task_id", getId(),
          "succeedtasks_filepath", Settings.SUCCEED_TASKLIST_FILENAME);
    }
    return commands;
  }
  
  public static String makeUniqueId(String machineId) {
    return "stop tablespoon on " + machineId;
  }
  
  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }
  
  
}
