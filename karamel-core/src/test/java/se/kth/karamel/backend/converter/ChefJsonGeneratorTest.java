/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.converter;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.backend.mocking.MockingUtil;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class ChefJsonGeneratorTest {

  @Test
  public void testGenerateClusterChefJsonsForPurge() throws KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String jsonString = "  {\"name\":\"MySqlCluster\","
        + "\"cookbooks\":[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{\"ndb/DataMemory\":"
        + "\"111\"}}],\"groups\":[{\"name\":\"datanodes\","
        + "\"cookbooks\":[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{},"
        + "\"recipes\":[{\"name\":\"ndb::ndbd\"}]}],\"size\":2,\"provider\":null},{\"name\":\"mgmnodes\",\"cookbooks\":"
        + "[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{},\"recipes\":"
        + "[{\"name\":\"ndb::mgmd\"},{\"name\":\"ndb::mysqld\"},{\"name\":\"ndb::memcached\"}]}],\"size\":"
        + "1,\"provider\":null}],\"ec2\":{\"type\":\"m3.medium\",\"ami\":null,\"region\":\"eu-west-1\",\"price\":"
        + "null,\"vpc\":null,\"subnet\":null}}";
    //Workaround for https://github.com/karamelchef/karamel/issues/28
    String yaml = ClusterDefinitionService.jsonToYaml(jsonString);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(yaml);
//    JsonCluster definition = ClusterDefinitionService.jsonToJsonObject(jsonString);
    List<JsonCookbook> cookbooks = definition.getCookbooks();
    JsonCookbook ndb = null;
    for (JsonCookbook jc : cookbooks) {
      if (jc.getName().equals("ndb")) {
        ndb = jc;
      }
    }
    ClusterRuntime clusterRuntime = MockingUtil.dummyRuntime(definition);
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsonsForPurge(definition, clusterRuntime);
    Assert.assertEquals(3, chefJsons.size());
    Assert.assertNotNull(chefJsons.get("datanodes1ndb::purge"));
    Assert.assertNotNull(chefJsons.get("datanodes2ndb::purge"));
    Assert.assertNotNull(chefJsons.get("mgmnodes1ndb::purge"));
    JsonObject jsonObject = chefJsons.get("mgmnodes1ndb::purge");
    String st = jsonObject.toString();
    Assert.assertTrue(st.contains("\"DataMemory\":\"111\""));
  }

  @Test
  public void testGenerateClusterChefJsonsForInstallation() throws KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String jsonString = "  {\"name\":\"MySqlCluster\","
        + "\"cookbooks\":[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{\"ndb/DataMemory\":"
        + "\"111\"}}],\"groups\":[{\"name\":\"datanodes\","
        + "\"cookbooks\":[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{},"
        + "\"recipes\":[{\"name\":\"ndb::ndbd\"}]}],\"size\":2,\"provider\":null},{\"name\":\"mgmnodes\",\"cookbooks\":"
        + "[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{},\"recipes\":"
        + "[{\"name\":\"ndb::mgmd\"},{\"name\":\"ndb::mysqld\"},{\"name\":\"ndb::memcached\"}]}],\"size\":"
        + "1,\"provider\":null}],\"ec2\":{\"type\":\"m3.medium\",\"ami\":null,\"region\":\"eu-west-1\",\"price\":"
        + "null,\"vpc\":null,\"subnet\":null}}";
    //Workaround for https://github.com/karamelchef/karamel/issues/28
    String yaml = ClusterDefinitionService.jsonToYaml(jsonString);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(yaml);
//    JsonCluster definition = ClusterDefinitionService.jsonToJsonObject(jsonString);
    List<JsonCookbook> cookbooks = definition.getCookbooks();
    JsonCookbook ndb = null;
    for (JsonCookbook jc : cookbooks) {
      if (jc.getName().equals("ndb")) {
        ndb = jc;
      }
    }
    ClusterRuntime clusterRuntime = MockingUtil.dummyRuntime(definition);
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsonsForInstallation(definition, clusterRuntime);
    JsonObject jsonObject = chefJsons.get("mgmnodes1ndb::mgmd");
    String st = jsonObject.toString();
    Assert.assertTrue(st.contains("\"DataMemory\":\"111\""));
  }

  @Test
  public void testEncodingInAttributes() throws KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    //Related to https://github.com/karamelchef/karamel/issues/72
    String jsonString = "  {\"name\":\"MySqlCluster\","
        + "\"cookbooks\":[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{\"ndb/DataMemory\":"
        + "\"1C==\"}}],\"groups\":[{\"name\":\"datanodes\","
        + "\"cookbooks\":[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{},"
        + "\"recipes\":[{\"name\":\"ndb::ndbd\"}]}],\"size\":2,\"provider\":null},{\"name\":\"mgmnodes\",\"cookbooks\":"
        + "[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{},\"recipes\":"
        + "[{\"name\":\"ndb::mgmd\"},{\"name\":\"ndb::mysqld\"},{\"name\":\"ndb::memcached\"}]}],\"size\":"
        + "1,\"provider\":null}],\"ec2\":{\"type\":\"m3.medium\",\"ami\":null,\"region\":\"eu-west-1\",\"price\":"
        + "null,\"vpc\":null,\"subnet\":null}}";

    String yaml = ClusterDefinitionService.jsonToYaml(jsonString);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(yaml);
    List<JsonCookbook> cookbooks = definition.getCookbooks();
    JsonCookbook ndb = null;
    for (JsonCookbook jc : cookbooks) {
      if (jc.getName().equals("flink")) {
        ndb = jc;
      }
    }
    ClusterRuntime clusterRuntime = MockingUtil.dummyRuntime(definition);
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsonsForInstallation(definition, clusterRuntime);
    JsonObject jsonObject = chefJsons.get("mgmnodes1ndb::mgmd");
    String st = jsonObject.toString();
    Assert.assertTrue(st.contains("\"DataMemory\":\"1C==\""));
  }

  @Test
  public void testArrayAttribtuesInChefJsons() throws KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String jsonString = "  {\"name\":\"MySqlCluster\","
        + "\"cookbooks\":[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{\"ndb/DataMemory\":"
        + "\"111\", \"ndb/ports\":[\"123\", \"134\", \"145\"]}}],"
        + "\"groups\":[{\"name\":\"datanodes\","
        + "\"cookbooks\":[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{},"
        + "\"recipes\":[{\"name\":\"ndb::ndbd\"}]}],\"size\":2,\"provider\":null},{\"name\":\"mgmnodes\",\"cookbooks\":"
        + "[{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/ndb-chef\","
        + "\"attrs\":{},\"recipes\":"
        + "[{\"name\":\"ndb::mgmd\"},{\"name\":\"ndb::mysqld\"},{\"name\":\"ndb::memcached\"}]}],\"size\":"
        + "1,\"provider\":null}],\"ec2\":{\"type\":\"m3.medium\",\"ami\":null,\"region\":\"eu-west-1\",\"price\":"
        + "null,\"vpc\":null,\"subnet\":null}}";
    String yaml = ClusterDefinitionService.jsonToYaml(jsonString);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(yaml);
    ClusterRuntime clusterRuntime = MockingUtil.dummyRuntime(definition);
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsonsForInstallation(definition, clusterRuntime);
    JsonObject jsonObject = chefJsons.get("mgmnodes1ndb::mgmd");
    String st = jsonObject.toString();
    Assert.assertTrue(st.contains("\"DataMemory\":\"111\""));
    Assert.assertTrue(st.contains("\"ports\":[\"123\",\"134\",\"145\"]"));
  }
}
