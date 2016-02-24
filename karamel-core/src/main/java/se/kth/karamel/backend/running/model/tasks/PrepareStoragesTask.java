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
import se.kth.karamel.common.launcher.amazon.StorageDevice;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class PrepareStoragesTask extends Task {

  private final StorageDevice[] storageDevices;

  public PrepareStoragesTask(MachineRuntime machine, ClusterStats clusterStats, TaskSubmitter submitter,
      StorageDevice[] storageDevices) {
    super("prepare storages", "prepare storages", false, machine, clusterStats, submitter);
    this.storageDevices = storageDevices;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      StringBuilder tuple = new StringBuilder();
      for (StorageDevice device : storageDevices) {
        tuple.append(String.format("'%s','%s' ", device.kernelAlias(), device.mountPoint()));
      }
      commands = ShellCommandBuilder.makeSingleFileCommand(Settings.SCRIPET_PATH_PREPARE_STORAGE,
          "sudo_command", getSudoCommand(),
          "task_id", getId(),
          "succeedtasks_filepath", Settings.SUCCEED_TASKLIST_FILENAME,
          "device_mountpoint_tuple", tuple.toString(),
          "pid_file", Settings.PID_FILE_NAME
      );
    }
    return commands;
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String findOsId = FindOsTypeTask.makeUniqueId(getMachineId());
    deps.add(findOsId);
    return deps;
  }

  public static String makeUniqueId(String machineId) {
    return "prepare storages on " + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId());
  }

}
