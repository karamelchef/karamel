/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import java.util.Map;
import java.util.HashMap;
import junit.framework.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author kamal
 */
public class DumpTest {

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
    Map<String, String> server = new HashMap<>();
    server.put("port", "5003");
    server.put("username", "root");
    mysql.put("server", server);
    ndb.put("mysql", mysql);
    
    attrs.put("ndb", ndb);
    cluster.put("attrs", attrs);

    Yaml yaml = new Yaml();
    String output = yaml.dump(cluster);

    System.out.println(output);
  }
}
