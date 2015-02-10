/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.running.model.ClusterEntity;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.common.SshKeyPair;

/**
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
  
  public synchronized void registerEc2Context(Ec2Context ec2Context) throws KaramelException {
    commonContext.setEc2Context(ec2Context);
  }

  public synchronized void registerEc2Context(String clusterName, Ec2Context ec2Context) throws KaramelException {
    if (repository.containsKey(clusterName)) {
      logger.error(String.format("'%s' is already running, you cannot change the ec2 credentials now :-|", clusterName));
      throw new KaramelException(String.format("Cluster '%s' is already running", clusterName));
    }

    ClusterContext clusterContext = clusterContexts.get(clusterName);
    if (clusterContext == null) {
      clusterContext = new ClusterContext();
    }
    clusterContext.setEc2Context(ec2Context);
    clusterContexts.put(clusterName, clusterContext);
  }

  public synchronized void registerSshKeyPair(SshKeyPair sshKeyPair) throws KaramelException {
    commonContext.setSshKeyPair(sshKeyPair);
  }

  public synchronized void registerSshKeyPair(String clusterName, SshKeyPair sshKeyPair) throws KaramelException {
    if (repository.containsKey(clusterName)) {
      logger.error(String.format("'%s' is already running, you cannot change the ssh keypair now :-|", clusterName));
      throw new KaramelException(String.format("Cluster '%s' is already running", clusterName));
    }

    ClusterContext clusterContext = clusterContexts.get(clusterName);
    if (clusterContext == null) {
      clusterContext = new ClusterContext();
    }
    clusterContext.setSshKeyPair(sshKeyPair);
    clusterContexts.put(clusterName, clusterContext);
  }

  public synchronized ClusterEntity clusterStatus(String clusterName) throws KaramelException {
    if (!repository.containsKey(clusterName)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
    ClusterManager cluster = repository.get(clusterName);
    return cluster.getRuntime();
  }

  public synchronized void startCluster(String json) throws KaramelException {
    Gson gson = new Gson();
    JsonCluster jsonCluster = gson.fromJson(json, JsonCluster.class);
    logger.info(String.format("Let me see if I can start '%s' ...", jsonCluster.getName()));
    String clusterName = jsonCluster.getName();
    if (repository.containsKey(clusterName)) {
      logger.info(String.format("'%s' is already running :-|", jsonCluster.getName()));
      throw new KaramelException(String.format("Cluster '%s' is already running", clusterName));
    }
    ClusterContext checkedContext = checkContext(clusterName);
    ClusterManager cluster = new ClusterManager(jsonCluster, checkedContext);
    repository.put(clusterName, cluster);
    cluster.start();
    cluster.enqueue(ClusterManager.Command.LAUNCH);
  }

  public synchronized void pauseCluster(String clusterName) throws KaramelException {
    logger.info(String.format("User asked for pausing the cluster '%s'", clusterName));
    if (!repository.containsKey(clusterName)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
    checkContext(clusterName);
    ClusterManager cluster = repository.get(clusterName);
    cluster.enqueue(ClusterManager.Command.PAUSE);
  }

  public synchronized void resumeCluster(String clusterName) throws KaramelException {
    logger.info(String.format("User asked for resuming the cluster '%s'", clusterName));
    if (!repository.containsKey(clusterName)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
    checkContext(clusterName);
    ClusterManager cluster = repository.get(clusterName);
    cluster.enqueue(ClusterManager.Command.RESUME);

  }

  public synchronized void purgeCluster(String clusterName) throws KaramelException {
    logger.info(String.format("User asked for purging the cluster '%s'", clusterName));
    if (!repository.containsKey(clusterName)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }

    ClusterManager cluster = repository.get(clusterName);
    cluster.enqueue(ClusterManager.Command.PURGE);
  }

  private ClusterContext checkContext(String clusterName) throws KaramelException {
    ClusterContext context = clusterContexts.get(clusterName);
    ClusterContext validatedContext = ClusterContext.validateContext(context, commonContext);
    clusterContexts.put(clusterName, validatedContext);
    return validatedContext;
  }

}
