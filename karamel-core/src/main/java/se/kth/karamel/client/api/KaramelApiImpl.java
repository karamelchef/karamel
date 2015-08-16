/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.launcher.amazon.Ec2Launcher;
import se.kth.karamel.backend.launcher.google.GceContext;
import se.kth.karamel.backend.launcher.google.GceLauncher;
import se.kth.karamel.backend.launcher.nova.NovaContext;
import se.kth.karamel.backend.launcher.nova.NovaLauncher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.serializers.ClusterEntitySerializer;
import se.kth.karamel.backend.running.model.serializers.DefaultTaskSerializer;
import se.kth.karamel.backend.running.model.serializers.GroupEntitySerializer;
import se.kth.karamel.backend.running.model.serializers.MachineEntitySerializer;
import se.kth.karamel.backend.running.model.serializers.ShellCommandSerializer;
import se.kth.karamel.backend.running.model.tasks.AptGetEssentialsTask;
import se.kth.karamel.backend.running.model.tasks.InstallBerkshelfTask;
import se.kth.karamel.backend.running.model.tasks.MakeSoloRbTask;
import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.backend.running.model.tasks.VendorCookbookTask;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.NovaCredentials;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.SshKeyService;
import se.kth.karamel.common.exception.InvalidNovaCredentialsException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.settings.NovaSetting;
import se.kth.karamel.cookbook.metadata.KaramelizedCookbook;

/**
 * Implementation of the Karamel Api for UI
 *
 * @author kamal
 */
public class KaramelApiImpl implements KaramelApi {

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
      return cb.getMetadataJson();
    } else {
      KaramelizedCookbook cb = CookbookCache.get(cookbookUrl);
      return cb.getMetadataJson();
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
    Credentials credentials = GceLauncher.readCredentials(jsonFilePath);
    GceContext context = GceLauncher.validateCredentials(credentials);
    Confs confs = Confs.loadKaramelConfs();
    confs.put(Settings.GCE_JSON_KEY_FILE_PATH, jsonFilePath);
    confs.writeKaramelConfs();
    clusterService.registerGceContext(context);
    return true;
  }

  @Override
  public NovaCredentials loadNovaCredentialsIfExist() throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    return NovaLauncher.readCredentials(confs);
  }

  @Override
  public boolean updateNovaCredentialsIfValid(NovaCredentials credentials) throws InvalidNovaCredentialsException {
    NovaContext context = NovaLauncher.validateCredentials(credentials,
                                                            ContextBuilder.newBuilder(new NovaApiMetadata()));
    Confs confs = Confs.loadKaramelConfs();
    confs.put(NovaSetting.NOVA_ACCOUNT_ID_KEY, credentials.getAccountName());
    confs.put(NovaSetting.NOVA_ACCESSKEY_KEY, credentials.getAccountPass());
    confs.put(NovaSetting.NOVA_ACCOUNT_ENDPOINT, credentials.getEndpoint());
    confs.put(NovaSetting.NOVA_REGION, credentials.getRegion());
    confs.writeKaramelConfs();
    clusterService.registerNovaContext(context);
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
  public void registerGithubAccount(String email, String password) {
    // TODO - test github credentials
    ClusterService.getInstance().getCommonContext().setGithubEmail(email);
    ClusterService.getInstance().getCommonContext().setGithubPassword(password);
    
  }
  
}
