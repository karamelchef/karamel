/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlDependency;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlKaramelFile;

/**
 * Represents attributes/default.rb file in cookbook
 *
 * @author kamal
 */
public final class KaramelFile {

  private final String fileContent;

  private Map<String, YamlDependency> kv;

  public KaramelFile(String fileContent) {
    this.fileContent = fileContent;
    loadDependencies();
  }

  public void loadDependencies() {
    Yaml yaml = new Yaml(new Constructor(YamlKaramelFile.class));
    YamlKaramelFile file = (YamlKaramelFile) yaml.load(fileContent);
    kv = file.getDependencyMap();
  }

  public YamlDependency getDepenency(String recipeName) {
    return kv.get(recipeName);
  }

}
