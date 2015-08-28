/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.github.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.backend.Experiment;
import se.kth.karamel.backend.Experiment.Code;
import se.kth.karamel.backend.github.GithubApi;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 * How to use. Invoke methods in this order: (1) @see ChefExperimentExtractor#parseAttributesAddToGit() (2) @see
 * ChefExperimentExtractor#parseRecipesAddToGit()
 *
 */
public class ChefExperimentExtractor {

  private static final String YAML_DEPENDENCY_PREFIX = "      - ";
  private static final String YAML_RECIPE_PREFIX = "  - recipe: ";

  // <AttrName, AttrValue> pair added to attributes/default.rb 
  private static final SortedMap<String, String> attrs = new TreeMap<>();
  private static final Map<String, Map<String, String>> configFiles = new HashMap<>();

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
    configFiles.clear();

    StringBuilder recipeDescriptions = new StringBuilder();
    List<Code> experiments = experiment.getCode();

    // Extract all the configFileNames: write them to metadata.rb later
    // Extract all the from the configFile contents: write them to attributes/default.rb later
    // No conflict detection for duplicate key-value pairs yet. Should be done in Javascript in Browser.
    for (Code code : experiments) {
      String configFileName = code.getConfigFileName();
      Map<String, String> cfs = configFiles.get(configFileName);
      if (cfs == null) {
        cfs = new HashMap<>();
        configFiles.put(configFileName, cfs);
      }
      recipeDescriptions.append("recipe            \"").append(repoName).append(Settings.COOKBOOK_DELIMITER).
          append(code.getName()).append("\",  \"configFile=").append(configFileName)
          .append("; Experiment name: ").append(code.getName()).append("\"").append(System.lineSeparator());
      String str = code.getConfigFileContents();
      Pattern p = Pattern.compile("\\s*(.*)\\s*=\\s*(.*)\\s*");
      Matcher m = p.matcher(str);
      while (m.find()) {
        String name = m.group(1);
        String value = m.group(2);
        if (!name.isEmpty()) {
          cfs.put(name, value);
//          attrs.put(name, value);
        }
      }
    }

    String email = (GithubApi.getEmail() == null) ? "karamel@karamel.io" : GithubApi.getEmail();
    try {
      StringBuilder defaults_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_ATTRIBUTES_DEFAULT
      //          , "name", repoName,
      //          "user", experiment.getUser(),
      //          "group", experiment.getGroup(),
      //          "http_binaries", experiment.getUrlBinary()
      );

      String str = experiment.getDefaultAttributes();
      Pattern p = Pattern.compile("\\s*(.*)\\s*=\\s*(.*)\\s*");
      Matcher m = p.matcher(str);
      while (m.find()) {
        String name = m.group(1);
        String value = m.group(2);
        if (!name.isEmpty()) {
          attrs.put(name, value);
        }
      }

      // Add all key-value pairs from the config files to the default attributes
      for (String key : attrs.keySet()) {
        String entry = "default[:" + repoName + "][:" + key + "] = \"" + attrs.get(key) + "\"";
        defaults_rb.append(entry).append(System.lineSeparator());
      }

      StringBuilder metadata_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_METADATA,
          "name", repoName,
          "user", experiment.getUser(),
          "email", email,
          "depends", "",
          "resolve_ips", "",
          "build_command", experiment.getMavenCommand(),
          "url_binary", experiment.getUrlBinary(),
          "url_gitclone", experiment.getUrlGitClone(),
          "build_command", experiment.getMavenCommand(),
          "ip_params", "",
          "more_recipes", recipeDescriptions.toString()
      );

      for (String key : attrs.keySet()) {
        String entry = "attribute \"" + repoName + "/" + key + "\"," + System.lineSeparator()
            + ":description => \"" + key + " parameter value\"," + System.lineSeparator()
            + ":type => \"string\"";
        metadata_rb.append(entry).append(System.lineSeparator()).append(System.lineSeparator());
      }

      // 3. write them to files and push to github
      GithubApi.addFile(owner, repoName, "attributes/default.rb", defaults_rb.toString());
      GithubApi.addFile(owner, repoName, "metadata.rb", metadata_rb.toString());

    } catch (IOException ex) {
      throw new KaramelException("Problem parsing attributes from GitHub: " + ex.getMessage());
    }
  }

  /**
   * Parses the user-defined script files and for each script, a recipe file is generated and added to the git repo.
   *
   * @param owner
   * @param repoName
   * @param experimentContext
   * @throws se.kth.karamel.common.exception.KaramelException
   * @throws KaramelExceptionntents.toString()); // Update Karamel
   */
  public static void parseRecipesAddToGit(String owner, String repoName, Experiment experimentContext)
      throws KaramelException {

    try {

      StringBuilder kitchenContents = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_KITCHEN_YML,
          "name", repoName
      );
      GithubApi.addFile(owner, repoName, ".kitchen.yml", kitchenContents.toString());

      List<Code> experiments = experimentContext.getCode();

      String localDependencies = repoName + Settings.COOKBOOK_DELIMITER + "install" + System.lineSeparator()
          + experimentContext.getLocalDependencies();
      String[] lDeps = localDependencies.split(System.lineSeparator());

      Set<String> lDepsRemoveDuplicates = new HashSet<>();
      StringBuilder lDepsFinal = new StringBuilder();
      for (String s : lDeps) {
        s = s.trim();
        if (!s.isEmpty()) {
          lDepsRemoveDuplicates.add(s);
        }
      }
      for (String s : lDepsRemoveDuplicates) {
        lDepsFinal.append(YAML_DEPENDENCY_PREFIX).append(s).append(System.lineSeparator());
      }

      String globalDependencies = experimentContext.getGlobalDependencies();
      String[] gDeps = globalDependencies.split(System.lineSeparator());
      Set<String> gDepsRemoveDuplicates = new HashSet<>();
      StringBuilder gDepsFinal = new StringBuilder();
      for (String s : gDeps) {
        s = s.trim();
        if (!s.isEmpty()) {
          gDepsRemoveDuplicates.add(s);
        }
      }
      for (String s : gDepsRemoveDuplicates) {
        gDepsFinal.append(YAML_DEPENDENCY_PREFIX).append(s).append(System.lineSeparator());
      }

      StringBuilder recipeDepsKaramelfile = new StringBuilder();
      for (Code experiment : experiments) {
        String recipeName = experiment.getName();
        recipeDepsKaramelfile.append(YAML_RECIPE_PREFIX).append(repoName).append(Settings.COOKBOOK_DELIMITER).append(
            recipeName)
            .append(System.lineSeparator());
        recipeDepsKaramelfile.append("    local:").append(System.lineSeparator());
        recipeDepsKaramelfile.append(lDepsFinal.toString());
        recipeDepsKaramelfile.append("    global:").append(System.lineSeparator());
        recipeDepsKaramelfile.append(gDepsFinal.toString());
      }

      StringBuilder karamelContents = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_KARAMELFILE,
          "name", repoName,
          "next_recipes", recipeDepsKaramelfile.toString()
      );
      // TODO - integration with cluster defn file
//      String ymlString = experimentContext.getClusterDefinition();

      String berksfile = experimentContext.getBerksfile();

      StringBuilder berksContents = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_BERKSFILE,
          "berks_dependencies", berksfile
      );

      GithubApi.addFile(owner, repoName, "Berksfile", berksContents.toString());

      Map<String, String> expConfigFileNames = new HashMap<>();
      Map<String, String> expConfigFilePaths = new HashMap<>();

      // 2. write them to recipes/default.rb and metadata.rb
      for (Code experiment : experiments) {
        String experimentName = experiment.getName();
        String configFilePath = experiment.getConfigFileName();
        String configFileContents = experiment.getConfigFileContents();

        String configFileName = configFilePath;
        int filePos = configFileName.lastIndexOf("/");
        if (filePos != -1) {
          configFileName = configFileName.substring(filePos + 1);
        }

        String email = (GithubApi.getEmail() == null) ? "karamel@karamel.io" : GithubApi.getEmail();

        String username = attrs.containsKey("user") ? attrs.get("user") : experimentContext.getUser();
        String groupname = attrs.containsKey("group") ? attrs.get("group") : experimentContext.getUser();

        StringBuilder recipe_rb = CookbookGenerator.instantiateFromTemplate(
            Settings.CB_TEMPLATE_RECIPE_EXPERIMENT,
            "cookbook", repoName,
            "name", experimentName,
            "interpreter", experiment.getScriptType(),
            "user", username,
            "group", groupname,
            "script_contents", experiment.getScriptContents()
        );

        String recipeContents = recipe_rb.toString();

        // Replace all parameters with chef attribute values
        for (String attr : attrs.keySet()) {
          recipeContents = recipeContents.replaceAll("%%" + attr + "%%", "#{node[:" + repoName + "][:" + attr + "]}");
        }

        for (String attr : attrs.keySet()) {
          configFileContents = configFileContents.replaceAll("%%" + attr + "%%",
              "<%= node[:" + repoName + "][:" + attr + "] =>");
        }

        if (!configFilePath.isEmpty()) {
          expConfigFileNames.put(experimentName, configFileName);
          expConfigFilePaths.put(experimentName, configFilePath);
        }

        // 3. write them to files and push to github
        GithubApi.addFile(owner, repoName, "recipes" + File.separator + experimentName + ".rb", recipeContents);
        if (!configFileName.isEmpty()) {
          GithubApi.addFile(owner, repoName,
              "templates" + File.separator + "defaults" + File.separator + configFileName + ".erb", configFileContents);
        }

      }

      StringBuilder configFilesTemplateDefns = new StringBuilder();
      for (String expName : expConfigFileNames.keySet()) {
        String configFilePath = expConfigFileNames.get(expName);
        String configFileName = expConfigFileNames.get(expName);
        StringBuilder configProps = CookbookGenerator.instantiateFromTemplate(
            Settings.CB_TEMPLATE_CONFIG_PROPS,
            "name", expName,
            "configFileName", configFileName,
            "configFilePath", configFilePath,
            "ip_params", ""
        );
        configFilesTemplateDefns.append(configProps).append(System.lineSeparator());
      }

      StringBuilder install_rb = CookbookGenerator.instantiateFromTemplate(
          Settings.CB_TEMPLATE_RECIPE_INSTALL,
          "name", repoName,
          "cookbook", repoName,
          "checksum", "",
          "resolve_ips", "",
          "setup_code", experimentContext.getExperimentSetupCode(),
          "config_files", configFilesTemplateDefns.toString()
      );

      GithubApi.addFile(owner, repoName, "recipes/install.rb", install_rb.toString());
      GithubApi.addFile(owner, repoName, "Karamelfile", karamelContents.toString());

    } catch (IOException ex) {
      throw new KaramelException("Problem parsing recipes from GitHub: " + ex.getMessage());
    }

  }
}
