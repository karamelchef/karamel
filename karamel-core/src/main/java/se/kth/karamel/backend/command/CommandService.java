/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.command;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.ClusterManager;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.running.model.ClusterEntity;
import se.kth.karamel.backend.running.model.GroupEntity;
import se.kth.karamel.backend.running.model.MachineEntity;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.common.ClasspathResourceUtil;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class CommandService {

  private static String chosenCluster = null;
  private static final ClusterService clusterService = ClusterService.getInstance();
//  private static final KaramelApi api = new KaramelApiImpl();

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
    } else if (cmd.equals("yaml")) {
      if (chosenCluster != null) {
        ClusterManager cluster = cluster(chosenCluster);
        JsonCluster json = cluster.getDefinition();
        try {
          result = ClusterDefinitionService.jsonToYaml(json);
        } catch (KaramelException ex) {
          result = "sorry couldn't load the yaml";
        }
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("status")) {
      if (chosenCluster != null) {
        StringBuilder builder = new StringBuilder();
        ClusterManager cluster = cluster(chosenCluster);
        ClusterEntity clusterEntity = cluster.getRuntime();
        builder.append("Name:\t").append(clusterEntity.getName()).append("\n")
                .append("Phase:\t").append(clusterEntity.getPhase()).append("\n")
                .append("Failed:\t").append(clusterEntity.isFailed()).append("\n")
                .append("Paused:\t").append(clusterEntity.isPaused());
        result = builder.toString();
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("detail")) {
      if (chosenCluster != null) {
        ClusterManager cluster = cluster(chosenCluster);
        JsonCluster json = cluster.getDefinition();

        try {
          result = ClusterDefinitionService.serializeJson(json);
        } catch (KaramelException ex) {
          result = "sorry couldn't load the yaml";
        }
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("groups")) {
      if (chosenCluster != null) {
        StringBuilder builder = new StringBuilder();
        ClusterManager cluster = cluster(chosenCluster);
        ClusterEntity clusterEntity = cluster.getRuntime();
        builder.append("\t").append("Name").append("\t|\t").append("Phase").append("\t|\t").append("Failed").append("\n");
        for (GroupEntity group : clusterEntity.getGroups()) {
          builder.append("\t").append(group.getName()).append("\t|\t").append(group.getPhase()).append("\t|\t").append(group.isFailed()).append("\n");
        }
        result = builder.toString();
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else if (cmd.equals("machines")) {
      if (chosenCluster != null) {
        if (chosenCluster != null) {
          StringBuilder builder = new StringBuilder();
          ClusterManager cluster = cluster(chosenCluster);
          ClusterEntity clusterEntity = cluster.getRuntime();
          builder.append("\t").append("Group").append("\t|\t").append("Public IP").append("\t|\t").append("Private IP").append("\t|\t").append("SSH Port").append("\t|\t").append("SSH User").append("\t|\t").append("Life Status").append("\t|\t").append("Task Status").append("\n");
          for (GroupEntity group : clusterEntity.getGroups()) {
            for (MachineEntity machine : group.getMachines()) {
              builder.append("\t").append(group.getName()).append("\t|\t").append(machine.getPublicIp()).append("\t|\t").append(machine.getPrivateIp()).append("\t|\t").append(machine.getSshPort()).append("\t|\t").append(machine.getSshUser()).append("\t|\t").append(machine.getLifeStatus()).append("\t|\t").append(machine.getTasksStatus()).append("\n");
            }
          }
          result = builder.toString();
        } else {
          result = "no cluster has been chosen yet!!";
        }
      } else {
        result = "no cluster has been chosen yet!!";
      }
    } else {
      boolean found = false;
      Pattern p = Pattern.compile("use\\s+(\\w+)");
      Matcher matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        if (cluster(clusterName) != null) {
          chosenCluster = clusterName;
          result = String.format("switched to %s now", clusterName);
        } else {
          result = String.format("cluster %s is not registered yet!!", clusterName);
        }
      }

      p = Pattern.compile("which\\s+(cluster|ec2|ssh)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String subcmd = matcher.group(1);
        if (subcmd.equals("cluster")) {
          if (chosenCluster != null) {
            result = String.format("%s has been chosen.", chosenCluster);
          } else {
            result = "no cluster has been chosen yet!!";
          }
        } else if (subcmd.equals("ec2")) {
          Ec2Context ec2Context = clusterService.getCommonContext().getEc2Context();
          if (ec2Context != null) {
            result = String.format("ec2 account id is %s", ec2Context.getCredentials().getAccountId());
          } else {
            result = "no ec2 account has been chosen yet!!";
          }
        } else if (subcmd.equals("ssh")) {
          SshKeyPair sshKeyPair = clusterService.getCommonContext().getSshKeyPair();
          if (sshKeyPair != null) {
            result = String.format("public key path: %s \nprivate key path: %s", sshKeyPair.getPublicKeyPath(), sshKeyPair.getPrivateKeyPath());
          } else {
            result = "no ssh keys has been chosen yet!!";
          }
        }
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
