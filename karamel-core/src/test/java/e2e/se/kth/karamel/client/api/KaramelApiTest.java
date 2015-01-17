/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package e2e.se.kth.karamel.client.api;

import se.kth.karamel.common.exception.KaramelException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
//@Ignore
public class KaramelApiTest {

  KaramelApi api = new KaramelApiImpl();

//  @Test
  public void testGetCookbookDetails() throws KaramelException {
    String json = api.getCookbookDetails("https://github.com/hopstart/hadoop-chef", false);
    assertFalse(json.isEmpty());
  }

//  @Test
  public void testYamlToJson() throws IOException, KaramelException {
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/reference.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    assertFalse(json.isEmpty());
    System.out.println(json);
  }

//  @Test
  public void testJsonToYaml() throws KaramelException, IOException {
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/reference.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    String convertedYaml = api.jsonToYaml(json);
    assertFalse(convertedYaml.isEmpty());
    System.out.println(convertedYaml);
  }

  @Test
  public void testEndToEnd() throws KaramelException, IOException, InterruptedException {
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/hopshub.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
//    System.out.println(json);
//    System.out.println("===================================================");
    Confs confs = Confs.loadEc2Confs();
    api.updateEc2CredentialsIfValid(confs.getProperty(Settings.EC2_ACCOUNT_ID_KEY), confs.getProperty(Settings.EC2_ACCESSKEY_KEY));
//    api.updateEc2CredentialsIfValid("aaa", confs.getProperty(Settings.EC2_ACCESSKEY_KEY));
    api.startCluster(json);
    long ms1 = System.currentTimeMillis();
    while (ms1 + 6000000 > System.currentTimeMillis()) {
      String clusterStatus = api.getClusterStatus("HopsHub");

      System.out.println(clusterStatus);
      Thread.currentThread().sleep(60000);
    }
  }

}
