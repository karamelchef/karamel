/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.github.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.backend.ExperimentContext;
import se.kth.karamel.backend.ExperimentContext.Experiment;
import se.kth.karamel.backend.github.Github;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author jdowling
 */
public class ChefExperimentExtractor {

  /**
   * Parses all scripts and config files and outputs to metadata.rb and attributes/default.rb the configuration values
   * found.
   *
   * @param owner org/user on github
   * @param repoName name of github repository
   * @param experiment input scripts/config filenames and content
   * @throws KaramelException
   */
  public static void parseAttributesAddToGit(String owner, String repoName, ExperimentContext experiment)
      throws KaramelException {

    // <AttrName, AttrValue> pair added to attributes/default.rb 
    Map<String, String> defaultAttrs = new HashMap<>();
    // <AttrName, AttrValue> pair added to metadata.rb 
    Map<String, String> metadataAttrs = new HashMap<>();
    // List of recipes added to metadata.rb
    Set<String> recipeNames = new HashSet<>();

    // Add recipeNames to metadata.rb
    recipeNames = experiment.getExperiments().keySet();
    // Parse all the config variables and put them into attributes/default.rb
    Map<String, Experiment> experiments = experiment.getExperiments();

    for (String recipeName : recipeNames) {
      Experiment exp = experiments.get(recipeName);
      String str = exp.getConfigFileContents();
      Pattern p = Pattern.compile("%%(.*)%%\\s*=\\s*(.*)\\s*");
      Matcher m = p.matcher(str);
      while (m.find()) {
        String matched = m.group();
        String name = m.group(1);
        String value = m.group(2);
        defaultAttrs.put(name, value);
        metadataAttrs.put(name, value);
      }
    }

    for (String recipeName : recipeNames) {
      Experiment exp = experiments.get(recipeName);
      String str = exp.getScriptContents();
      Pattern p = Pattern.compile("%%[--]*[-D]*(.*)%%\\s*=\\s*(.*)[\\s]+");
      Matcher m = p.matcher(str);
      while (m.find()) {
        String name = m.group(1);
        String value = m.group(2);
        defaultAttrs.put(name, value);
        metadataAttrs.put(name, value);
      }
    }

    // 2. write them to attributes/default.rb and metadata.rb
    String email = (Github.getEmail() == null) ? "karamel@karamel.io" : Github.getEmail();
    try {
      StringBuilder defaults_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_ATTRIBUTES_DEFAULT,
          "name", repoName,
          "user", experiment.getUser(),
          "group", experiment.getGroup(),
          "http_binaries", experiment.getUrl()
      );

      for (String key : defaultAttrs.keySet()) {
        String entry = "default[:" + repoName + "][:" + key + "] = \"" + defaultAttrs.get(key) + "\"";
        defaults_rb.append(System.lineSeparator()).append(entry).append(System.lineSeparator());
      }

      StringBuilder metadata_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_METADATA,
          "name", repoName,
          "user", owner,
          "email", email
      );
      for (String recipe : recipeNames) {
        String entry = "recipe \"" + repoName + "::" + recipe + "\", \"Executes experiment as "
            + experiments.get(recipe).getScriptType() + " script.\"";
        metadata_rb.append(System.lineSeparator()).append(entry).append(System.lineSeparator());
      }
      for (String key : metadataAttrs.keySet()) {
        String entry = "attribute \"" + repoName + "/" + key + "\"," + System.lineSeparator()
            + ":description => \"" + key + " parameter value\"," + System.lineSeparator()
            + ":type => \"string\"";
        metadata_rb.append(System.lineSeparator()).append(entry).append(System.lineSeparator());
      }

      // 3. write them to files and push to github
      Github.addFile(owner, repoName, "attributes/default.rb", defaults_rb.toString());
      Github.addFile(owner, repoName, "metadata.rb", metadata_rb.toString());

    } catch (IOException ex) {
      Logger.getLogger(ChefExperimentExtractor.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    }
  }

  /**
   * Parses the user-defined script files and for each script, a recipe file is generated and added to the git repo.
   *
   * @param owner
   * @param repoName
   * @param experimentContext
   * @throws KaramelException
   */
  public static void parseRecipesAddToGit(String owner, String repoName, ExperimentContext experimentContext)
      throws KaramelException {

    try {
      StringBuilder install_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_RECIPE_INSTALL,
          "name", repoName
      );
      Github.addFile(owner, repoName, "recipes/install.rb", install_rb.toString());

      Map<String, Experiment> experiments = experimentContext.getExperiments();
      Set<String> recipeNames = experiments.keySet();

      // 2. write them to recipes/default.rb and metadata.rb
      for (String recipe : recipeNames) {
        String email = (Github.getEmail() == null) ? "karamel@karamel.io" : Github.getEmail();
        Experiment experiment = experiments.get(recipe);
        StringBuilder recipe_rb = CookbookGenerator.instantiateFromTemplate(
            Settings.CB_TEMPLATE_RECIPE_EXPERIMENT,
            "name", recipe,
            "pre_chef_commands", experiment.getPreScriptChefCode(),
            "script_type", experiment.getScriptCommand(),
            "experiment_name", recipe,
            "user", experimentContext.getUser(),
            "group", experimentContext.getGroup(),
            "script_contents", experiment.getScriptContents()
        );

        // 3. write them to files and push to github
        Github.addFile(owner, repoName, "recipes/" + recipe + ".rb", recipe_rb.toString());
      }
    } catch (IOException ex) {
      Logger.getLogger(ChefExperimentExtractor.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    }

  }
}
