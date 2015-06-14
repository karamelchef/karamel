package se.kth.karamel.backend;

import java.util.HashMap;
import java.util.Map;

public class ExperimentContext {

  public static enum Script {bash, csh, perl, python, ruby, execute};
  
  /**
   * Type of script to execute.
   */
  private Script script;
  
  /**
   * Url for the experiment code. Can have a .jar or .tar.gz extention.
   */
  private String url;

  /**
   * username to run program as
   */
  private String user = "karamel";

  /**
   * groupname to run program as
   */
  private String group = "karamel";

  /**
   * Description of the experiment cookbook.
   */
  private String description = "Karamel experiment repository description placeholder";

  /**
   * relative path of directory for results to download
   */
  private String resultsDirectory = "results";

  /**
   * Experiment name - recipe name in Chef
   */
  private String recipeName = "default";

  /**
   *
   */
  /**
   * Bash script pair: <relativePath,contents>
   */
  private final Map<String, String> scripts = new HashMap<>();
  /**
   * Config file pair: <relativePath,contents>
   */
  private final Map<String, String> configFiles = new HashMap<>();

  public ExperimentContext() {
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void addScript(String filename, String contents) {
    scripts.put(filename, contents);
  }

  public void addConfigFile(String filename, String contents) {
    configFiles.put(filename, contents);
  }

  public Map<String, String> getScripts() {
    return scripts;
  }

  public Map<String, String> getConfigFiles() {
    return configFiles;
  }

  public String getGroup() {
    return group;
  }

  public String getResultsDirectory() {
    return resultsDirectory;
  }

  public String getUser() {
    return user;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setResultsDirectory(String resultsDirectory) {
    this.resultsDirectory = resultsDirectory;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public void setScript(Script script) {
    this.script = script;
  }

  public Script getScript() {
    return script;
  }

  public String getRecipeName() {
    return recipeName;
  }

  public void setRecipeName(String recipeName) {
    this.recipeName = recipeName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
  
}
