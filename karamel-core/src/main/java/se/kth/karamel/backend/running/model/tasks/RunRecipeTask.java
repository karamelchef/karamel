/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import java.io.IOException;
import java.util.List;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class RunRecipeTask extends Task {

  private final String recipe;
  private String json;

  public RunRecipeTask(String machineId, String recipe, String json) {
    super("recipe " + recipe, machineId);
    this.recipe = recipe;
    this.json = json;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {
    if (commands == null) {
      String jsonFileName = recipe.replaceAll(Settings.COOOKBOOK_DELIMITER, "__");
      commands = ShellCommandBuilder.fileScript2Commands(Settings.SCRIPT_PATH_RUN_RECIPE, 
              "chef_json", json,
              "json_file_name", jsonFileName,
              "log_file_name", jsonFileName);
    }
    return commands;
  }

  public static String makeUniqueId(String machineId, String recipe) {
    return RunRecipeTask.class.getSimpleName() + recipe +  machineId;
  }

  @Override
  public String uniqueId() {
    return makeUniqueId(super.getMachineId(), recipe);
  }
}
