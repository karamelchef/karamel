package se.kth.karamel.backend.honeytap.rules;

import se.kth.honeytap.scaling.rules.Rule;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class ClusterRules {

  private String clusterName;
  private ArrayList<GroupModel> groups = new ArrayList<>();

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public ArrayList<GroupModel> getGroups() {
    return groups;
  }

  public void setGroups(ArrayList<GroupModel> groups) {
    this.groups = groups;
  }

  public Rule[] getRulesOfGroup(String groupId) {
    for (GroupModel group : groups) {
      if (group.getGroupId().equals(groupId))
        return group.getRules();
    }
    return new Rule[0];
  }
}
