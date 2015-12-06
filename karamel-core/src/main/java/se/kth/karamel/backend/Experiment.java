package se.kth.karamel.backend;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Experiment {

  /**
   * username to run program as
   */
  private String user = "karamel";

  /**
   * groupname to run program as
   */
  private String group = "karamel";

  /**
   * Repository on GitHub
   */
  private String githubRepo = "";

  /**
   * Description of the experiment cookbook.
   */
  private String description = "Karamel experiment repository description placeholder";

  private String githubOwner = "";

  /**
   * Comma-separated String of Cookbook::recipe global dependencies used to generate the KaramelFile
   */
  private String localDependencies="";

  /**
   * Comma-separated String of Cookbook::recipe local dependencies used to generate the KaramelFile
   */
  private String globalDependencies="";
  
  /**
   * Comma-separated list of Berksfile dependencies. Each entry is enclosed in double-quotation marks.
   */
  private String berksfile = "";

  /**
   * Url for the experiment binary. Typically, a .tar.gz extention.
   */
  private String urlBinary = "";

  /**
   * Url for the experiment source code. Typically, a github URL.
   */
  private String urlGitClone = "";

  /**
   * Maven command to build experiment source code.
   */
  private String mavenCommand = "";

  /**
   * Chef code to be executed before the experiment in the Install phase
   */
  private String experimentSetupCode = "";

  
  /**
   * default/attributes.rb in Chef
   */
  private String defaultAttributes = "";

  /**
   * YAML for the Cluster context
   */
  private String clusterDefinition = "";


  private ArrayList<Code> code = new ArrayList<>();
  
  
  @XmlRootElement
  public static class Code {

    private String name;
    private String scriptContents;
    private String configFileName;
    private String configFileContents;
    private String scriptType;

    /**
     * Create an experiment as a Chef recipe.
     *
     * @param name
     * @param scriptContents
     * @param configFileName
     * @param configFileContents
     * @param scriptType
     */
    public Code(String name, String scriptContents, String configFileName, String configFileContents,
        String scriptType) {
      this.name = name;
      this.scriptContents = scriptContents;
      this.configFileName = configFileName == null ? "" : configFileName;
      this.configFileContents = configFileContents == null ? "" : configFileContents;
      this.scriptType = scriptType;
    }

    public Code() {
    }

    public String getName() {
      return name;
    }

    public void setName(String experimentName) {
      this.name = experimentName;
    }

    public String getConfigFileContents() {
      return configFileContents;
    }

    public void setConfigFileContents(String configFileContents) {
      this.configFileContents = configFileContents;
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

    public String getScriptType() {
      return scriptType;
    }

    public String getScriptCommand() {
      return scriptType;
    }

    public void setScriptType(String scriptType) {
      this.scriptType = scriptType;
    }

  }

  public Experiment() {
  }

  public ArrayList<Code> getCode() {
    return code;
  }

  public void setCode(ArrayList<Code> code) {
    this.code = code;
  }
  
  public String getExperimentSetupCode() {
    return experimentSetupCode;
  }

  public void setExperimentSetupCode(String experimentSetupCode) {
    this.experimentSetupCode = experimentSetupCode;
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

  public String getGlobalDependencies() {
    return globalDependencies;
  }

  public void setGlobalDependencies(String globalDependencies) {
    this.globalDependencies = globalDependencies;
  }

  public String getLocalDependencies() {
    return localDependencies;
  }

  public void setLocalDependencies(String localDependencies) {
    this.localDependencies = localDependencies;
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

  public String getBerksfile() {
    return berksfile;
  }

  public void setBerksfile(String berksfile) {
    this.berksfile = berksfile;
  }
  
}
