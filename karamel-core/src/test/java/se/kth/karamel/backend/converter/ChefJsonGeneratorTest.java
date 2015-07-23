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
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.backend.mocking.MockingUtil;

/**
 *
 * @author kamal
 */
public class ChefJsonGeneratorTest {

  @Test
  public void testGenerateClusterChefJsons() throws KaramelException {
    String jsonString = "  {\"name\":\"MySqlCluster\",\"cookbooks\":[{\"name\":\"ndb\",\"attrs\":{\"ndb/DataMemory\":"
        + "\"111\"},\"branch\":\"master\",\"github\":\"hopshadoop/ndb-chef\"}],\"groups\":[{\"name\":\"datanodes\","
        + "\"cookbooks\":[{\"name\":\"ndb\",\"attrs\":{},\"branch\":\"master\",\"github\":\"hopshadoop/ndb-chef\","
        + "\"recipes\":[{\"name\":\"ndb::ndbd\"}]}],\"size\":2,\"provider\":null},{\"name\":\"mgmnodes\",\"cookbooks\":"
        + "[{\"name\":\"ndb\",\"attrs\":{},\"branch\":\"master\",\"github\":\"hopshadoop/ndb-chef\",\"recipes\":"
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
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsons(definition, clusterRuntime);
    JsonObject jsonObject = chefJsons.get("mgmnodes1ndb::mgmd");
    String st = jsonObject.toString();
    Assert.assertTrue(st.contains("\"DataMemory\":\"111\""));
  }

  @Test
  public void testEncodingInAttributes() throws KaramelException {
    //Related to https://github.com/karamelchef/karamel/issues/72
    String jsonString = "  {\"name\":\"MySqlCluster\",\"cookbooks\":[{\"name\":\"ndb\",\"attrs\":{\"ndb/DataMemory\":"
        + "\"1C==\"},\"branch\":\"master\",\"github\":\"hopshadoop/ndb-chef\"}],\"groups\":[{\"name\":\"datanodes\","
        + "\"cookbooks\":[{\"name\":\"ndb\",\"attrs\":{},\"branch\":\"master\",\"github\":\"hopshadoop/ndb-chef\","
        + "\"recipes\":[{\"name\":\"ndb::ndbd\"}]}],\"size\":2,\"provider\":null},{\"name\":\"mgmnodes\",\"cookbooks\":"
        + "[{\"name\":\"ndb\",\"attrs\":{},\"branch\":\"master\",\"github\":\"hopshadoop/ndb-chef\",\"recipes\":"
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
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsons(definition, clusterRuntime);
    JsonObject jsonObject = chefJsons.get("mgmnodes1ndb::mgmd");
    String st = jsonObject.toString();
    Assert.assertTrue(st.contains("\"DataMemory\":\"1C==\""));
  }
}
