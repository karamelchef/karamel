/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.dag.DagParams;
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

  /**
   * Recursive method to merge two json objects. Scales O(N^2) - only viable for
   * small json objects.
   *
   * @param origObj
   * @param paramObj
   */
  JsonObject merge(JsonObject origObj, JsonObject paramObj) {
    Set<Map.Entry<String, JsonElement>> original = origObj.entrySet();

    for (Map.Entry<String, JsonElement> entry : paramObj.entrySet()) {
      boolean exists = false;
      String pKey = entry.getKey();
      JsonElement pValue = entry.getValue();
      for (Map.Entry<String, JsonElement> o : original) {
        if (o.getKey().compareToIgnoreCase(pKey) == 0) {
          if (o.getValue().isJsonObject() && pValue.isJsonObject()) {
            merge(o.getValue().getAsJsonObject(), pValue.getAsJsonObject());
            exists = true;
          }
        }
      }
      if (exists == false) {
        origObj.add(pKey, pValue);
      }
    }
    return origObj;
  }

  @Override
  public List<ShellCommand> getCommands() throws IOException {

    Set<JsonElement> paramsToMerge = DagParams.getGlobalParams();
    if (paramsToMerge != null) {
      try {
        // Merge in Global return results into the json file.
        JsonElement obj = new JsonParser().parse(json);
        if (obj.isJsonObject()) {
          JsonObject jsonObj = obj.getAsJsonObject();

          for (JsonElement param : paramsToMerge) {
            if (param.isJsonObject()) {
              JsonObject paramObj = param.getAsJsonObject();
              merge(jsonObj, paramObj);
            }
          }
          json = new Gson().toJson(jsonObj);
        } else {
          Logger.getLogger(RunRecipeTask.class.getName()).warning(
              String.format("Invalid json object for chef-solo: \n %s'", json));
        }
      } catch (JsonIOException | JsonSyntaxException ex) {
        Logger.getLogger(RunRecipeTask.class.getName()).warning(
            String.format("Invalid return value as Json object: %s \n %s'", ex.toString(), json));
      }
    }

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

  public String getRecipeName() {
    return recipeCanonicalName.split(Settings.COOOKBOOK_DELIMITER)[1];
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
