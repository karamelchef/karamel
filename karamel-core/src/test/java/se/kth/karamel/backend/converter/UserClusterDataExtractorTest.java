/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.converter;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.backend.mocking.MockingUtil;

/**
 *
 * @author kamal
 */
public class UserClusterDataExtractorTest {

  @Test
  public void clusterLinksTest() throws IOException, KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/hopsworks.yml"), Charsets.UTF_8);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(ymlString);
    String links = UserClusterDataExtractor.clusterLinks(definition, null);
    String expected = "Visit <a target='_blank' href='http://www.hops.io/'>Hop's Website</a> or <a target='_blank' "
        + "href='http://www.karamel.io/'>Karamel's Website</a>";
    Assert.assertEquals(expected, links.trim());
    ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(definition);
    links = UserClusterDataExtractor.clusterLinks(definition, dummyRuntime);
    expected = "Click <a target='_blank' href='https://hopsworks1:8181/hop-dashboard'>here</a> to launch hopsworks in your browser\n"
        + "Visit <a target='_blank' href='http://www.hops.io/'>Hop's Website</a> or <a target='_blank' href='http://www.karamel.io/'>Karamel's Website</a>\n"
        + "";
    Assert.assertEquals(expected, links);
  }

}
