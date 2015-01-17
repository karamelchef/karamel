/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata.karamelfile.yaml;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author kamal
 */
public class TestYamlKaramelFile {

  @Test
  public void testDeserialize() throws IOException {
    String ymlString = Resources.toString(Resources.getResource("se/kth/karamel/cookbook/metadata/karamelfile/yaml/KaramelFile"), Charsets.UTF_8);
    Yaml yaml = new Yaml(new Constructor(YamlKaramelFile.class));
    YamlKaramelFile file = (YamlKaramelFile) yaml.load(ymlString);
    Assert.assertNotNull(file);
  }
}
