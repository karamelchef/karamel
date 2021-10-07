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
import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.launcher.OsType;
import se.kth.karamel.backend.machines.MachineInterface;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.IoUtils;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class CheckStickybitTmpTask extends Task {

  private static final Logger logger = Logger.getLogger(RunRecipeTask.class);

  public CheckStickybitTmpTask(MachineRuntime machine, ClusterStats clusterStats, TaskSubmitter submitter) {
    super("check sticybit", "check sticybit", false, machine, clusterStats, submitter);
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPT_CHECK_STICKYBIT_TMP);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId) {
    return "check stickybit /tmp on " + machineId;
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
      String content = IoUtils.readContentFromPath(localResultsFile);
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
    return;
  }

}
