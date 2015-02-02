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
public class VendorCookbookTask extends Task {

  private final String cookbookId;
  private final String cookbooksHome;
  private final String cookbookName;
  private final String cookbookUrl;
  private final String branch;

  public VendorCookbookTask(MachineEntity machine, String cookbookId, String cookbooksHome, String cookbookName, String cookbookUrl, String branch) {
    super("clone and vendor " + cookbookUrl, machine);
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
    return RunRecipeTask.class.getSimpleName() + cookbookId + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId(), cookbookId);
  }
}
