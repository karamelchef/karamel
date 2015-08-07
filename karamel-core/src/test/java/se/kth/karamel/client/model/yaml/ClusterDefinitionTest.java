/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import se.kth.karamel.client.model.Ec2;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.client.model.Baremetal;
import se.kth.karamel.client.model.Cookbook;
import se.kth.karamel.client.model.Gce;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonCookbook;
import se.kth.karamel.common.IoUtils;
import se.kth.karamel.common.Settings;
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
    String yaml = IoUtils.readContentFromClasspath("se/kth/hop/model/reference.yml");
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
    assertTrue(cookbooks.containsKey("hopagent"));
    assertEquals("hopstart/test-repo", cookbooks.get("hopagent").getGithub());
    assertEquals("hopagent-chef", cookbooks.get("hopagent").getCookbook());
    assertEquals("master", cookbooks.get("hopagent").getBranch());
    assertTrue(cookbooks.containsKey("hop"));
    assertEquals("hopstart/hop-chef", cookbooks.get("hop").getGithub());
    assertNull(cookbooks.get("hop").getCookbook());
    assertEquals("master", cookbooks.get("hop").getBranch());
    assertTrue(cookbooks.containsKey("cuneiform"));
    assertEquals("biobankcloud/cuneiform-chef", cookbooks.get("cuneiform").getGithub());
    Map<String, YamlGroup> groups = cluster.getGroups();
    assertTrue(groups.containsKey("dashboard"));
    assertEquals(1, groups.get("dashboard").getSize());
    assertEquals("3306", groups.get("dashboard").getAttr("ndb/mysqld"));
    assertTrue(groups.get("dashboard").getRecipes().contains("hopagent"));
    assertTrue(groups.get("dashboard").getRecipes().contains("hopdashboard"));
    assertTrue(groups.get("dashboard").getRecipes().contains("ndb::mysqld"));
    assertTrue(groups.containsKey("namenodes"));
    assertEquals(2, groups.get("namenodes").getSize());
    assertTrue(groups.get("namenodes").getRecipes().contains("hopagent"));
    assertTrue(groups.get("namenodes").getRecipes().contains("ndb::memcached"));
    assertTrue(groups.get("namenodes").getRecipes().contains("ndb::mysqld"));
    assertTrue(groups.get("namenodes").getRecipes().contains("ndb::mgmd"));
    assertTrue(groups.get("namenodes").getRecipes().contains("hop::nn"));
    assertTrue(groups.get("namenodes").getRecipes().contains("hop::rm"));
    assertTrue(groups.get("namenodes").getRecipes().contains("hop::jhs"));
    assertTrue(groups.get("namenodes").getProvider() instanceof Ec2);
    Ec2 provider2 = (Ec2) groups.get("namenodes").getProvider();
    assertEquals("m3.medium", provider2.getType());
    assertTrue(groups.containsKey("ndb"));
    assertEquals(2, groups.get("ndb").getSize());
    assertTrue(groups.get("ndb").getRecipes().contains("hopagent"));
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
    assertTrue(groups.get("datanodes").getRecipes().contains("hopagent"));
    assertTrue(groups.get("datanodes").getRecipes().contains("hop::dn"));
    assertTrue(groups.get("datanodes").getRecipes().contains("hop::nm"));
    assertTrue(groups.get("datanodes").getProvider() instanceof Ec2);
    Ec2 provider4 = (Ec2) groups.get("datanodes").getProvider();
    assertEquals("m3.medium", provider4.getType());

    Gce provider5 = (Gce) groups.get("gcevms").getProvider();
    assertEquals("n1-standard-1", provider5.getType());
    assertEquals("ubuntu-1404-trusty-v20150316", provider5.getImage());
    assertEquals("europe-west1-b", provider5.getZone());
  }

  @Test
  public void testJsonCookbook() throws IOException, KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String yaml = IoUtils.readContentFromClasspath("se/kth/hop/model/reference.yml");
    YamlCluster cluster = ClusterDefinitionService.yamlToYamlObject(yaml);
    Map<String, Cookbook> cookbooks = cluster.getCookbooks();
    assertTrue(cookbooks.containsKey("hopagent"));
    Cookbook cookbook = cookbooks.get("hopagent");
    JsonCookbook jc = new JsonCookbook(cookbook, "hopagent", new HashMap<String, Object>());
    assertEquals("hopstart/test-repo", jc.getGithub());
    assertEquals("hopagent-chef", jc.getCookbook());
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
    String yaml = IoUtils.readContentFromClasspath("se/kth/hop/model/validations.yml");
    ClusterDefinitionService.yamlToJson(yaml);
  }

}
