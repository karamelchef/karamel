package se.kth.karamel.backend;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExperimentContext {

  public static enum ScriptType {

    bash("code"), perl("code"), python("code"), ruby("code"), execute("command");

    private final String blockCommand;

    private ScriptType(String blockCommand) {
      this.blockCommand = blockCommand;
    }

    public String getBlockCommand() {
      return blockCommand;
    }
  };

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

  private final Map<String, Experiment> mapExperiments = new HashMap<>();

  public static class Experiment {

    private String scriptContents;
    private String configFileName;
    private String configFileContents;
    private String preScriptChefCode;
    private ScriptType scriptType;

    /**
     * Create an experiment as a Chef recipe.
     *
     * @param scriptContents
     * @param configFileName
     * @param configFileContents
     * @param preScriptChefCode
     * @param scriptType
     */
    public Experiment(String scriptContents, String configFileName, String configFileContents,
        String preScriptChefCode, ScriptType scriptType) {
      this.scriptContents = scriptContents;
      this.configFileName = configFileName;
      this.configFileContents = configFileContents;
      this.preScriptChefCode = preScriptChefCode;
      this.scriptType = scriptType;
    }

    public String getPreScriptChefCode() {
      return preScriptChefCode;
    }

    public void setPreScriptChefCode(String preScriptChefCode) {
      this.preScriptChefCode = preScriptChefCode;
    }

    public void setScriptContents(String script) {
      this.scriptContents = script;
    }

    public String getScriptContents() {
      return scriptContents;
    }

    public String getConfigFileContents() {
      return configFileContents;
    }

    public String getConfigFileName() {
      return configFileName;
    }

    public ScriptType getScriptType() {
      return scriptType;
    }

    public String getScriptCommand() {
      return scriptType.getBlockCommand();
    }

    public void setConfigFileContents(String configFileContents) {
      this.configFileContents = configFileContents;
    }

    public void setConfigFileName(String configFileName) {
      this.configFileName = configFileName;
    }

    public void setScriptType(ScriptType scriptType) {
      this.scriptType = scriptType;
    }

  }

  public ExperimentContext() {
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void addExperiment(String recipeName, Experiment exp) {
    mapExperiments.put(recipeName, exp);
  }

  public void addExperiment(String recipeName, String scriptContents, String configFileName, String configFileContents,
      String preScriptChefCode, ScriptType scriptType) {
    Experiment exp = new Experiment(scriptContents, configFileName, configFileContents, preScriptChefCode, scriptType);
    mapExperiments.put(recipeName, exp);
  }

  public Map<String, Experiment> getExperiments() {
    return mapExperiments;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
