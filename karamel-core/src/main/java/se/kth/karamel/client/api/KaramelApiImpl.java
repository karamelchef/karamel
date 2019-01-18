package se.kth.karamel.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jclouds.ContextBuilder;
import org.jclouds.domain.Credentials;
import org.jclouds.openstack.nova.v2_0.NovaApiMetadata;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.backend.command.CommandService;
import se.kth.karamel.backend.github.GithubApi;
import se.kth.karamel.backend.github.GithubUser;
import se.kth.karamel.backend.github.OrgItem;
import se.kth.karamel.backend.github.RepoItem;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.launcher.amazon.Ec2Launcher;
import se.kth.karamel.backend.launcher.google.GceContext;
import se.kth.karamel.backend.launcher.google.GceLauncher;
import se.kth.karamel.backend.launcher.nova.NovaContext;
import se.kth.karamel.backend.launcher.nova.NovaLauncher;
import se.kth.karamel.backend.launcher.novav3.NovaV3Context;
import se.kth.karamel.backend.launcher.novav3.NovaV3Launcher;
import se.kth.karamel.backend.launcher.occi.OcciContext;
import se.kth.karamel.backend.launcher.occi.OcciLauncher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.serializers.ClusterEntitySerializer;
import se.kth.karamel.backend.running.model.serializers.DefaultTaskSerializer;
import se.kth.karamel.backend.running.model.serializers.GroupEntitySerializer;
import se.kth.karamel.backend.running.model.serializers.MachineEntitySerializer;
import se.kth.karamel.backend.running.model.serializers.ShellCommandSerializer;
import se.kth.karamel.backend.running.model.tasks.AptGetEssentialsTask;
import se.kth.karamel.backend.running.model.tasks.InstallChefdkTask;
import se.kth.karamel.backend.running.model.tasks.MakeSoloRbTask;
import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.backend.running.model.tasks.VendorCookbookTask;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;
import se.kth.karamel.common.exception.InvalidOcciCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.util.Ec2Credentials;
import se.kth.karamel.common.util.NovaCredentials;
import se.kth.karamel.common.util.OcciCredentials;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.util.SshKeyService;
import se.kth.karamel.common.util.settings.NovaSetting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import se.kth.karamel.common.cookbookmeta.CookbookCache;

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
    Set<String> urls = new HashSet<>();
    urls.add(cookbookUrl);
    CookbookCache cache = ClusterDefinitionService.CACHE;
    if (refresh) {
      cache.prepareNewParallel(urls);
      KaramelizedCookbook cb = cache.get(cookbookUrl);
      return cb.getInfoJson();
    } else {
      cache.prepareParallel(urls);
      KaramelizedCookbook cb = cache.get(cookbookUrl);
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
    confs.put(Settings.AWS_ACCESSKEY_KEY, credentials.getAccessKey());
    confs.put(Settings.AWS_SECRETKEY_KEY, credentials.getSecretKey());
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
  public NovaCredentials loadNovaV3CredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    return NovaV3Launcher.readCredentials(confs);
  }

  @Override
  public boolean updateNovaV3CredentialsIfValid(NovaCredentials credentials) throws InvalidNovaCredentialsException {
    NovaV3Context context = NovaV3Launcher.validateCredentials(credentials);
    Confs confs = Confs.loadKaramelConfs();
    confs.put(NovaSetting.NOVA_ACCOUNT_ID_KEY.getParameter(), credentials.getAccountName());
    confs.put(NovaSetting.NOVA_ACCESSKEY_KEY.getParameter(), credentials.getAccountPass());
    confs.put(NovaSetting.NOVA_ACCOUNT_ENDPOINT.getParameter(), credentials.getEndpoint());
    confs.put(NovaSetting.NOVA_REGION.getParameter(), credentials.getRegion());
    confs.put(NovaSetting.NOVA_NETWORKID.getParameter(), credentials.getNetworkId());
    confs.writeKaramelConfs();
    clusterService.registerNovaV3Context(context);
    return true;
  }

  @Override
  public NovaCredentials loadNovaCredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    return NovaLauncher.readCredentials(confs);
  }

  @Override
  public boolean updateNovaCredentialsIfValid(NovaCredentials credentials) throws InvalidNovaCredentialsException {
    if (credentials.getVersion().equals("v2")) {
      NovaContext context = NovaLauncher.validateCredentials(credentials,
        ContextBuilder.newBuilder(new NovaApiMetadata()));
      clusterService.registerNovaContext(context);
    } else if (credentials.getVersion().equals("v3")) {
      NovaV3Context context = NovaV3Launcher.validateCredentials(credentials);
      clusterService.registerNovaV3Context(context);
    } else {
      // Hej
    }

    Confs confs = Confs.loadKaramelConfs();
    confs.put(NovaSetting.NOVA_ACCOUNT_ID_KEY.getParameter(), credentials.getAccountName());
    confs.put(NovaSetting.NOVA_ACCESSKEY_KEY.getParameter(), credentials.getAccountPass());
    confs.put(NovaSetting.NOVA_ACCOUNT_ENDPOINT.getParameter(), credentials.getEndpoint());
    confs.put(NovaSetting.NOVA_REGION.getParameter(), credentials.getRegion());
    confs.put(NovaSetting.NOVA_REGION.getParameter(), credentials.getRegion());
    confs.put(NovaSetting.NOVA_VERSION.getParameter(), credentials.getVersion());
    confs.put(NovaSetting.NOVA_NETWORKID.getParameter(), credentials.getNetworkId());
    confs.writeKaramelConfs();
    return true;
  }

  public OcciCredentials loadOcciCredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    return OcciLauncher.readCredentials(confs);
  }

  @Override
  public boolean updateOcciCredentialsIfValid(OcciCredentials credentials) throws InvalidOcciCredentialsException {
    OcciContext context = OcciLauncher.validateCredentials(credentials);
    Confs confs = Confs.loadKaramelConfs();
    confs.put("occi.user.certificate.path", credentials.getUserCertificatePath());
    confs.put("occi.certificate.dir", credentials.getSystemCertDir());
    confs.writeKaramelConfs();
    clusterService.registerOcciContext(context);
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
        registerTypeAdapter(InstallChefdkTask.class, new DefaultTaskSerializer()).
        setPrettyPrinting().
        create();
    String json = gson.toJson(clusterManager);
    return json;
  }

  @Override
  public void pauseCluster(String clusterName) throws KaramelException {
    clusterService.pauseDag(clusterName);
  }

  @Override
  public void resumeCluster(String clusterName) throws KaramelException {
    clusterService.resumeDag(clusterName);
  }

  @Override
  public void terminateCluster(String clusterName) throws KaramelException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void startCluster(String json) throws KaramelException {
    //logger.info("cluster to launch: \n" + json);
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
    keypair = SshKeyService.loadSshKeys(keypair.getPublicKeyPath(), keypair.getPrivateKeyPath(),
        keypair.getPassphrase());
    clusterService.registerSshKeyPair(keypair);
    return keypair;
  }

  private void saveSshConfs(SshKeyPair keypair, Confs confs) {
    confs.put(Settings.SSH_PRIVKEY_PATH_KEY, keypair.getPrivateKeyPath());
    confs.put(Settings.SSH_PUBKEY_PATH_KEY, keypair.getPublicKeyPath());
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
