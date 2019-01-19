/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.io.Files;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.launcher.OsType;
import se.kth.karamel.backend.machines.MachineInterface;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class FindOsTypeTask extends Task {

  private static final Logger logger = Logger.getLogger(RunRecipeTask.class);

  public FindOsTypeTask(MachineRuntime machine, ClusterStats clusterStats, TaskSubmitter submitter) {
    super("find os-type", "find os-type", false, machine, clusterStats, submitter);
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_FIND_OSTYPE,
          "pid_file", Settings.PID_FILE_NAME,
          "task_id", getId(),
          "install_dir_path", Settings.REMOTE_INSTALL_DIR_PATH(getSshUser()),
          "succeedtasks_filepath", Settings.SUCCEED_TASKLIST_FILENAME,
          "ostype_filename", Settings.OSTYPE_FILE_NAME);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId) {
    return "find os-type on " + machineId;
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
  public void collectResults(MachineInterface sshMachine) throws KaramelException {
    String sshUser = getMachine().getSshUser();
    String clusterName = getMachine().getGroup().getCluster().getName();
    String publicIp = getMachine().getPublicIp();
    String remoteFile = Settings.REMOTE_OSTYPE_PATH(sshUser);
    String localResultsFile = Settings.MACHINE_OSTYPE_PATH(clusterName, publicIp);
    try {
      sshMachine.downloadRemoteFile(remoteFile, localResultsFile, true);
    } catch (IOException ex) {
      logger.debug(String.format("No return values for ostype in %s", publicIp));
      return;
    }
    try {
      String content = Files.toString(new File(localResultsFile), Charset.forName("UTF-8"));
      content = content.trim().toLowerCase();
      if (content.isEmpty()) {
        throw new KaramelException(String.format("The OS-Type file for %s is empty", publicIp));
      } 
      OsType osType = OsType.valuebyDestroString(content);
      getMachine().setOsType(osType);
    } catch (IOException ex) {
      String msg = String.format("Cannot find the results file for ostype in %s ", publicIp);
      throw new KaramelException(msg, ex);
    }

  }

}
