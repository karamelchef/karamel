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
import se.kth.karamel.backend.running.model.ClusterEntity;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.client.model.json.JsonCluster;

/**
 *
 * @author kamal
 */
public class ClusterService {

  private static final Logger logger = Logger.getLogger(ClusterService.class);

  private static final Map<String, ClusterManager> repository = new HashMap<>();

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
    ClusterManager cluster = new ClusterManager(jsonCluster);
    repository.put(clusterName, cluster);
    Thread t = new Thread(cluster);
    t.start();
    cluster.enqueue(ClusterManager.Command.LAUNCH);
  }

  public synchronized void pauseCluster(String clusterName) throws KaramelException {
    logger.info(String.format("User asked for pausing the cluster '%s'", clusterName));
    if (!repository.containsKey(clusterName)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
    ClusterManager cluster = repository.get(clusterName);
    cluster.enqueue(ClusterManager.Command.PAUSE);
  }

  public synchronized void resumeCluster(String clusterName) throws KaramelException {
    logger.info(String.format("User asked for resuming the cluster '%s'", clusterName));
    if (!repository.containsKey(clusterName)) {
      throw new KaramelException(String.format("Repository doesn't contain a cluster name '%s'", clusterName));
    }
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


}
