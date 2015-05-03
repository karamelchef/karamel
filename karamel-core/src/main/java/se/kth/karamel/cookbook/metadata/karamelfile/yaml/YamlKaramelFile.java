/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata.karamelfile.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kamal
 */
public class YamlKaramelFile {

  private List<YamlDependency> dependencies = new ArrayList<>();

  public List<YamlDependency> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<YamlDependency> dependencies) {
    if (dependencies != null) {
      this.dependencies = dependencies;
    }
  }

  public Map<String, YamlDependency> getDependencyMap() {
    Map<String, YamlDependency> map = new HashMap<>();
    for (YamlDependency yamlDependency : dependencies) {
      map.put(yamlDependency.getRecipeCanonicalName(), yamlDependency);
    }
    return map;
  }

}
