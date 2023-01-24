package se.kth.karamel.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.backend.command.CommandService;

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
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.util.SshKeyService;

import java.util.HashSet;
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
  public void registerSudoPassword(String password) {
    ClusterService.getInstance().getCommonContext().setSudoAccountPassword(password);
  }
}
