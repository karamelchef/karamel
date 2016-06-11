package se.kth.karamel.backend.honeytap.rules;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class RuleModel {
  private String ruleName;
  private String condition;
  private int action;

  public String getRuleName() {
    return ruleName;
  }

  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public int getAction() {
    return action;
  }

  public void setAction(int action) {
    this.action = action;
  }
}
