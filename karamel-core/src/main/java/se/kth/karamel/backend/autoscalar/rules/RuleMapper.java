package se.kth.karamel.backend.autoscalar.rules;

import se.kth.autoscalar.scaling.monitoring.RuleSupport;
import se.kth.autoscalar.scaling.rules.Rule;
import se.kth.karamel.common.exception.KaramelException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class RuleMapper {

  private static final String GREATER_THAN = ">";
  private static final String GREATER_THAN_OR_EQUAL = ">=";
  private static final String LESS_THAN = "<";
  private static final String LESS_THAN_OR_EQUAL = "<=";

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

}
