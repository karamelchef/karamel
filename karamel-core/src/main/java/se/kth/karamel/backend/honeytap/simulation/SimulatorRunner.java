package se.kth.karamel.backend.honeytap.simulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kth.honeytap.scaling.core.HoneyTapAPI;
import se.kth.honeytap.scaling.exceptions.HoneyTapException;
import se.kth.honeytap.scaling.group.Group;
import se.kth.honeytap.scaling.monitoring.MonitoringListener;
import se.kth.honeytap.scaling.rules.Rule;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.honeytap.HoneyTapSimulatorHandler;
import se.kth.karamel.backend.honeytap.rules.GroupModel;
import se.kth.karamel.backend.honeytap.rules.Mapper;
import se.kth.karamel.backend.honeytap.rules.RuleLoader;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.IoUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class SimulatorRunner {
  public static void main(String[] args) {
    try {
      String yaml = IoUtils.readContentFromClasspath("se/kth/karamel/backend/honeytap/simulator/hadoop.yml");
      JsonCluster cluster = ClusterDefinitionService.yamlToJsonObject(yaml);
      for (JsonGroup group : cluster.getGroups()) {
        if (group.getAutoscale()) {
          startAutoScalingGroup(group.getName(), UUID.randomUUID().toString(), 1,
                  cluster.getGroups().size(), cluster.getName());
        }
      }
    } catch (KaramelException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static HoneyTapAPI honeyTapAPI;
  private static HoneyTapSimulatorHandler honeyTapSimulatorHandler;
  private static Log log = LogFactory.getLog(ClusterDefinitionService.class);
  private static final Map<String,   MonitoringListener> autoscalerListenersMap = new HashMap<>();
  static boolean asInitSuccessful = false;

  private static void initAutoScaling(int noOfGroups) {
    try {
      if (honeyTapAPI == null) {
        honeyTapAPI = new HoneyTapAPI();
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

}
