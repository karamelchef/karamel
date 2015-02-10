/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.command;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.ClusterManager;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.common.ClasspathResourceUtil;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class CommandService {

  private static String chosenCluster = null;
  private static final ClusterService clusterService = ClusterService.getInstance();
  private static final KaramelApi api = new KaramelApiImpl();
  
  public static String processCommand(String command) {
    String cmd = command.toLowerCase();
    String result = "command not found";
    if (cmd.equals("help")) {
      try {
        result = ClasspathResourceUtil.readContent("se/kth/karamel/backend/command/cheatsheet");
      } catch (IOException ex) {
        result = "sorry, couldn't load the cheatsheet";
      }
    } else if (cmd.equals("clusters")) {
      result = clusters();
    } else if (cmd.equals("yaml")){ 
      if (chosenCluster != null) {
        ClusterManager cluster = cluster(chosenCluster);
        JsonCluster json = cluster.getDefinition();
        try {
          String jsonToYaml = ClusterDefinitionService.jsonToYaml(json);
          result = jsonToYaml;
        } catch (KaramelException ex) {
          result = "sorry couldn't load the yaml";
        }
      }
    } else if (cmd.equals("detail")){ 
      if (chosenCluster != null) {
        ClusterManager cluster = cluster(chosenCluster);
        JsonCluster json = cluster.getDefinition();
        try {
          String jsonToYaml = ClusterDefinitionService.jsonToYaml(json);
          result = jsonToYaml;
        } catch (KaramelException ex) {
          result = "sorry couldn't load the yaml";
        }
      }
    } else {
      Pattern p = Pattern.compile("use\\s+(\\w+)");
      Matcher matcher = p.matcher(cmd);
      if (matcher.matches()) {
        String clusterName = matcher.group(1);
        if (cluster(clusterName) != null) {
          chosenCluster = clusterName;
          result = String.format("switched to %s now", clusterName);
        } else 
          result = String.format("cluster %s is not registered yet!!", clusterName);
      }
    }
    return result;
  }

  private static ClusterManager cluster(String name) {
    Map<String, ClusterManager> repository = clusterService.getRepository();
    Set<Map.Entry<String, ClusterManager>> clusters = repository.entrySet();
    for (Map.Entry<String, ClusterManager> cluster : clusters) {
      if (cluster.getKey().toLowerCase().equals(name.toLowerCase())) {
        return cluster.getValue();
      }
    }
    return null;
  }

  private static String clusters() {
    String result;
    Map<String, ClusterManager> repository = clusterService.getRepository();
    Set<String> keySet = repository.keySet();
    if (!keySet.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (String name : keySet) {
        builder.append(name).append("\n");
      }
      result = builder.toString();
    } else {
      result = "No cluster is registered yet..";
    }
    return result;
  }
}
