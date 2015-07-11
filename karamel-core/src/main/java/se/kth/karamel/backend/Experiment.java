package se.kth.karamel.backend;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Experiment {

  public static enum ScriptType {

    bash, csh, perl, python, ruby
  };

  /**
   * Url for the experiment binary. Typically, a .tar.gz extention.
   */
  private String urlBinary;

  /**
   * Url for the experiment source code. Typically, a github URL.
   */
  private String urlGitClone;

  /**
   * Maven command to build experiment source code.
   */
  private String mavenCommand;

  /**
   * Comma-separated String of Cookbook::recipe dependencies used to generate the KaramelFile
   */
  private String dependencies;

  /**
   * Full path to the directory containing the experiment results.
   */
  private String resultsDir;
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

  private Map<String, Code> mapExperiments = new HashMap<>();

  private String githubRepo = "";

  private String githubOwner = "";

  private String experimentSetupCode = "";

//  private String attributes = "";
  @XmlRootElement
  public static class Code {

    private String scriptContents;
    private String defaultAttributes;
    private String preScriptChefCode;
    private ScriptType scriptType;

    /**
     * Create an experiment as a Chef recipe.
     *
     * @param scriptContents
     * @param defaultAttributes
     * @param preScriptChefCode
     * @param scriptType
     */
    public Code(String scriptContents, String defaultAttributes,
        String preScriptChefCode, ScriptType scriptType) {
      this.scriptContents = scriptContents;
      this.defaultAttributes = defaultAttributes;
      this.preScriptChefCode = preScriptChefCode == null ? "" : preScriptChefCode;
      this.scriptType = scriptType;
    }

    public Code() {
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

  public Experiment() {
  }

  public String getExperimentSetupCode() {
    return experimentSetupCode;
  }

  public void setExperimentSetupCode(String experimentSetupCode) {
    this.experimentSetupCode = experimentSetupCode;
  }

  public String getResultsDir() {
    return resultsDir;
  }

  public void setResultsDir(String resultsDir) {
    this.resultsDir = resultsDir;
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

  public String getUrlBinary() {
    return urlBinary;
  }

  public void setUrlBinary(String url) {
    this.urlBinary = url;
  }

  public String getUrlGitClone() {
    return urlGitClone;
  }

  public void setUrlGitClone(String urlGitClone) {
    this.urlGitClone = urlGitClone;
  }

  public void addExperiment(String recipeName, Code exp) {
    mapExperiments.put(recipeName, exp);
  }

  public void addExperiment(String recipeName, String scriptContents, String defaultAttrContents,
      String preScriptChefCode, ScriptType scriptType) {
    Code exp = new Code(scriptContents, defaultAttrContents, preScriptChefCode, scriptType);
    mapExperiments.put(recipeName, exp);
  }

  public void setDependencies(String dependencies) {
    this.dependencies = dependencies;
  }

  public String getDependencies() {
    return dependencies;
  }

  public void setMavenCommand(String mavenCommand) {
    this.mavenCommand = mavenCommand;
  }

  public String getMavenCommand() {
    return mavenCommand;
  }

  public Map<String, Code> getExperiments() {
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

  public Map<String, Code> getMapExperiments() {
    return mapExperiments;
  }

  public void setMapExperiments(Map<String, Code> mapExperiments) {
    this.mapExperiments = mapExperiments;
  }

  public String getUser() {
    return user;
  }

  public void setGroup(String group) {
    this.group = group;
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
