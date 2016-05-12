package se.kth.karamel.backend.autoscalar.rules;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.autoscalar.scaling.rules.Rule;
import se.kth.karamel.common.exception.KaramelException;

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

  private static Map<String, ClusterASRules> loadedClustersRules = new HashMap<String, ClusterASRules>();

  public static Rule[] getRulesOfGroup(String clusterName, String groupName) throws KaramelException {
    if (!loadedClustersRules.containsKey(clusterName)) {
      //String yamlPath = "basePath".concat(clusterName).concat(".yml");
      String yamlPath = "/Users/ashansa/HS/thesis/karamel-source/karamel-core/".concat(clusterName).concat(".yml");
      File file = new File(yamlPath);
      if (!file.exists()) {
        throw new KaramelException("yaml " + yamlPath + " is not available");
      }
      try {
        String ymlString = Files.toString(file, Charsets.UTF_8);
        Yaml yaml = new Yaml(new Constructor(ClusterASRules.class));
        ClusterASRules rules = (ClusterASRules) yaml.load(ymlString);
        loadedClustersRules.put(clusterName, rules);
      } catch (IOException e) {
        throw new KaramelException("Could not load yml at path:  " + yamlPath);
      }
    }
    return loadedClustersRules.get(clusterName).getRulesOfGroup(groupName);
  }
}
