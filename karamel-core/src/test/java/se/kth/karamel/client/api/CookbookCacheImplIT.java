/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.api;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.List;
import org.junit.Ignore;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class CookbookCacheImplIT {
  
  @Ignore
  public void testLoadCookbooks() throws IOException, KaramelException {
    CookbookCacheIml cache = new CookbookCacheIml();
    String ymlString = Resources.toString(Resources.getResource("se/kth/karamel/client/model/test-definitions/hopsworks_compact.yml"), Charsets.UTF_8);
    YamlCluster cluster = ClusterDefinitionService.yamlToYamlObject(ymlString);
    List<KaramelizedCookbook> all = cache.loadAllKaramelizedCookbooks(cluster);
    for (KaramelizedCookbook kc : all) {
      System.out.println(kc.getUrls().id);
    }
  }
}
