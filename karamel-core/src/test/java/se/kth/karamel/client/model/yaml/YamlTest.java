/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import java.io.IOException;
import se.kth.karamel.client.model.Ec2;
import java.util.Map;
import static junit.framework.TestCase.*;
import org.junit.Before;
import org.junit.Test;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.client.model.Baremetal;
import se.kth.karamel.client.model.Cookbook;
import se.kth.karamel.client.model.Gce;
import se.kth.karamel.common.IoUtils;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
//@Ignore
public class YamlTest {

  private YamlCluster cluster;

  @Before
  public void init() throws KaramelException, IOException {
    String yaml = IoUtils.readContentFromClasspath("se/kth/hop/model/reference.yml");
    this.cluster = ClusterDefinitionService.yamlToYamlObject(yaml);
  }

  @Test
  public void testReferenceYaml() throws Exception {
    assertNotNull(cluster);
    assertEquals("ReferenceYaml", cluster.getName());
    assertTrue(cluster.getProvider() instanceof Ec2);
    Ec2 provider = (Ec2) cluster.getProvider();
    assertEquals("m1.small", provider.getType());
    assertEquals("ami-0307ce74", provider.getAmi());
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

}
