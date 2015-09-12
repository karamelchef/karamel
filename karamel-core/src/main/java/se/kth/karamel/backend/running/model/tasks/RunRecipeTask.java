/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ShellCommandBuilder;
import se.kth.karamel.backend.dag.DagParams;
import se.kth.karamel.backend.machines.MachineInterface;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.stats.ClusterStats;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class RunRecipeTask extends Task {

  private static final Logger logger = Logger.getLogger(RunRecipeTask.class);
  private final String recipeCanonicalName;
  private String json;
  private final String cookbookId;
  private final String cookbookName;

  public RunRecipeTask(MachineRuntime machine, ClusterStats clusterStats, String recipe, String json, 
      TaskSubmitter submitter, String cookbookId, String cookbookName) {
    super("recipe " + recipe, machine, clusterStats, submitter);
    this.recipeCanonicalName = recipe;
    this.json = json;
    this.cookbookId = cookbookId;
    this.cookbookName = cookbookName;
  }

  /**
   * Recursive method to merge two json objects. Scales O(N^2) - only viable for small json objects.
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
          GsonBuilder builder = new GsonBuilder();
          builder.disableHtmlEscaping();
          Gson gson = builder.setPrettyPrinting().create();
          json = gson.toJson(jsonObj);
        } else {
          logger.warn(String.format("Invalid json object for chef-solo: \n %s'", json));
        }
      } catch (JsonIOException | JsonSyntaxException ex) {
        logger.warn(String.format("Invalid return value as Json object: %s \n %s'", ex.toString(), json));
      }
    }

    if (commands == null) {
      String jsonFileName = recipeCanonicalName.replaceAll(Settings.COOKBOOK_DELIMITER, "__");
      commands = ShellCommandBuilder.fileScript2Commands(Settings.SCRIPT_PATH_RUN_RECIPE,
          "chef_json", json,
          "json_file_name", jsonFileName,
          "log_file_name", jsonFileName,
          "sudo_command", getSudoCommand());
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
    return recipeCanonicalName.split(Settings.COOKBOOK_DELIMITER)[1];
  }

  public String getCookbookName() {
    return cookbookName;
  }

  public static String installRecipeIdFromCookbookName(String machineId, String cookbook) {
    String installName = cookbook + Settings.COOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
    return makeUniqueId(machineId, installName);
  }

  public static String installRecipeIdFromAnotherRecipeName(String machineId, String recipe) {
    String[] cmp = recipe.split(Settings.COOKBOOK_DELIMITER);
    String installName = cmp[0] + Settings.COOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
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

  /**
   * It then parses the JSON object and updates a central location for Chef Attributes.
   *
   * @param sshMachine
   * @throws se.kth.karamel.common.exception.KaramelException
   */
  @Override
  public void collectResults(MachineInterface sshMachine) throws KaramelException {
    String remoteFile = Settings.RECIPE_RESULT_REMOTE_PATH(getRecipeCanonicalName());
    String localResultsFile = Settings.RECIPE_RESULT_LOCAL_PATH(getRecipeCanonicalName(),
        getMachine().getGroup().getCluster().getName(), getMachine().getPublicIp());
    try {
      sshMachine.downloadRemoteFile(remoteFile, localResultsFile, true);
    } catch (IOException ex) {
      logger.debug(String.format("No return values for %s on %s", getRecipeCanonicalName(),
          getMachine().getPublicIp()));
      return;
    }
    JsonReader reader;
    try {
      reader = new JsonReader(new FileReader(localResultsFile));
    } catch (FileNotFoundException ex) {
      String msg = String.format("Cannot find the results file for %s on %s", getRecipeCanonicalName(),
          getMachine().getPublicIp());
      throw new KaramelException(msg, ex);
    }
    JsonParser jsonParser = new JsonParser();
    try {
      JsonElement el = jsonParser.parse(reader);
      DagParams.setGlobalParams(el);
    } catch (JsonIOException | JsonSyntaxException ex) {
      throw new KaramelException(String.format("Invalid return value as Json object for %s on %s",
          getRecipeCanonicalName(), getMachine().getPublicIp()), ex);
    }
  }

  @Override
  public void downloadExperimentResults(MachineInterface sshMachine) throws KaramelException {
    String remoteFile = Settings.EXPERIMENT_RESULT_REMOTE_PATH(getRecipeCanonicalName()) + ".out";
    String localResultsFile = Settings.EXPERIMENT_RESULT_LOCAL_PATH(getRecipeCanonicalName(),
        getMachine().getGroup().getCluster().getName(), getMachine().getPublicIp());
    try {
      sshMachine.downloadRemoteFile(remoteFile, localResultsFile, true);
    } catch (IOException ex) {
      logger.info(String.format("Cannot find experiment results for download for %s on %s", getRecipeCanonicalName(),
          getMachine().getPublicIp()));
      return;
    }
  }
}
