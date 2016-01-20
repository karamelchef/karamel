/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.kandy;

import com.google.common.io.Files;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class KandyRestClient {

  private static final Logger logger = Logger.getLogger(KandyRestClient.class);
  private static ClientConfig config;
  private static Client client;
  private static WebResource storeService;
  private static WebResource costService;

  private static synchronized void checkResources() {
    try {
      if (config == null) {
        config = new DefaultClientConfig();
        client = Client.create(config);
        storeService = client.resource(UriBuilder.fromUri(Settings.KANDY_REST_STATS_STORE).build());
        costService = client.resource(UriBuilder.fromUri(Settings.KANDY_REST_CLUSTER_COST).build());
      }
    } catch (Exception e) {
      logger.debug("exception during intitalizing the KandyClient", e);
    }
  }

  public static void pushClusterStats(String clusterName, ClusterStats stats) {
    checkResources();
    if (stats.getId() == null) {
      storeNewStat(stats);
    } else {
      updateStat(stats);
    }
    storeLocally(clusterName, stats);
  }

  private static void storeLocally(String clusterName, ClusterStats stats) {
    String json = stats.toJsonAndMarkNotUpdated();
    try {
      String name = clusterName.toLowerCase();
      File folder = new File(Settings.CLUSTER_STATS_FOLDER(name));
      if (!folder.exists()) {
        folder.mkdirs();
      }
      File file = new File(Settings.CLUSTER_STATS_PATH(name, stats.getLocalId()));
      if (file.exists()) {
        file.delete();
      }
      Files.write(json, file, Charset.forName("UTF-8"));
    } catch (IOException ex) {
      logger.error("Could not save cluster stats locally " + ex.getMessage());
    }
  }

  private static void storeNewStat(ClusterStats stats) {
    try {
      String json = stats.toJsonAndMarkNotUpdated();
      ClientResponse response = storeService.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, json);
      if (response.getStatus() >= 300) {
        logger.error(String.format("Kandy server couldn't store the cluster stats because '%s'",
            response.getStatusInfo().getReasonPhrase()));
      } else {
        String id = response.getEntity(String.class);
        stats.setId(id);
        logger.debug(String.format("Cluster status is stored for the first time in Kandy with id %s", id));
      }
    } catch (Exception e) {
      logger.debug("exception during storing cluster stats to Kandy: " + e.getMessage());
    }
  }

  private static void updateStat(ClusterStats stats) {
    try {
      String json = stats.toJsonAndMarkNotUpdated();
      WebResource updateService
          = client.resource(UriBuilder.fromUri(Settings.KANDY_REST_STATS_UPDATE(stats.getId())).build());

      ClientResponse response = updateService.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, json);

      if (response.getStatus()
          >= 300) {
        logger.debug(String.format("Kandy server couldn't store the cluster stats because '%s'",
            response.getStatusInfo().getReasonPhrase()));
      } else
      logger.debug(String.format("Cluster status is updated in Kandy with id %s", stats.getId()));
    } catch (Exception e) {
      logger.debug("exception during updatinig cluster stats to Kandy: " + e.getMessage());
    }
  }

  public static String estimateCost(String clusterDef) throws KaramelException {
    try {
      checkResources();
      ClientResponse response = costService.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, clusterDef);
      if (response.getStatus() >= 300) {
        logger.error(String.format("Kandy server couldn't return the cluster cost because '%s'",
            response.getStatusInfo().getReasonPhrase()));
      } else {
        String cost = response.getEntity(String.class);
        return cost;
      }
    } catch (Exception e) {
      logger.error("exception during calling cost estimation to Kandy: " + e.getMessage());
      throw new KaramelException(e.getMessage());
    }
    return null;
  }

}
