package se.kth.karamel.backend;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExperimentContext {

  public static enum ScriptType { bash, csh, perl, python, ruby };

  /**
   * Url for the experiment code. Can have a .jar or .tar.gz extention.
   */
  private String url;
  
  /**
   * YAML for the Cluster context
   */
  private String clusterDefinition;

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

  private Map<String, Experiment> mapExperiments = new HashMap<>();

  private String githubRepo = "";
  
  private String githubOwner = "";
  
//  private String attributes = "";
  
  
  @XmlRootElement
  public static class Experiment {

    private String scriptContents;
    private String defaultAttributes;
    private String preScriptChefCode;
    private ScriptType scriptType;

    /**
     * Create an experiment as a Chef recipe.
     *
     * @param scriptContents
     * @param configFileName
     * @param defaultAttributes
     * @param preScriptChefCode
     * @param scriptType
     */
    public Experiment(String scriptContents, String defaultAttributes,
        String preScriptChefCode, ScriptType scriptType) {
      this.scriptContents = scriptContents;
      this.defaultAttributes = defaultAttributes;
      this.preScriptChefCode = preScriptChefCode == null ? "" : preScriptChefCode;
      this.scriptType = scriptType;
    }

    public Experiment() {
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

    public String getDefaultAttributes() {
      return defaultAttributes;
    }

    public ScriptType getScriptType() {
      return scriptType;
    }

    public String getScriptCommand() {
      return scriptType.toString();
    }

    public void setDefaultAttributes(String defaultAttributes) {
      this.defaultAttributes = defaultAttributes;
    }

    public void setScriptType(ScriptType scriptType) {
      this.scriptType = scriptType;
    }

  }

  public ExperimentContext() {
  }

  public void setGithubOwner(String githubOwner) {
    this.githubOwner = githubOwner;
  }

  public String getGithubOwner() {
    return githubOwner;
  }

  public String getGithubRepo() {
    return githubRepo;
  }

  public void setGithubRepo(String githubRepo) {
    this.githubRepo = githubRepo;
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

  public void addExperiment(String recipeName, String scriptContents, String defaultAttrContents,
      String preScriptChefCode, ScriptType scriptType) {
    Experiment exp = new Experiment(scriptContents, defaultAttrContents, preScriptChefCode, scriptType);
    mapExperiments.put(recipeName, exp);
  }

  public Map<String, Experiment> getExperiments() {
    return mapExperiments;
  }

  public String getGroup() {
    return group;
  }

  public String getClusterDefinition() {
    return clusterDefinition;
  }

  public void setClusterDefinition(String clusterDefinition) {
    this.clusterDefinition = clusterDefinition;
  }

  public Map<String, Experiment> getMapExperiments() {
    return mapExperiments;
  }

  public void setMapExperiments(Map<String, Experiment> mapExperiments) {
    this.mapExperiments = mapExperiments;
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
