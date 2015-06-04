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
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class VendorCookbookTask extends Task {

  private final String cookbookId;
  private final String cookbooksHome;
  private final String cookbookName;
  private final String cookbookUrl;
  private final String branch;

  public VendorCookbookTask(MachineRuntime machine, TaskSubmitter submitter, String cookbookId, String cookbooksHome, 
      String cookbookName, String cookbookUrl, String branch) {
    super("clone and vendor " + cookbookUrl, machine, submitter);
    this.cookbookId = cookbookId;
    this.cookbooksHome = cookbooksHome;
    this.cookbookName = cookbookName;
    this.cookbookUrl = cookbookUrl;
    this.branch = branch;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      commands = ShellCommandBuilder.fileScript2Commands(Settings.SCRIPT_PATH_CLONE_VENDOR_COOKBOOK,
              "cookbooks_home", cookbooksHome,
              "cookbook_name", cookbookName,
              "cookbook_url", cookbookUrl,
              "branch_name", branch,
              "vendor_subfolder", Settings.COOKBOOKS_VENDOR_SUBFOLDER);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId, String cookbookId) {
    return "clone and vendor " + cookbookId + " on " + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId(), cookbookId);
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String id = MakeSoloRbTask.makeUniqueId(getMachineId());
    deps.add(id);
    return deps;
  }
}
