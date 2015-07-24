/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import java.util.ArrayList;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlDependency;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlKaramelFile;

/**
 *
 * @author kamal
 */
public final class KaramelFile {

  private ArrayList<YamlDependency> dependencies;

  public KaramelFile(String fileContent) throws MetadataParseException {
    Yaml yaml = new Yaml(new Constructor(YamlKaramelFile.class));
    YamlKaramelFile file = null;
    try {
      file = (YamlKaramelFile) yaml.load(fileContent);
    } catch (YAMLException ex) {
      throw new MetadataParseException(ex.getMessage());
    }
//    dependencies = file.getDependencyMap();
    dependencies = new ArrayList<>();
    dependencies.addAll(file.getDependencies());
  }

  public YamlDependency getDependency(String recipeName) {
    for (YamlDependency yd : dependencies) {
      if (yd.getRecipe().compareToIgnoreCase(recipeName) == 0 || yd.getRecipeCanonicalName().compareToIgnoreCase(
          recipeName) == 0) {
        return yd;
      }
    }
//    return dependencies.get(recipeName);
    return null;
  }

//  public void setDependency(String recipeName, YamlDependency yd) {
//    dependencies.put(recipeName, yd);
//  }
//  public Map<String, YamlDependency> getDependencies() {
//    return dependencies;
//  }
//
//  public void setDependencies(Map<String, YamlDependency> dependencies) {
//    this.dependencies = dependencies;
//  }
  public ArrayList<YamlDependency> getDependencies() {
    return dependencies;
  }

  public void setDependencies(ArrayList<YamlDependency> dependencies) {
    this.dependencies = dependencies;
  }

}
