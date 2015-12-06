/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import se.kth.karamel.common.exception.MetadataParseException;

/**
 *
 * @author kamal
 */
public final class KaramelFile {

  private ArrayList<KaramelFileYamlDeps> dependencies;

  public KaramelFile(String fileContent) throws MetadataParseException {
    Yaml yaml = new Yaml(new Constructor(KaramelFileYamlRep.class));
    KaramelFileYamlRep file = null;
    try {
      file = (KaramelFileYamlRep) yaml.load(fileContent);
    } catch (YAMLException ex) {
      throw new MetadataParseException(ex.getMessage());
    }
    dependencies = new ArrayList<>();
    dependencies.addAll(file.getDependencies());
  }

  public KaramelFileYamlDeps getDependency(String recipeName) {
    for (KaramelFileYamlDeps yd : dependencies) {
      if (yd.getRecipe().compareToIgnoreCase(recipeName) == 0 || yd.getRecipeCanonicalName().compareToIgnoreCase(
          recipeName) == 0) {
        return yd;
      }
    }
    return null;
  }

  public ArrayList<KaramelFileYamlDeps> getDependencies() {
    return dependencies;
  }

  public void setDependencies(ArrayList<KaramelFileYamlDeps> dependencies) {
    this.dependencies = dependencies;
  }

}
