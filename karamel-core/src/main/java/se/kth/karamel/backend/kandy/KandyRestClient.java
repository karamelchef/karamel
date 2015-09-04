/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.kandy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import se.kth.karamel.backend.stats.ClusterStats;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class KandyRestClient {

  private static ClientConfig config;
  private static Client client;
  private static WebResource storeService;
  private static WebResource updateService;

  private static synchronized void checkResources() {
    if (config == null) {
      config = new DefaultClientConfig();
      client = Client.create(config);
      storeService = client.resource(UriBuilder.fromUri(Settings.KANDY_REST_STATS_STORE).build());
      updateService = client.resource(UriBuilder.fromUri(Settings.KANDY_REST_STATS_UPDATE).build());
    }
  }

  public static void pushClusterStats(ClusterStats stats) throws KaramelException {
    checkResources();
    if (stats.getId() == null) {
      storeNewStat(stats);
    } else {
      updateStat(stats);
    }

  }

  private static void storeNewStat(ClusterStats stats) throws KaramelException {
    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();
    Gson gson = builder.setPrettyPrinting().create();
    String json = gson.toJson(stats);

    ClientResponse response = storeService.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, json);
    String id = response.getEntity(String.class);
    stats.setId(id);
    if (response.getStatus() >= 300) {
      throw new KaramelException(String.format("Kandy server couldn't store the cluster stats because '%s'",
          response.getStatusInfo().getReasonPhrase()));
    }
  }

  private static void updateStat(ClusterStats stats) throws KaramelException {
    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();
    Gson gson = builder.setPrettyPrinting().create();
    String json = gson.toJson(stats);

    ClientResponse response = updateService.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, json);
    if (response.getStatus() >= 300) {
      throw new KaramelException(String.format("Kandy server couldn't store the cluster stats because '%s'",
          response.getStatusInfo().getReasonPhrase()));
    }
  }

}
