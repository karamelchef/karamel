package se.kth.karamel.backend.autoscalar.rules;


import se.kth.autoscalar.scaling.rules.Rule;
import se.kth.karamel.common.exception.KaramelException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class ClusterGroupRulesBk {

  private String clusterName;
  private Map<String, ArrayList<RuleModel>> ruleGroups = new HashMap<String, ArrayList<RuleModel>>();

  private Map<String, ArrayList<Rule>> ruleMapping = new HashMap<String, ArrayList<Rule>>();
  //private HashMap<String, HashMap<String,RuleModel>> ruleGroups = new HashMap<String, HashMap<String, RuleModel>>();
  //new Yaml(new Constructor(ClusterGroupRules.class)).load(Files.toString(new File(
  // "/Users/ashansa/HS/thesis/karamel-source/karamel-core/test.yml"), Charsets.UTF_8))
  public ClusterGroupRulesBk() {
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

 /* public Map<String, HashMap<String,RuleModel>> getRuleGroups() {
    return ruleGroups;
  }

  public void setRuleGroups(HashMap<String, HashMap<String,RuleModel>> ruleGroups) {
    this.ruleGroups = ruleGroups;
  }*/

  /*public Map<String, ArrayList<RuleModel>> getRuleGroups() {
    return ruleGroups;
  }*/

  public void setRuleGroups(Map<String, ArrayList<RuleModel>> ruleGroups) {
    for (String groupName : ruleGroups.keySet()) {
      setRules(groupName, ruleGroups.get(groupName));
    }
  }

  /**
   * This will replace the ruleModels in the map with the newly given rule list
   * @param groupName
   * @param ruleModels
   */
  public void setRules(String groupName, ArrayList<RuleModel> ruleModels) {
    ArrayList<Rule> rules = new ArrayList<Rule>();
    for (RuleModel model : ruleModels) {
      if (model.getAction() == 0) {
        //since no action is defined, will not add rule
        continue;
      } else {
        try {
          rules.add(Mapper.getAutoScalingRule(model));
        } catch (KaramelException e) {
          //TODO-AS check whether we can log error
        }
      }
    }
    ruleMapping.put(groupName, rules);
  }

  private void getResourceType(String resourceType) {

  }

  public Rule[] getRulesOfGroup(String groupName) {
    ArrayList<Rule> rules = ruleMapping.get(groupName);
    return rules.toArray(new Rule[rules.size()]);
  }
}
