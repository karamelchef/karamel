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
public class RunRecipeTask extends Task {

    private final String recipeCanonicalName;
  private String json;
  private final String cookbookId;
  private final String cookbookName;

  public RunRecipeTask(MachineRuntime machine, String recipe, String json, TaskSubmitter submitter, String cookbookId, String cookbookName) {
    super("recipe " + recipe, machine, submitter);
    this.recipeCanonicalName = recipe;
    this.json = json;
    this.cookbookId = cookbookId;
    this.cookbookName = cookbookName;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      String jsonFileName = recipeCanonicalName.replaceAll(Settings.COOOKBOOK_DELIMITER, "__");
      commands = ShellCommandBuilder.fileScript2Commands(Settings.SCRIPT_PATH_RUN_RECIPE,
              "chef_json", json,
              "json_file_name", jsonFileName,
              "log_file_name", jsonFileName);
    }
    return commands;
  }

  public String getRecipeCanonicalName() {
    return recipeCanonicalName;
  }

  public String getCookbookId() {
    return cookbookId;
  }

  public String getCookbookName() {
    return cookbookName;
  }

  public static String installRecipeIdFromCookbookName(String machineId, String cookbook) {
    String installName = cookbook + Settings.COOOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
    return makeUniqueId(machineId, installName);
  }

  public static String installRecipeIdFromAnotherRecipeName(String machineId, String recipe) {
    String[] cmp = recipe.split(Settings.COOOKBOOK_DELIMITER);
    String installName = cmp[0] + Settings.COOOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
    return makeUniqueId(machineId, installName);
  }

  public static String makeUniqueId(String machineId, String recipe) {
    return recipe + " on " + machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId(), recipeCanonicalName);
  }

  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String installId = installRecipeIdFromAnotherRecipeName(getMachineId(), recipeCanonicalName);
    if (uniqueId().equals(installId)) {
      String id = VendorCookbookTask.makeUniqueId(getMachineId(), cookbookId);
      deps.add(id);
    } else {
      deps.add(installId);
    }
    return deps;
  }
}
