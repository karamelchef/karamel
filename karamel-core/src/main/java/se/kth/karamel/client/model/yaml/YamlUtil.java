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
import org.yaml.snakeyaml.scanner.ScannerException;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class YamlUtil {

  /**
   *
   * @param yamlPath fully qualified path in the classpath
   * @return
   * @throws se.kth.karamel.common.exception.KaramelException
   */
  public static YamlCluster loadYamlFileInClassPath(String yamlPath) throws KaramelException {
    String ymlString;
    try {
      ymlString = Resources.toString(Resources.getResource(yamlPath), Charsets.UTF_8);
      return loadCluster(ymlString);
    } catch (IOException ex) {
      throw new KaramelException("couldn't load the yaml", ex);
    }

  }

  /**
   * loads java cluster from yaml string
   *
   * @param ymlString
   * @return
   * @throws se.kth.karamel.common.exception.KaramelException
   */
  public static YamlCluster loadCluster(String ymlString) throws KaramelException {
    try {
      Yaml yaml = new Yaml(new Constructor(YamlCluster.class));
      Object document = yaml.load(ymlString);
      return ((YamlCluster) document);
    } catch (ScannerException ex) {
      throw new KaramelException("Syntax error in the yaml!!", ex);
    }
  }
}
