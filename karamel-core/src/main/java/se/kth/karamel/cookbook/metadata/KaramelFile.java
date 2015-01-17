/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlDependency;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlKaramelFile;

/**
 * Represents attributes/default.rb file in cookbook
 *
 * @author kamal
 */
public final class KaramelFile {

  private final String url;

  private Map<String, YamlDependency> kv;

  public KaramelFile(String rawFileUrl) throws CookbookUrlException {
    this.url = rawFileUrl;
    loadDependencies();
  }

  public void loadDependencies() throws CookbookUrlException {
    URL fileUrl;
    try {
      fileUrl = new URL(url);
      String ymlString = Resources.toString(fileUrl, Charset.forName("UTF-8"));
      Yaml yaml = new Yaml(new Constructor(YamlKaramelFile.class));
      YamlKaramelFile file = (YamlKaramelFile)yaml.load(ymlString);
      kv = file.getDependencyMap();
    } catch (MalformedURLException ex) {
      throw new CookbookUrlException("Karamelfile url is malford " + url, ex);
    } catch (IOException ex) {
      throw new CookbookUrlException("Cannot parse the Karamelfile " + url, ex);
    }

  }

  public YamlDependency getDepenency(String recipeName) {
    return kv.get(recipeName);
  }

}
