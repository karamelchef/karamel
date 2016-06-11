package se.kth.karamel.backend.honeytap.rules;

import se.kth.honeytap.scaling.group.Group;
import se.kth.honeytap.scaling.monitoring.RuleSupport;
import se.kth.honeytap.scaling.rules.Rule;
import se.kth.karamel.common.exception.KaramelException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class Mapper {

  private static final String GREATER_THAN = ">";
  private static final String GREATER_THAN_OR_EQUAL = ">=";
  private static final String LESS_THAN = "<";
  private static final String LESS_THAN_OR_EQUAL = "<=";

  private static final String NUMBER_OF_VCPUS = "vcpus";
  private static final String RAM = "RAM";
  private static final String STORAGE = "storage";

  public static Rule getAutoScalingRule(RuleModel ruleModel) throws KaramelException {
    String ruleName = ruleModel.getRuleName();
    String condition = ruleModel.getCondition().trim();
    if (condition.endsWith("%")) {
      condition = condition.substring(0, condition.length() -1);
    }
    Rule rule = createRule(ruleName, condition.split(" "), ruleModel.getAction());
    return rule;
  }

  private static Rule createRule(String ruleName, String[] conditionFractions, int action) throws KaramelException {
    if (ruleName == null || conditionFractions == null || conditionFractions.length != 3) {
      throw new KaramelException("Unable to create the Auto-Scaling rule for rule model: " + ruleName);
    } else {
      try {
        //3 fractions of condition: resource type, comparator, threshold
        RuleSupport.ResourceType resourceType = RuleSupport.ResourceType.valueOf(conditionFractions[0]);
        //TODO-AS convert > to Greater than
        String comparatorString = conditionFractions[1];
        comparatorString = getASComparatorString(comparatorString);

        RuleSupport.Comparator comparator = RuleSupport.Comparator.valueOf(comparatorString);
        float threshold = Float.valueOf(conditionFractions[2]);
        return new Rule(ruleName, resourceType, comparator, threshold, action);
      } catch (Exception e) {
        throw new KaramelException("Unable to create the Auto-Scaling rule for rule model: " + ruleName, e);
      }
    }
  }

  private static String getASComparatorString(String comparator) {
    if (GREATER_THAN.equals(comparator))
      return RuleSupport.Comparator.GREATER_THAN.name();
    else if (GREATER_THAN_OR_EQUAL.equals(comparator))
      return RuleSupport.Comparator.GREATER_THAN_OR_EQUAL.name();
    else if (LESS_THAN.equals(comparator))
      return RuleSupport.Comparator.LESS_THAN.name();
    else if (LESS_THAN_OR_EQUAL.equals(comparator))
      return RuleSupport.Comparator.LESS_THAN_OR_EQUAL.name();
    else
      return comparator;
  }

  public static HashMap<Group.ResourceRequirement, Integer> getASMinReqMap(HashMap<String, Integer> srcMap) {
    HashMap<Group.ResourceRequirement, Integer> minReq = new HashMap<>();

    for (Map.Entry<String, Integer> entry : srcMap.entrySet()) {
      String resourceTypeString = getASResourceReqString(entry.getKey());
      if (resourceTypeString != null) {
        minReq.put(Group.ResourceRequirement.valueOf(resourceTypeString), entry.getValue());
      }
    }
    return minReq;
  }

  private static String getASResourceReqString(String resourceType) {
    if (NUMBER_OF_VCPUS.equals(resourceType))
      return Group.ResourceRequirement.NUMBER_OF_VCPUS.name();
    else if (RAM.equals(resourceType))
      return Group.ResourceRequirement.RAM.name();
    else if (STORAGE.equals(resourceType))
      return Group.ResourceRequirement.STORAGE.name();
    else
      return null;
  }

}
