/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author kamal
 */
public class YamlUtil {

  /**
   *
   * @param yamlPath fully qualified path in the classpath
   * @return
   * @throws IOException
   */
  public static YamlCluster loadYamlFileInClassPath(String yamlPath) throws IOException {
    String ymlString = Resources.toString(Resources.getResource(yamlPath), Charsets.UTF_8);
    return loadCluster(ymlString);
  }

  /**
   * loads java cluster from yaml string
   * @param ymlString
   * @return
   * @throws IOException 
   */
  public static YamlCluster loadCluster(String ymlString) throws IOException {
    Yaml yaml = new Yaml(new Constructor(YamlCluster.class));
    Object document = yaml.load(ymlString);
    return ((YamlCluster) document);
  }
}
