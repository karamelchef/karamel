/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlScope;
import se.kth.karamel.common.clusterdef.yaml.YamlGroup;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import se.kth.karamel.common.clusterdef.Ec2;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.core.clusterdef.ClusterDefinitionValidator;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.util.IoUtils;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class ClusterDefinitionTest {

  @Test
  public void testYamlToYamlObject() throws Exception {
    YamlCluster cluster;
    String yaml = IoUtils.readContentFromClasspath("se/kth/karamel/client/model/test-definitions/reference.yml");
    cluster = ClusterDefinitionService.yamlToYamlObject(yaml);
    assertNotNull(cluster);
    assertEquals("ReferenceYaml", cluster.getName());
    assertTrue(cluster.getProvider() instanceof Ec2);
    Ec2 provider = (Ec2) cluster.getProvider();
    assertEquals("m1.small", provider.getType());
    assertEquals("ami-0307ce74", provider.getAmi());
    assertEquals("eu-west-1", provider.getRegion());
    assertEquals("ubuntu", provider.getUsername());
    assertTrue(0.1f == provider.getPrice());

    assertEquals(cluster.getAttr("mysql/user"), "admin");
    assertEquals(cluster.getAttr("ndb/ndbapi/public_ips"), "$ndb.public_ips");
    assertEquals(Lists.newArrayList("123", "134", "145"), cluster.getAttr("hop/ports"));
    assertEquals(cluster.getAttr("hop/dn/http_port"), "50075");
    assertEquals(cluster.getAttr("hop/yarn/ps_port"), "20888");
    assertEquals(cluster.getAttr("hop/rm/http_port"), "8088");
    assertEquals(cluster.getAttr("hop/nm/jmxport"), "8083");
    assertEquals(cluster.getAttr("hop/nm/http_port"), "8042");
    assertEquals(cluster.getAttr("hop/rm/jmxport"), "8042");
    assertEquals(cluster.getAttr("hop/nm/jmxport"), "8083");
    assertEquals(cluster.getAttr("hop/jhs/http_port"), "19888");
    assertEquals(cluster.getAttr("ndb/mgmd/port"), "1186");
    assertEquals(cluster.getAttr("ndb/ndbd/port"), "10000");

    Map<String, Cookbook> cookbooks = cluster.getCookbooks();
    assertTrue(cookbooks.containsKey("kagent"));
    assertEquals("testorg/testrepo", cookbooks.get("kagent").getGithub());
    assertEquals("cookbooks/kagent-chef", cookbooks.get("kagent").getCookbook());
    assertEquals("master", cookbooks.get("kagent").getBranch());
    assertTrue(cookbooks.containsKey("hops"));
    assertEquals("testorg/testrepo", cookbooks.get("hops").getGithub());
    assertEquals("cookbooks/hopshadoop/hops-hadoop-chef", cookbooks.get("hops").getCookbook());
    assertEquals("master", cookbooks.get("hops").getBranch());
    assertTrue(cookbooks.containsKey("hiway"));
    assertEquals("cookbooks/biobankcloud/hiway-chef", cookbooks.get("hiway").getCookbook());
    Map<String, YamlGroup> groups = cluster.getGroups();
    assertTrue(groups.containsKey("dashboard"));
    assertEquals(1, groups.get("dashboard").getSize());
    assertEquals("3306", groups.get("dashboard").getAttr("ndb/mysqld"));
    assertTrue(groups.get("dashboard").getRecipes().contains("kagent"));
    assertTrue(groups.get("dashboard").getRecipes().contains("hopsworks"));
    assertTrue(groups.get("dashboard").getRecipes().contains("ndb::mysqld"));
    assertTrue(groups.containsKey("namenodes"));
    assertEquals(2, groups.get("namenodes").getSize());
    assertTrue(groups.get("namenodes").getRecipes().contains("kagent"));
    assertTrue(groups.get("namenodes").getRecipes().contains("ndb::memcached"));
    assertTrue(groups.get("namenodes").getRecipes().contains("ndb::mysqld"));
    assertTrue(groups.get("namenodes").getRecipes().contains("ndb::mgmd"));
    assertTrue(groups.get("namenodes").getRecipes().contains("hops::nn"));
    assertTrue(groups.get("namenodes").getRecipes().contains("hops::rm"));
    assertTrue(groups.get("namenodes").getRecipes().contains("hops::jhs"));
    assertTrue(groups.get("namenodes").getProvider() instanceof Ec2);
    Ec2 provider2 = (Ec2) groups.get("namenodes").getProvider();
    assertEquals("m3.medium", provider2.getType());
    assertTrue(groups.containsKey("ndb"));
    assertEquals(2, groups.get("ndb").getSize());
    assertTrue(groups.get("ndb").getRecipes().contains("kagent"));
    assertTrue(groups.get("ndb").getRecipes().contains("ndb::ndbd"));
    assertTrue(groups.get("ndb").getProvider() instanceof Baremetal);
    Baremetal provider3 = (Baremetal) groups.get("ndb").getProvider();
    provider3.validate();
    assertEquals("kamal", provider3.getUsername());
    assertEquals(3, provider3.getIps().size());
    assertTrue(provider3.getIps().contains("192.168.33.11-192.168.33.13"));
    assertTrue(provider3.getIps().contains("192.168.33.14"));
    assertTrue(provider3.getIps().contains("192.168.33.15"));
    assertTrue(groups.containsKey("datanodes"));
    assertEquals(4, groups.get("datanodes").getSize());
    assertTrue(groups.get("datanodes").getRecipes().contains("kagent"));
    assertTrue(groups.get("datanodes").getRecipes().contains("hops::dn"));
    assertTrue(groups.get("datanodes").getRecipes().contains("hops::nm"));
    assertTrue(groups.get("datanodes").getProvider() instanceof Ec2);
    Ec2 provider4 = (Ec2) groups.get("datanodes").getProvider();
    assertEquals("m3.medium", provider4.getType());

    Gce provider5 = (Gce) groups.get("gcevms").getProvider();
    assertEquals("n1-standard-1", provider5.getType());
    assertEquals("ubuntu-1404-trusty-v20150316", provider5.getImage());
    assertEquals("europe-west1-b", provider5.getZone());

    Nova provider6 = (Nova) groups.get("novavms").getProvider();
    assertEquals("1", provider6.getFlavor());
    assertEquals("ubuntu-1404", provider6.getImage());
  }

  @Test
  public void testJsonCookbook() throws IOException, KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String yaml = IoUtils.readContentFromClasspath("se/kth/karamel/client/model/test-definitions/reference.yml");
    YamlCluster cluster = ClusterDefinitionService.yamlToYamlObject(yaml);
    Map<String, Cookbook> cookbooks = cluster.getCookbooks();
    assertTrue(cookbooks.containsKey("kagent"));
    Cookbook cookbook = cookbooks.get("kagent");
    JsonCookbook jc = new JsonCookbook(cookbook.getUrls().id, "kagent", new HashMap<String, Object>());
    assertEquals("testorg/testrepo", jc.getKaramelizedCookbook().getUrls().orgRepo);
    assertEquals("cookbooks/kagent-chef", jc.getKaramelizedCookbook().getUrls().cookbookRelPath);
  }

  @Test
  public void foldOutAttrTest() throws MetadataParseException {
    YamlScope yamlScope = new YamlScope() {
    };
    Map<String, Object> attrs = new HashMap<>();
    yamlScope.foldOutAttr("mysql/user", "admin", attrs);
    assertFalse(attrs.isEmpty());
    assertTrue(attrs.size() == 1);
    assertTrue(attrs.get("mysql") instanceof Map);
    Map<String, Object> mysql = (Map<String, Object>) attrs.get("mysql");
    assertTrue(mysql.size() == 1);
    assertTrue(mysql.get("user") instanceof String);
    String user = (String) mysql.get("user");
    assertEquals("admin", user);
  }

  @Test
  public void dumpMap() {
    Map<String, Object> cluster = new HashMap<>();
    Map<String, Object> attrs = new HashMap<>();
    Map<String, Object> ndb = new HashMap<>();
    Map<String, String> nn = new HashMap<>();
    nn.put("jmxport", "8077");
    nn.put("http_port", "50070");
    ndb.put("nn", nn);
    Map<String, Object> mysql = new HashMap<>();
    Map<String, Object> server = new HashMap<>();
    List<String> ports = Lists.newArrayList("5003", "5004", "5005");
    server.put("ports", ports);
    server.put("username", "root");
    mysql.put("server", server);
    ndb.put("mysql", mysql);
    attrs.put("ndb", ndb);
    cluster.put("attrs", attrs);

    Yaml yaml = new Yaml();
    String output = yaml.dump(cluster);
    String expected = "attrs:\n"
        + "  ndb:\n"
        + "    nn: {jmxport: '8077', http_port: '50070'}\n"
        + "    mysql:\n"
        + "      server:\n"
        + "        username: root\n"
        + "        ports: ['5003', '5004', '5005']\n"
        + "";
    assertEquals(expected, output);
  }

  @Test(expected = ValidationException.class)
  public void testInvalidGroupSizeForBaremetal() throws IOException, KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String yaml = IoUtils.readContentFromClasspath("se/kth/karamel/client/model/test-definitions/validations.yml");
    ClusterDefinitionService.yamlToJson(yaml);
  }

  @Test(expected = ValidationException.class)
  public void testDuplciateRecipeInAGroup() throws IOException, KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String yaml = IoUtils.readContentFromClasspath("se/kth/karamel/client/model/test-definitions/validations2.yml");
    ClusterDefinitionService.yamlToJson(yaml);
  }

  @Test
  public void testGroupLevelRecipesInJson() throws KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String json = "  {\"name\":\"flink\","
        + "\"cookbooks\":["
        + "{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/apache-hadoop-chef\","
        + "\"attrs\":{}}],"
        + "\"groups\":["
        + "{\"name\":\"namenodes\","
        + "\"cookbooks\":["
        + "{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/apache-hadoop-chef\","
        + "\"attrs\":{},"
        + "\"recipes\":["
        + "{\"name\":\"hadoop::nn\"}]},"
        + "{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef\","
        + "\"recipes\":["
        + "{\"name\":\"flink\"}]}],"
        + "\"size\":1,"
        + "\"ec2\":null,"
        + "\"gce\":null,"
        + "\"openstack\":null,"
        + "\"baremetal\":{\"username\":null,\"ips\":[\"192.168.33.11\"]}},"
        + "{\"name\":\"datanodes\","
        + "\"cookbooks\":["
        + "{\"id\":\"https://github.com/testorg/testrepo/tree/master/cookbooks/hopshadoop/apache-hadoop-chef\","
        + "\"attrs\":{},"
        + "\"recipes\":[{\"name\":\"hadoop::dn\"}]}],"
        + "\"size\":1,"
        + "\"ec2\":null,"
        + "\"gce\":null,"
        + "\"openstack\":null,"
        + "\"baremetal\":{\"username\":null,\"ips\":[\"192.168.33.12\"]}}],"
        + "\"ec2\":null,"
        + "\"gce\":null,"
        + "\"openstack\":null,"
        + "\"baremetal\":{\"username\":\"vagrant\",\"ips\":[]}}";
    JsonCluster jsonCluster = ClusterDefinitionService.jsonToJsonObject(json);
    ClusterDefinitionValidator.validate(jsonCluster);
    String yml = ClusterDefinitionService.jsonToYaml(jsonCluster);
    jsonCluster = ClusterDefinitionService.yamlToJsonObject(yml);
    List<JsonCookbook> cookbooks = jsonCluster.getCookbooks();
    assertEquals(2, cookbooks.size());
    assertEquals("flink", cookbooks.get(1).getName());
  }
}
