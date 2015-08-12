package se.kth.karamel.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.jclouds.domain.Credentials;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.Experiment;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.backend.command.CommandService;
import se.kth.karamel.backend.github.GithubApi;
import se.kth.karamel.backend.github.GithubUser;
import se.kth.karamel.backend.github.OrgItem;
import se.kth.karamel.backend.github.RepoItem;
import se.kth.karamel.backend.github.util.ChefExperimentExtractor;
import se.kth.karamel.backend.github.util.GithubUrl;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.launcher.amazon.Ec2Launcher;
import se.kth.karamel.backend.launcher.google.GceContext;
import se.kth.karamel.backend.launcher.google.GceLauncher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.serializers.ClusterEntitySerializer;
import se.kth.karamel.backend.running.model.serializers.GroupEntitySerializer;
import se.kth.karamel.backend.running.model.serializers.MachineEntitySerializer;
import se.kth.karamel.backend.running.model.serializers.ShellCommandSerializer;
import se.kth.karamel.backend.running.model.serializers.DefaultTaskSerializer;
import se.kth.karamel.backend.running.model.tasks.AptGetEssentialsTask;
import se.kth.karamel.backend.running.model.tasks.InstallBerkshelfTask;
import se.kth.karamel.backend.running.model.tasks.MakeSoloRbTask;
import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.backend.running.model.tasks.VendorCookbookTask;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.cookbook.metadata.KaramelizedCookbook;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.SshKeyService;
import se.kth.karamel.cookbook.metadata.Berksfile;
import se.kth.karamel.cookbook.metadata.DefaultRb;
import se.kth.karamel.cookbook.metadata.ExperimentRecipe;
import se.kth.karamel.cookbook.metadata.InstallRecipe;
import se.kth.karamel.cookbook.metadata.KaramelFile;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlDependency;

/**
 * Implementation of the Karamel Api for UI
 *
 * @author kamal
 */
public class KaramelApiImpl implements KaramelApi {

  private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(KaramelApiImpl.class);

  private static final ClusterService clusterService = ClusterService.getInstance();

  @Override
  public String commandCheatSheet() throws KaramelException {
    return CommandService.processCommand("help").getResult();
  }

  @Override
  public CommandResponse processCommand(String command, String... args) throws KaramelException {
    return CommandService.processCommand(command, args);
  }

  @Override
  public String getCookbookDetails(String cookbookUrl, boolean refresh) throws KaramelException {
    if (refresh) {
      KaramelizedCookbook cb = CookbookCache.load(cookbookUrl);
      return cb.getInfoJson();
    } else {
      KaramelizedCookbook cb = CookbookCache.get(cookbookUrl);
      return cb.getInfoJson();
    }
  }

  @Override
  public String jsonToYaml(String json) throws KaramelException {
    return ClusterDefinitionService.jsonToYaml(json);
  }

  @Override
  public String yamlToJson(String yaml) throws KaramelException {
    return ClusterDefinitionService.yamlToJson(yaml);
  }

  @Override
  public Ec2Credentials loadEc2CredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    return Ec2Launcher.readCredentials(confs);
  }

  @Override
  public boolean updateEc2CredentialsIfValid(Ec2Credentials credentials) throws KaramelException {
    Ec2Context context = Ec2Launcher.validateCredentials(credentials);
    Confs confs = Confs.loadKaramelConfs();
    confs.put(Settings.AWS_ACCESS_KEY, credentials.getAccessKey());
    confs.put(Settings.AWS_SECRET_KEY, credentials.getSecretKey());
    confs.writeKaramelConfs();
    clusterService.registerEc2Context(context);
    return true;
  }

  @Override
  public String loadGceCredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    String path = confs.getProperty(Settings.GCE_JSON_KEY_FILE_PATH);
    if (path != null) {
      Credentials credentials = GceLauncher.readCredentials(path);
      if (credentials != null) {
        return path;
      }
    }

    return null;
  }

  @Override
  public boolean updateGceCredentialsIfValid(String jsonFilePath) throws KaramelException {
    if (jsonFilePath.isEmpty() || jsonFilePath == null) {
      return false;
    }
    try {
      Credentials credentials = GceLauncher.readCredentials(jsonFilePath);
      GceContext context = GceLauncher.validateCredentials(credentials);
      Confs confs = Confs.loadKaramelConfs();
      confs.put(Settings.GCE_JSON_KEY_FILE_PATH, jsonFilePath);
      confs.writeKaramelConfs();
      clusterService.registerGceContext(context);
    } catch (Throwable ex) {
      throw new KaramelException(ex.getMessage());
    }
    return true;
  }

  @Override
  public String getClusterStatus(String clusterName) throws KaramelException {
    ClusterRuntime clusterManager = clusterService.clusterStatus(clusterName);
    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();
    Gson gson = builder.
        registerTypeAdapter(ClusterRuntime.class, new ClusterEntitySerializer()).
        registerTypeAdapter(MachineRuntime.class, new MachineEntitySerializer()).
        registerTypeAdapter(GroupRuntime.class, new GroupEntitySerializer()).
        registerTypeAdapter(ShellCommand.class, new ShellCommandSerializer()).
        registerTypeAdapter(RunRecipeTask.class, new DefaultTaskSerializer()).
        registerTypeAdapter(MakeSoloRbTask.class, new DefaultTaskSerializer()).
        registerTypeAdapter(VendorCookbookTask.class, new DefaultTaskSerializer()).
        registerTypeAdapter(AptGetEssentialsTask.class, new DefaultTaskSerializer()).
        registerTypeAdapter(InstallBerkshelfTask.class, new DefaultTaskSerializer()).
        setPrettyPrinting().
        create();
    String json = gson.toJson(clusterManager);
    return json;
  }

  @Override
  public void pauseCluster(String clusterName) throws KaramelException {
    clusterService.pauseCluster(clusterName);
  }

  @Override
  public void resumeCluster(String clusterName) throws KaramelException {
    clusterService.resumeCluster(clusterName);
  }

  @Override
  public void purgeCluster(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void startCluster(String json) throws KaramelException {
    logger.info("cluster to launch: \n" + json);
    clusterService.startCluster(json);
  }

  @Override
  public String getInstallationDag(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SshKeyPair loadSshKeysIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    SshKeyPair sshKeys = SshKeyService.loadSshKeys(confs);
    return sshKeys;
  }

  @Override
  public SshKeyPair loadSshKeysIfExist(String clusterName) throws KaramelException {
    Confs confs = Confs.loadAllConfsForCluster(clusterName);
    SshKeyPair sshKeys = SshKeyService.loadSshKeys(confs);
    return sshKeys;
  }

  @Override
  public SshKeyPair generateSshKeysAndUpdateConf() throws KaramelException {
    SshKeyPair sshkeys = SshKeyService.generateAndStoreSshKeys();
    Confs confs = Confs.loadKaramelConfs();
    confs.put(Settings.SSH_PRIVKEY_PATH_KEY, sshkeys.getPrivateKeyPath());
    confs.put(Settings.SSH_PUBKEY_PATH_KEY, sshkeys.getPublicKeyPath());
    confs.writeKaramelConfs();
    return sshkeys;
  }

  @Override
  public SshKeyPair generateSshKeysAndUpdateConf(String clusterName) throws KaramelException {
    SshKeyPair sshkeys = SshKeyService.generateAndStoreSshKeys(clusterName);
    Confs confs = Confs.loadJustClusterConfs(clusterName);
    confs.put(Settings.SSH_PRIVKEY_PATH_KEY, sshkeys.getPrivateKeyPath());
    confs.put(Settings.SSH_PUBKEY_PATH_KEY, sshkeys.getPublicKeyPath());
    confs.writeClusterConfs(clusterName);
    return sshkeys;
  }

  @Override
  public SshKeyPair registerSshKeys(SshKeyPair keypair) throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    saveSshConfs(keypair, confs);
    confs.writeKaramelConfs();
//    keypair = SshKeyService.loadSshKeys(confs);
    keypair = SshKeyService.loadSshKeys(keypair.getPublicKeyPath(), keypair.getPrivateKeyPath(),
        keypair.getPassphrase());
    clusterService.registerSshKeyPair(keypair);
    return keypair;
  }

  private void saveSshConfs(SshKeyPair keypair, Confs confs) {
    confs.put(Settings.SSH_PRIVKEY_PATH_KEY, keypair.getPrivateKeyPath());
    confs.put(Settings.SSH_PUBKEY_PATH_KEY, keypair.getPublicKeyPath());
    if (keypair.getPassphrase() != null && keypair.getPassphrase().isEmpty() == false) {
      confs.put(Settings.SSH_PRIVKEY_PASSPHRASE, keypair.getPassphrase());
    }
  }

  @Override
  public SshKeyPair registerSshKeys(String clusterName, SshKeyPair keypair) throws KaramelException {
    Confs confs = Confs.loadJustClusterConfs(clusterName);
    saveSshConfs(keypair, confs);
    confs.writeClusterConfs(clusterName);
    keypair = SshKeyService.loadSshKeys(keypair.getPublicKeyPath(), keypair.getPrivateKeyPath(),
        keypair.getPassphrase());
    clusterService.registerSshKeyPair(clusterName, keypair);
    return keypair;
  }

  @Override
  public void registerSudoPassword(String password) {
    ClusterService.getInstance().getCommonContext().setSudoAccountPassword(password);
  }

  @Override
  public List<OrgItem> listGithubOrganizations() throws KaramelException {
    return GithubApi.getOrganizations();
  }

  @Override
  public List<RepoItem> listGithubRepos(String organization) throws KaramelException {
    return GithubApi.getRepos(organization);
  }

  @Override
  public GithubUser registerGithubAccount(String user, String password) throws KaramelException {
    return GithubApi.registerCredentials(user, password);
  }

  @Override
  public GithubUser loadGithubCredentials() throws KaramelException {
    return GithubApi.loadGithubCredentials();
  }

  private void initGithubRepo(String user, String owner, String repo, String description) throws KaramelException {
    if (owner == null || owner.isEmpty() || owner.compareToIgnoreCase(user) == 0) {
      GithubApi.createRepoForUser(repo, description);
    } else {
      GithubApi.createRepoForOrg(owner, repo, description);
    }
  }

  @Override
  public void commitAndPushExperiment(Experiment experiment) throws KaramelException {
    String owner = experiment.getGithubOwner();
    String repoName = experiment.getGithubRepo();
    File f = GithubApi.getRepoDirectory(repoName);
    boolean repoExists = GithubApi.repoExists(owner, repoName);
    if (repoExists) {
      // local copy must exist, already pushed to GitHub
      if (!f.exists()) {
        throw new KaramelException("Remote repository already exists. Load the experiment if it already exists.");
      }
    } else {
      // no repo on GitHub. Should not exist a local directory with same repo name.
      if (f.exists()) {
        throw new KaramelException("The remote repo does not exist, however a conflicting local directory was found. "
            + "Remove the local directory in ~/.karamel/cookbook_designer first, then save again.");
      } else {
        // Create the repo if it doesn't exist and clone it to a local directory
        // That way, the local directory will only ever exist if it the repo has been created first.
        // Users should subsequently load a directory from GitHub.
        initGithubRepo(GithubApi.getUser(), owner, repoName, experiment.getDescription());
        // Scaffold a new experiment project with Karamel/Chef
        GithubApi.scaffoldRepo(repoName);
      }
    }

    // For all config and script files, compile them and generate Karamel/Chef files
    ChefExperimentExtractor.parseAttributesAddToGit(owner, repoName, experiment);

    ChefExperimentExtractor.parseRecipesAddToGit(owner, repoName, experiment);

    // Commit and push all changes to github
    GithubApi.commitPush(owner, repoName);

  }

  @Override
  public Experiment loadExperiment(String githubRepoUrl) throws KaramelException {
    Experiment ec = new Experiment();
    String repoName = GithubUrl.extractRepoName(githubRepoUrl);
    ec.setGithubRepo(repoName);
    String owner = GithubUrl.extractUserName(githubRepoUrl);
    ec.setGithubOwner(owner);
    if (repoName == null || owner == null || repoName.isEmpty() || owner.isEmpty()) {
      throw new KaramelException("Misformed url repo/owner: " + githubRepoUrl);
    }

    // Loading a repo involves wiping any existing local copy and replacing it with GitHub's copy.
    File localPath = new File(Settings.COOKBOOKS_PATH + File.separator + repoName);
    if (localPath.isDirectory() == true) {
      try {
        FileUtils.deleteDirectory(localPath);
      } catch (IOException ex) {
        logger.warn(ex.getMessage());
        if (localPath.isDirectory() == true) {
          throw new KaramelException("Couldn't remove local copy of the repo at directory: " + localPath.getPath());
        }
      }
    }
    // Download the latest copy from GitHub
    GithubApi.cloneRepo(owner, repoName);
    String strippedUrl = githubRepoUrl.replaceAll("\\.git", "");
    ec.setUrlGitClone(githubRepoUrl);

    KaramelizedCookbook kc = new KaramelizedCookbook(strippedUrl, true);
    KaramelFile kf = kc.getKaramelFile();
    Berksfile bf = kc.getBerksFile();
    DefaultRb attributes = kc.getDefaultRb();
    List<ExperimentRecipe> er = kc.getExperimentRecipes();
    InstallRecipe ir = kc.getInstallRecipe();

    ec.setUser((String) attributes.getValue(repoName + "/user"));
    ec.setGroup((String) attributes.getValue(repoName + "/group"));
    ec.setUrlBinary((String) attributes.getValue(repoName + "/url"));
    ec.setBerksfile(bf.toString());
    ec.setExperimentSetupCode(ir.getSetupCode());
    ArrayList<YamlDependency> deps = kf.getDependencies();
    Set<String> localSet = new HashSet<>();
    Set<String> globalSet = new HashSet<>();
    for (YamlDependency yd : deps) {
      if (!yd.getRecipe().contains(Settings.COOKBOOK_DELIMITER + "install")) {
        List<String> locals = yd.getLocal();
        // remove duplicates from locals
        localSet.addAll(locals);
        List<String> globals = yd.getGlobal();
        globalSet.addAll(globals);

      }
    }
    StringBuilder local = new StringBuilder();
    StringBuilder global = new StringBuilder();
    int i = 0;
    for (String s : localSet) {
      if (i == 0) {
        local.append(s);
      } else {
        local.append(System.lineSeparator()).append(s);
      }
      i++;
    }
    i = 0;
    for (String s : globalSet) {
      if (i == 0) {
        global.append(s);
      } else {
        global.append(System.lineSeparator()).append(s);
      }
      i++;
    }

    ec.setLocalDependencies(local.toString());
    ec.setGlobalDependencies(global.toString());
    for (ExperimentRecipe r : er) {
      Experiment.Code exp = new Experiment.Code(r.getRecipeName(), r.getScriptContents(), r.getConfigFileName(),
          r.getConfigFileContents(), r.getScriptType());
      List<Experiment.Code> exps = ec.getCode();
      exps.add(exp);
    }
    return ec;
  }

  @Override
  public RepoItem createGithubRepo(String org, String repo, String description) throws KaramelException {
    return GithubApi.createRepoForOrg(org, repo, description);
  }

  @Override
  public void removeFileFromExperiment(String owner, String repo, String experimentName) {
    try {
      GithubApi.removeFile(owner, repo, experimentName);
    } catch (KaramelException ex) {
      // Do nothing - Repository hasn't been created yet. That's ok.");
    }
  }

  @Override
  public void removeRepo(String owner, String repo, boolean removeLocal, boolean removeGitHub) throws KaramelException {
    boolean failedLocal = false;
    String failedMsg = "";

    // if failure while removing locally, still try and remove remote repo (if requested)
    if (removeLocal) {
      try {
        GithubApi.removeLocalRepo(owner, repo);
      } catch (KaramelException ex) {
        failedLocal = true;
        failedMsg = ex.toString();
      }
    }
    if (removeGitHub) {
      GithubApi.removeRepo(owner, repo);
    }
    if (failedLocal) {
      throw new KaramelException(failedMsg);
    }

  }
}
