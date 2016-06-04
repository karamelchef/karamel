/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.scanner.ScannerException;
import se.kth.honeytap.scaling.core.HoneyTapAPI;
import se.kth.honeytap.scaling.exceptions.HoneyTapException;
import se.kth.honeytap.scaling.group.Group;
import se.kth.honeytap.scaling.monitoring.MonitoringListener;
import se.kth.honeytap.scaling.rules.Rule;
import se.kth.karamel.backend.honeytap.HoneyTapSimulatorHandler;
import se.kth.karamel.backend.honeytap.rules.GroupModel;
import se.kth.karamel.backend.honeytap.rules.Mapper;
import se.kth.karamel.backend.honeytap.rules.RuleLoader;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.Nova;
import se.kth.karamel.common.clusterdef.Occi;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlGroup;
import se.kth.karamel.common.clusterdef.yaml.YamlPropertyRepresenter;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.FilesystemUtil;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.clusterdef.ClusterDefinitionValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stores/reads cluster definitions from Karamel home folder, does conversions between yaml and json definitions.
 *
 * @author kamal
 */
public class ClusterDefinitionService {

  public static String jsonToYaml(JsonCluster jsonCluster) throws KaramelException {
    YamlCluster yamlCluster = new YamlCluster(jsonCluster);
    DumperOptions options = new DumperOptions();
    options.setIndent(2);
    options.setWidth(120);
    options.setExplicitEnd(false);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setPrettyFlow(true);
    options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    YamlPropertyRepresenter yamlPropertyRepresenter = new YamlPropertyRepresenter();
    yamlPropertyRepresenter.addClassTag(YamlCluster.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Ec2.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Baremetal.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Gce.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Nova.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Occi.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Cookbook.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(YamlGroup.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(HashSet.class, Tag.MAP);
    Yaml yaml = new Yaml(yamlPropertyRepresenter, options);
    String content = yaml.dump(yamlCluster);
    return content;
  }

  public static void saveYaml(String yaml) throws KaramelException {
    try {
      YamlCluster cluster = yamlToYamlObject(yaml);
      String name = cluster.getName().toLowerCase();
      File folder = new File(Settings.CLUSTER_ROOT_PATH(name));
      if (!folder.exists()) {
        folder.mkdirs();
      }
      File file = new File(Settings.CLUSTER_YAML_PATH(name));
      Files.write(yaml, file, Charset.forName("UTF-8"));
    } catch (IOException ex) {
      throw new KaramelException("Could not convert yaml to java ", ex);
    }
  }

  public static String loadYaml(String clusterName) throws KaramelException {
    try {
      String name = clusterName.toLowerCase();
      File folder = new File(Settings.CLUSTER_ROOT_PATH(name));
      if (!folder.exists()) {
        throw new KaramelException(String.format("cluster '%s' is not available", name));
      }
      String yamlPath = Settings.CLUSTER_YAML_PATH(name);
      File file = new File(yamlPath);
      if (!file.exists()) {
        throw new KaramelException(String.format("yaml '%s' is not available", yamlPath));
      }
      String yaml = Files.toString(file, Charsets.UTF_8);
      return yaml;
    } catch (IOException ex) {
      throw new KaramelException("Could not save the yaml ", ex);
    }
  }

  public static void removeDefinition(String clusterName) throws KaramelException {
    String name = clusterName.toLowerCase();
    try {
      FilesystemUtil.deleteRecursive(Settings.CLUSTER_ROOT_PATH(name));
    } catch (FileNotFoundException ex) {
      throw new KaramelException(ex);
    }
  }

  public static List<String> listClusters() throws KaramelException {
    List<String> clusters = new ArrayList();
    File folder = new File(Settings.KARAMEL_ROOT_PATH);
    if (folder.exists()) {
      File[] files = folder.listFiles();
      for (File file : files) {
        if (file.isDirectory()) {
          File[] files2 = file.listFiles();
          for (File file2 : files2) {
            if (file2.isFile() && file2.getName().equals(Settings.YAML_FILE_NAME)) {
              clusters.add(file.getName());
            }
          }
        }
      }
    }
    return clusters;
  }

  public static String jsonToYaml(String json) throws KaramelException {
    Gson gson = new Gson();
    JsonCluster jsonCluster = gson.fromJson(json, JsonCluster.class);
    return jsonToYaml(jsonCluster);
  }

  public static JsonCluster jsonToJsonObject(String json) throws KaramelException {
    Gson gson = new Gson();
    JsonCluster jsonCluster = gson.fromJson(json, JsonCluster.class);
    return jsonCluster;
  }

  public static JsonCluster yamlToJsonObject(String yaml) throws KaramelException {
    YamlCluster cluster = yamlToYamlObject(yaml);
    JsonCluster jsonCluster = new JsonCluster(cluster);
    ClusterDefinitionValidator.validate(jsonCluster);
    return jsonCluster;
  }

  public static YamlCluster yamlToYamlObject(String ymlString) throws KaramelException {
    try {
      Yaml yaml = new Yaml(new Constructor(YamlCluster.class));
      Object document = yaml.load(ymlString);
      return ((YamlCluster) document);
    } catch (ScannerException ex) {
      throw new KaramelException("Syntax error in the yaml!!", ex);
    }
  }

  public static String yamlToJson(String yaml) throws KaramelException {
    JsonCluster jsonObj = yamlToJsonObject(yaml);
    for (JsonGroup group : jsonObj.getGroups()) {
      if (group.getAutoScalingEnabled()) {
        startAutoScalingGroup(group.getName(), UUID.randomUUID().toString(), 1,
                jsonObj.getGroups().size(), jsonObj.getName());
      }
    }
    return serializeJson(jsonObj);
  }

  private static HoneyTapAPI honeyTapAPI;
  private static HoneyTapSimulatorHandler honeyTapSimulatorHandler;
  private static Log log = LogFactory.getLog(ClusterDefinitionService.class);
  private static final Map<String,   MonitoringListener> autoscalerListenersMap = new HashMap<>();
  static boolean asInitSuccessful = false;

  private static void initAutoScaling(int noOfGroups) {
    try {
      if (honeyTapAPI == null) {
        honeyTapAPI = HoneyTapAPI.getInstance();
      }
      if (honeyTapAPI != null && honeyTapSimulatorHandler == null) {
        honeyTapSimulatorHandler = new HoneyTapSimulatorHandler(noOfGroups, honeyTapAPI);
      } else {
        log.error("Could not initiate auto scaling handler");
      }

      if (honeyTapAPI != null && honeyTapSimulatorHandler != null) {
        asInitSuccessful = true;
      }
    } catch (HoneyTapException e) {
      log.fatal("Error while initializing the HoneyTapAPI for group", e);
      return;
    }
  }

  private static void startAutoScalingGroup(String groupName, String groupId, int initialNoOfMachines, int noOfGroups,
                                String clusterName) {
    log.info("################################ going to start auto scaling for group: " + groupName +
            "################################");

    if (!asInitSuccessful) {
      initAutoScaling(noOfGroups);
    }

    if (honeyTapAPI != null) {
      try {
        //TODO-AS create rules and add it to AS
        GroupModel groupModel = RuleLoader.getGroupModel(clusterName, groupName);
        Rule[] rules = groupModel.getRules();
        String[] addedRules = addASRulesForGroup(groupId, rules);
        if (addedRules.length > 0) {
          //TODO-AS get params req to createGroup through the yml
          Map<Group.ResourceRequirement, Integer> minReq = Mapper.getASMinReqMap(groupModel.getMinReq());

          honeyTapAPI.createGroup(groupId, groupModel.getMinInstances(), groupModel.getMaxInstances(),
                  groupModel.getCoolingTimeOut(), groupModel.getCoolingTimeIn(), addedRules, minReq,
                  groupModel.getReliabilityReq());

          MonitoringListener listener = honeyTapAPI.startAutoScaling(groupId, initialNoOfMachines);
          autoscalerListenersMap.put(groupId, listener);
          //auto scalar will invoke monitoring component and subscribe for interested events to give AS suggestions
          honeyTapSimulatorHandler.startHandlingGroup(groupId);
        }
      } catch (HoneyTapException e) {
        log.error("Error while initiating auto-scaling for group: " + groupId, e);
      } catch (KaramelException e) {
        log.error("Error while retrieving rules for the group: " + groupName, e);
      }
    } else {
      log.error("Cannot initiate auto-scaling for group " + groupId + ". HoneyTapAPI has not been " +
              "initialized");
    }
  }

  private static String[] addASRulesForGroup(String groupId, Rule[] rules) {
    ArrayList<String> addedRules = new ArrayList<String>();
    for (Rule rule : rules) {
      try {
        honeyTapAPI.createRule(rule.getRuleName(), rule.getResourceType(), rule.getComparator(), rule.getThreshold(),
                rule.getOperationAction());
        honeyTapAPI.addRuleToGroup(rule.getRuleName(), groupId);
        addedRules.add(rule.getRuleName());
      } catch (HoneyTapException e) {
        log.error("Failed to add rule with name: " + rule.getRuleName());
      }
    }
    return addedRules.toArray(new String[addedRules.size()]);
  }

  public static String serializeJson(JsonCluster jsonCluster) throws KaramelException {
    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();
    Gson gson = builder.setPrettyPrinting().create();
    String json = gson.toJson(jsonCluster);
    return json;
  }
}
