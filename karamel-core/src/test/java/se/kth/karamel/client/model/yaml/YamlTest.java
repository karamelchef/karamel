/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import se.kth.karamel.client.model.Ec2;
import java.io.IOException;
import java.util.Map;
import static junit.framework.TestCase.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.kth.karamel.client.model.Cookbook;

/**
 *
 * @author kamal
 */
//@Ignore
public class YamlTest {

  private YamlCluster cluster;

  @Before
  public void init() throws IOException {
    this.cluster = YamlUtil.loadYamlFileInClassPath("se/kth/hop/model/reference.yml");
  }

  @Test
  public void testReferenceYaml() throws Exception {
    assertNotNull(cluster);
    assertEquals("ReferenceYaml", cluster.getName());
    assertTrue(cluster.getProvider() instanceof Ec2);
    Ec2 provider = (Ec2) cluster.getProvider();
    assertEquals("m1.small", provider.getType());
    assertEquals("ami-0307ce74", provider.getImage());
    assertEquals("eu-west-1", provider.getRegion());
    assertEquals("ubuntu", provider.getUsername());
    assertEquals(0.1f, provider.getPrice());

    assertEquals(cluster.getAttr("mysql/user"), "admin");
    assertEquals(cluster.getAttr("ndb/ndbapi/public_ips"), "$ndb.public_ips");
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
    assertEquals("hopstart/hopagent-chef", cookbooks.get("hopagent").getGithub());
    assertEquals("master", cookbooks.get("hopagent").getBranch());
    assertTrue(cookbooks.containsKey("hop"));
    assertEquals("hopstart/hop-chef", cookbooks.get("hop").getGithub());
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
    assertTrue(groups.get("ndb").getProvider() instanceof Ec2);
    Ec2 provider3 = (Ec2) groups.get("ndb").getProvider();
    assertEquals("m3.medium", provider3.getType());
    assertTrue(groups.containsKey("datanodes"));
    assertEquals(4, groups.get("datanodes").getSize());
    assertTrue(groups.get("datanodes").getRecipes().contains("hopagent"));
    assertTrue(groups.get("datanodes").getRecipes().contains("hop::dn"));
    assertTrue(groups.get("datanodes").getRecipes().contains("hop::nm"));
    assertTrue(groups.get("datanodes").getProvider() instanceof Ec2);
    Ec2 provider4 = (Ec2) groups.get("datanodes").getProvider();
    assertEquals("m3.medium", provider4.getType());
  }

}
