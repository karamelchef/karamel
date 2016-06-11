package se.kth.karamel.backend.honeytap.rules;

import se.kth.honeytap.scaling.rules.Rule;
import se.kth.karamel.common.exception.KaramelException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class GroupModel {

  private String GroupId;
  private int minInstances;
  private int maxInstances;
  private int coolingTimeIn;
  private int coolingTimeOut;
  private HashMap<String, Integer> minReq = new HashMap<>();
  private float reliabilityReq;
  ArrayList<RuleModel> ruleModels = new ArrayList<>();
  ArrayList<Rule> rules = new ArrayList<>();
  //NUMBER_OF_VCPUS, RAM, STORAGE


  public String getGroupId() {
    return GroupId;
  }

  public void setGroupId(String groupId) {
    GroupId = groupId;
  }

  public int getMinInstances() {
    return minInstances;
  }

  public void setMinInstances(int minInstances) {
    this.minInstances = minInstances;
  }

  public int getMaxInstances() {
    return maxInstances;
  }

  public void setMaxInstances(int maxInstances) {
    this.maxInstances = maxInstances;
  }

  public int getCoolingTimeIn() {
    return coolingTimeIn;
  }

  public void setCoolingTimeIn(int coolingTimeIn) {
    this.coolingTimeIn = coolingTimeIn;
  }

  public int getCoolingTimeOut() {
    return coolingTimeOut;
  }

  public void setCoolingTimeOut(int coolingTimeOut) {
    this.coolingTimeOut = coolingTimeOut;
  }

  public HashMap<String, Integer> getMinReq() {
    return minReq;
  }

  public void setMinReq(HashMap<String, Integer> minReq) {
    this.minReq = minReq;
  }

  public float getReliabilityReq() {
    return reliabilityReq;
  }

  public void setReliabilityReq(float reliabilityReq) {
    this.reliabilityReq = reliabilityReq;
  }

  public Rule[] getRules() {
    return rules.toArray(new Rule[rules.size()]);
  }

  public void setRuleModels(ArrayList<RuleModel> ruleModels) {
    for (RuleModel model : ruleModels) {
      if (model.getAction() == 0) {
        //since no action is defined, will not add rule
        continue;
      } else {
        try {
          rules.add(Mapper.getAutoScalingRule(model));
          //ruleModels.add(model);
        } catch (KaramelException e) {
          //TODO-AS check whether we can log error
        }
      }
    }
  }
}
