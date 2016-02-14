/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import com.google.gson.Gson;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.launcher.google.GceContext;
import se.kth.karamel.backend.launcher.nova.NovaContext;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.core.clusterdef.ClusterDefinitionValidator;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.util.SshKeyPair;

/**
 * Keeps repository of running clusters with a unique name for each. Privacy sensitive data such as credentials is
 * stored inside a context. There is a common context with shared values between clusters and each cluster has its own
 * context inside which values can be overwritten.
 *
 * @author kamal
 */
public class ClusterService {

  private static final Logger logger = Logger.getLogger(ClusterService.class);

  private static final ClusterService instance = new ClusterService();

  private final ClusterContext commonContext = new ClusterContext();
  private final Map<String, ClusterManager> repository = new HashMap<>();
  private final Map<String, ClusterContext> clusterContexts = new HashMap<>();

  public static ClusterService getInstance() {
    return instance;
  }

  public ClusterContext getCommonContext() {
    return commonContext;
  }

  public Map<String, ClusterManager> getRepository() {
    return repository;
  }

  public Map<String, ClusterContext> getClusterContexts() {
    return clusterContexts;
  }

  public synchronized void registerSudoAccountPassword(String password) {
    commonContext.setSudoAccountPassword(password);
  }

  public synchronized void registerEc2Context(Ec2Context ec2Context) throws KaramelException {
    commonContext.setEc2Context(ec2Context);
  }

  public synchronized void registerEc2Context(String clusterName, Ec2Context ec2Context) throws KaramelException {
    String name = clusterName.toLowerCase();
    if (repository.containsKey(name)) {
      logger.error(String.format("'%s' is already running, you cannot change the ec2 credentials now :-|",
          clusterName));
      throw new KaramelException(String.format("Cluster '%s' is already running", clusterName));
    }

    ClusterContext clusterContext = clusterContexts.get(name);
    if (clusterContext == null) {
      clusterContext = new ClusterContext();
    }
    clusterContext.setEc2Context(ec2Context);
    clusterContexts.put(name, clusterContext);
  }

  public synchronized void registerGceContext(GceContext gceContext) throws KaramelException {
    commonContext.setGceContext(gceContext);
  }

  public synchronized void registerSshKeyPair(SshKeyPair sshKeyPair) throws KaramelException {

    File pubKey = new File(sshKeyPair.getPublicKeyPath());
    if (pubKey.exists() == false) {
      throw new KaramelException("Could not find public key: " + sshKeyPair.getPublicKeyPath());
    }
    File privKey = new File(sshKeyPair.getPrivateKeyPath());
    if (privKey.exists() == false) {
      throw new KaramelException("Could not find private key: " + sshKeyPair.getPrivateKeyPath());
    }

    commonContext.setSshKeyPair(sshKeyPair);
  }

  public synchronized void registerSshKeyPair(String clusterName, SshKeyPair sshKeyPair) throws KaramelException {
    String name = clusterName.toLowerCase();
    if (repository.containsKey(name)) {
      logger.error(String.format("'%s' is already running, you cannot change the ssh keypair now :-|", clusterName));
      throw new KaramelException(String.format("Cluster '%s' is already running", clusterName));
    }

    ClusterContext clusterContext = clusterContexts.get(name);
    if (clusterContext == null) {
      clusterContext = new ClusterContext();
    }
    clusterContext.setSshKeyPair(sshKeyPair);
    clusterContexts.put(name, clusterContext);
  }

  public synchronized ClusterRuntime clusterStatus(String clusterName) throws KaramelException {
    String name = clusterName.toLowerCase();
    if (!repository.containsKey(name)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
    ClusterManager cluster = repository.get(name);
    return cluster.getRuntime();
  }

  public synchronized void startCluster(String json) throws KaramelException {
    Gson gson = new Gson();
    JsonCluster jsonCluster = gson.fromJson(json, JsonCluster.class);
    ClusterDefinitionValidator.validate(jsonCluster);
    String yml = ClusterDefinitionService.jsonToYaml(jsonCluster);
    //We have to do it again otherwise the global scope attributes get lost
    //for more info refer to https://github.com/karamelchef/karamel/issues/28
    jsonCluster = ClusterDefinitionService.yamlToJsonObject(yml);
    ClusterDefinitionService.saveYaml(yml);
    logger.info(String.format("Let me see if I can start '%s' ...", jsonCluster.getName()));
    String clusterName = jsonCluster.getName();
    String name = clusterName.toLowerCase();
    if (repository.containsKey(name)) {
      logger.info(String.format("'%s' is already running :-|", jsonCluster.getName()));
      throw new KaramelException(String.format("Cluster '%s' is already running", clusterName));
    }
    ClusterContext checkedContext = checkContext(jsonCluster);
    ClusterManager cluster = new ClusterManager(jsonCluster, checkedContext);
    repository.put(name, cluster);
    cluster.start();
    cluster.enqueue(ClusterManager.Command.LAUNCH_CLUSTER);
    cluster.enqueue(ClusterManager.Command.SUBMIT_INSTALL_DAG);
  }

  public synchronized void submitInstallationDag(String clusterName) throws KaramelException {
    String name = clusterName.toLowerCase();
    logger.info(String.format("User asked for installing '%s'", clusterName));
    if (!repository.containsKey(name)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
    ClusterManager cluster = repository.get(name);
    checkContext(cluster.getDefinition());
    cluster.enqueue(ClusterManager.Command.INTERRUPT_DAG);
    cluster.enqueue(ClusterManager.Command.SUBMIT_INSTALL_DAG);
  }

  public synchronized void submitPurgeDag(String clusterName) throws KaramelException {
    String name = clusterName.toLowerCase();
    logger.info(String.format("User asked for purging '%s'", clusterName));
    if (!repository.containsKey(name)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
    ClusterManager cluster = repository.get(name);
    checkContext(cluster.getDefinition());
    cluster.enqueue(ClusterManager.Command.INTERRUPT_DAG);
    cluster.enqueue(ClusterManager.Command.SUBMIT_PURGE_DAG);
  }

  public synchronized void pauseDag(String clusterName) throws KaramelException {
    String name = clusterName.toLowerCase();
    logger.info(String.format("User asked for pausing the cluster '%s'", clusterName));
    if (!repository.containsKey(name)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
    ClusterManager cluster = repository.get(name);
    checkContext(cluster.getDefinition());
    cluster.enqueue(ClusterManager.Command.PAUSE_DAG);
  }

  public synchronized void resumeDag(String clusterName) throws KaramelException {
    String name = clusterName.toLowerCase();
    logger.info(String.format("User asked for resuming the cluster '%s'", clusterName));
    if (!repository.containsKey(name)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
    ClusterManager cluster = repository.get(name);
    checkContext(cluster.getDefinition());
    cluster.enqueue(ClusterManager.Command.RESUME_DAG);

  }

  public synchronized void terminateCluster(String clusterName) throws KaramelException {
    String name = clusterName.toLowerCase();
    logger.info(String.format("User asked for terminating the cluster '%s'", clusterName));
    if (!repository.containsKey(name)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }

    final ClusterManager cluster = repository.get(name);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          ClusterRuntime runtime = cluster.getRuntime();
          cluster.enqueue(ClusterManager.Command.INTERRUPT_CLUSTER);
          cluster.enqueue(ClusterManager.Command.TERMINATE_CLUSTER);
          while (runtime.getPhase() != ClusterRuntime.ClusterPhases.NOT_STARTED) {
            Thread.sleep(100);
          }
          String name = runtime.getName().toLowerCase();
          logger.info(String.format("Cluster '%s' terminated, rmoving it from the list of running clusters",
              runtime.getName()));
          repository.remove(name);
        } catch (InterruptedException ex) {
        } catch (KaramelException ex) {
          logger.error("", ex);
        }
      }
    };
    t.start();
  }

  private ClusterContext checkContext(JsonCluster definition) throws KaramelException {
    String name = definition.getName().toLowerCase();
    ClusterContext context = clusterContexts.get(name);
    ClusterContext validatedContext = ClusterContext.validateContext(definition, context, commonContext);
    clusterContexts.put(name, validatedContext);
    return validatedContext;
  }

  public synchronized void registerNovaContext(NovaContext context) {
    commonContext.setNovaContext(context);
  }
}
