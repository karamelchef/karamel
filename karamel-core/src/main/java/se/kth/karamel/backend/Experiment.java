package se.kth.karamel.backend;

import java.util.ArrayList;
import java.util.List;
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

//  /**
//   * Full path to the directory containing the experiment results.
//   */
//  private String resultsDir;
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

  private List<Code> experiments = new ArrayList<>();

  private String githubRepo = "";

  private String githubOwner = "";

  private String experimentSetupCode = "";

  private String defaultAttributes = "";

  @XmlRootElement
  public static class Code {

    private String experimentName;
    private String scriptContents;
    private String configFileName;
    private String configFile;
    private ScriptType scriptType;

    /**
     * Create an experiment as a Chef recipe.
     *
     * @param experimentName
     * @param scriptContents
     * @param configFileName
     * @param configFile
     * @param scriptType
     */
    public Code(String experimentName, String scriptContents, String configFileName, String configFile,
        ScriptType scriptType) {
      this.experimentName = experimentName;
      this.scriptContents = scriptContents;
      this.configFileName = configFileName == null ? "" : configFileName;
      this.configFile = configFile == null ? "" : configFile;
      this.scriptType = scriptType;
    }

    public Code() {
    }

    public String getExperimentName() {
      return experimentName;
    }

    public void setExperimentName(String experimentName) {
      this.experimentName = experimentName;
    }

    public String getConfigFile() {
      return configFile;
    }

    public void setConfigFile(String configFile) {
      this.configFile = configFile;
    }

    public String getConfigFileName() {
      return configFileName;
    }

    public void setConfigFileName(String configFileName) {
      this.configFileName = configFileName;
    }

    public void setScriptContents(String script) {
      this.scriptContents = script;
    }

    public String getScriptContents() {
      return scriptContents;
    }

    public String getDefaultAttributes() {
      return configFile;
    }

    public ScriptType getScriptType() {
      return scriptType;
    }

    public String getScriptCommand() {
      return scriptType.toString();
    }

    public void setDefaultAttributes(String defaultAttributes) {
      this.configFile = defaultAttributes;
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

//  public String getResultsDir() {
//    return resultsDir;
//  }
//
//  public void setResultsDir(String resultsDir) {
//    this.resultsDir = resultsDir;
//  }

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

  private boolean existsExperiment(Code exp) {
    for (Code c : experiments) {
      if (exp.getExperimentName().compareToIgnoreCase(c.getExperimentName()) == 0) {
        return false;
      }
    }
    return true;
  }

  public boolean addExperiment(Code exp) {
    if (existsExperiment(exp)) {
      return false;
    }
    experiments.add(exp);
    return true;
  }

  public boolean addExperiment(String recipeName, String scriptContents, String configFileName,
      String configFileContents, ScriptType scriptType) {
    Code exp = new Code(recipeName, scriptContents, configFileName, configFileContents, scriptType);
    if (existsExperiment(exp)) {
      return false;
    }
    experiments.add(exp);
    return true;
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

  public String getGroup() {
    return group;
  }

  public String getClusterDefinition() {
    return clusterDefinition;
  }

  public void setClusterDefinition(String clusterDefinition) {
    this.clusterDefinition = clusterDefinition;
  }

  public List<Code> getExperiments() {
    return experiments;
  }

  public void setExperiments(List<Code> experiments) {
    this.experiments = experiments;
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

  public String getDefaultAttributes() {
    return defaultAttributes;
  }

  public void setDefaultAttributes(String defaultAttributes) {
    this.defaultAttributes = defaultAttributes;
  }

}
