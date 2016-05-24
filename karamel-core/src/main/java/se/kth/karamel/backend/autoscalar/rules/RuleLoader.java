package se.kth.karamel.backend.autoscalar.rules;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.autoscalar.scaling.rules.Rule;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class RuleLoader {

  private static Map<String, ClusterRules> loadedClustersRules = new HashMap<String, ClusterRules>();

  public static GroupModel getGroupModel(String clusterName, String groupName) throws KaramelException {
    ClusterRules clusterRule = getClusterRule(clusterName);
    for (GroupModel groupModel : clusterRule.getGroups()) {
      if (groupModel.getGroupId().equals(groupName)) {
        return groupModel;
      }
    }
    return null;
  }

  public static Rule[] getRulesOfGroup(String clusterName, String groupName) throws KaramelException {
    ClusterRules clusterRule = getClusterRule(clusterName);
    return clusterRule.getRulesOfGroup(groupName);
  }

  private static ClusterRules getClusterRule(String clusterName) throws KaramelException {
    if (!loadedClustersRules.containsKey(clusterName)) {
      //String ruleFilePath = "basePath".concat(clusterName).concat(".yml");
      String ruleFilePath = Settings.RULES_PATH.concat(File.separator).concat(clusterName).concat(".yml");
      File file = new File(ruleFilePath);
      if (!file.exists()) {
        throw new KaramelException("yaml " + ruleFilePath + " is not available");
      }
      try {
        String ymlString = Files.toString(file, Charsets.UTF_8);
        Yaml yaml = new Yaml(new Constructor(ClusterRules.class));
        ClusterRules rules = (ClusterRules) yaml.load(ymlString);
        loadedClustersRules.put(clusterName, rules);
      } catch (IOException e) {
        throw new KaramelException("Could not load yml at path:  " + ruleFilePath);
      }
    }
    return loadedClustersRules.get(clusterName);
  }
}
