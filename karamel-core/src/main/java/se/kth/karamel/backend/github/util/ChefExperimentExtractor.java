/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.github.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.Experiment;
import se.kth.karamel.backend.Experiment.Code;
import se.kth.karamel.backend.github.Github;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonCookbook;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.client.model.json.JsonRecipe;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.cookbook.metadata.KaramelFile;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlDependency;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlKaramelFile;

/**
 * How to use. Invoke methods in this order: (1) @see ChefExperimentExtractor#parseAttributesAddToGit() (2) @see
 * ChefExperimentExtractor#parseRecipesAddToGit()
 *
 * @author jdowling
 */
public class ChefExperimentExtractor {

  // <AttrName, AttrValue> pair added to attributes/default.rb 
  private static final Map<String, String> attrs = new HashMap<>();

  /**
   * Parses all scripts and config files and outputs to metadata.rb and attributes/default.rb the configuration values
   * found.
   *
   * @param owner org/user on github
   * @param repoName name of github repository
   * @param experiment input scripts/config filenames and content
   * @throws KaramelException
   */
  public static void parseAttributesAddToGit(String owner, String repoName, Experiment experiment)
      throws KaramelException {

    attrs.clear();
    // Add recipeNames to metadata.rb
    StringBuilder recipeDescriptions = new StringBuilder();
    Set<String> recipeNames = experiment.getExperiments().keySet();
    // Parse all the config variables and put them into attributes/default.rb
    Map<String, Code> experiments = experiment.getExperiments();

    for (String recipeName : recipeNames) {
      recipeDescriptions.append("recipe            " + repoName + "::" + recipeName + ", Experiment: " + recipeName).append(System.lineSeparator());
      Code exp = experiments.get(recipeName);
      String str = exp.getDefaultAttributes();
      Pattern p = Pattern.compile("%%(.*)%%\\s*=\\s*(.*)\\s*");
      Matcher m = p.matcher(str);
      while (m.find()) {
        String name = m.group(1);
        String value = m.group(2);
        attrs.put(name, value);
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
          "http_binaries", experiment.getUrlBinary()
      );

      for (String key : attrs.keySet()) {
        String entry = "default[:" + repoName + "][:" + key + "] = \"" + attrs.get(key) + "\"";
        defaults_rb.append(System.lineSeparator()).append(entry).append(System.lineSeparator());
      }

      StringBuilder config_props = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_CONFIG_PROPS,
          "name", repoName,
          "user", experiment.getUser(),
          "group", experiment.getGroup(),
          "http_binaries", experiment.getUrlBinary()
      );
      for (String key : attrs.keySet()) {
//        String entry = key + " = " + attrs.get(key);
        String entry = key + " = <%= node[:" + repoName + "][:" + key + "] %>";
        config_props.append(entry).append(System.lineSeparator());
      }

      // TODO: jim - resolve_ips
      StringBuilder metadata_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_METADATA,
          "resolve_ips", "",
          "results_dir", experiment.getResultsDir(),
          "build_command", experiment.getMavenCommand(),
          "ip_params", "",
          "%%more_recipes%%", recipeDescriptions.toString()
      );
      for (String recipe : recipeNames) {
        String entry = "recipe \"" + repoName + "::" + recipe + "\", \"Executes experiment as "
            + experiments.get(recipe).getScriptType() + " script.\"";
        metadata_rb.append(System.lineSeparator()).append(entry).append(System.lineSeparator());
      }
      for (String key : attrs.keySet()) {
        String entry = "attribute \"" + repoName + "/" + key + "\"," + System.lineSeparator()
            + ":description => \"" + key + " parameter value\"," + System.lineSeparator()
            + ":type => \"string\"";
        metadata_rb.append(System.lineSeparator()).append(entry).append(System.lineSeparator());
      }

      // 3. write them to files and push to github
      Github.addFile(owner, repoName, "attributes/default.rb", defaults_rb.toString());
      Github.addFile(owner, repoName, "metadata.rb", metadata_rb.toString());
      Github.addFile(owner, repoName, "templates/defaults/config.props.erb", config_props.toString());

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
  public static void parseRecipesAddToGit(String owner, String repoName, Experiment experimentContext)
      throws KaramelException {

    try {
      StringBuilder install_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_RECIPE_INSTALL,
          "name", repoName,
          "checksum", "",
          "setup_code", experimentContext.getExperimentSetupCode()
      );
      Github.addFile(owner, repoName, "recipes/install.rb", install_rb.toString());

      Map<String, Code> experiments = experimentContext.getExperiments();
      Set<String> recipeNames = experiments.keySet();

      StringBuilder kitchenContents = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_KITCHEN_YML,
          "name", repoName
      );
      Github.addFile(owner, repoName, ".kitchen.yml", kitchenContents.toString());

      String dependencies = experimentContext.getDependencies();
      if (!dependencies.isEmpty()) {
        String yamlEntryPrefix = "      -";
        dependencies = dependencies.replaceAll(",", System.lineSeparator() + yamlEntryPrefix);
        dependencies = yamlEntryPrefix + dependencies;
      }
      StringBuilder karamelContents = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_KARAMELFILE,
          "name", repoName,
          "dependencies", dependencies
      );
      KaramelFile karamelFile = new KaramelFile(karamelContents.toString());
      // Update Karamelfile with dependencies from the cluster definition
      String ymlString = experimentContext.getClusterDefinition();
      JsonCluster jsonCluster = ClusterDefinitionService.yamlToJsonObject(ymlString);
      List<String> clusterDependencies = new ArrayList<>();

      for (JsonGroup g : jsonCluster.getGroups()) {
        for (JsonCookbook cb : g.getCookbooks()) {
          for (JsonRecipe r : cb.getRecipes()) {
            clusterDependencies.add(r.getCanonicalName());
          }
        }
      }

      // 2. write them to recipes/default.rb and metadata.rb
      for (String recipe : recipeNames) {
        YamlDependency yd = new YamlDependency();
        yd.setGlobal(clusterDependencies);
        yd.setRecipe(repoName + Settings.COOOKBOOK_DELIMITER + recipe);
//        String email = (Github.getEmail() == null) ? "karamel@karamel.io" : Github.getEmail();
        Code experiment = experiments.get(recipe);
        StringBuilder recipe_rb = CookbookGenerator.instantiateFromTemplate(
            Settings.CB_TEMPLATE_RECIPE_EXPERIMENT,
            "name", recipe,
            "pre_chef_commands", experiment.getPreScriptChefCode(),
            "interpreter", experiment.getScriptType().toString(),
            //            "command", experiment.getScriptCommand(),
            //            "experiment_name", recipe,
            "user", experimentContext.getUser(),
            "group", experimentContext.getGroup(),
            "script_contents", experiment.getScriptContents()
        );
        
        StringBuilder experimentProvider = CookbookGenerator.instantiateFromTemplate(
            Settings.CB_TEMPLATE_PROVIDERS_START,
            "name", recipe,
            "interpreter", experiment.getScriptType().toString(),
            "user", experimentContext.getUser(),
            "group", experimentContext.getGroup(),
            "script_contents", experiment.getScriptContents()
        );
        
        StringBuilder experimentResources = CookbookGenerator.instantiateFromTemplate(
            Settings.CB_TEMPLATE_RESOURCES_START);
        // Replace all parameters with chef attribute values
        String recipeContents = recipe_rb.toString();
        for (String attr : attrs.keySet()) {
          recipeContents = recipeContents.replaceAll("%%" + attr + "%%", "#{node[:" + repoName + "][:" + attr + "]}");
        }
        
        // Replace all parameters with chef attribute values
        String experimentContents = experimentProvider.toString();
        for (String attr : attrs.keySet()) {
          experimentContents = experimentContents.replaceAll("%%" + attr + "%%", "#{node[:" + repoName + "][:" + attr + "]}");
        }

        // 3. write them to files and push to github
        Github.addFile(owner, repoName, "recipes" + File.separator + recipe + ".rb", recipeContents);
        Github.addFile(owner, repoName, "providers" + File.separator + recipe + ".rb", experimentContents);
        Github.addFile(owner, repoName, "resources" + File.separator + recipe + ".rb", experimentResources.toString());

        String defaultAttrsContents = experiment.getDefaultAttributes();
        if (defaultAttrsContents != null && defaultAttrsContents.isEmpty()) {
          Github.addFile(owner, repoName, "attributes" + File.separator + "defaults.rb",
              defaultAttrsContents);
        }

//        karamelFile.setDependency(repoName + Settings.COOOKBOOK_DELIMITER + recipe, yd);
        karamelFile.getDependencies().add(yd);
      }

      DumperOptions options = new DumperOptions();
      options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
      Representer r = new Representer();
      r.addClassTag(KaramelFile.class, Tag.MAP);
      Yaml karamelYml = new Yaml(new Constructor(YamlKaramelFile.class), r, options);
      String karamelFileContents = karamelYml.dump(karamelFile);
      Github.addFile(owner, repoName, "Karamelfile", karamelFileContents);

    } catch (IOException ex) {
      Logger.getLogger(ChefExperimentExtractor.class.getName()).log(Level.SEVERE, null, ex);
      throw new KaramelException(ex.getMessage());
    }

  }
}
