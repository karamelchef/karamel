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
import org.junit.Test;
import se.kth.karamel.backend.command.CommandResponse;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.SshKeyPair;

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
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/spark.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    assertFalse(json.isEmpty());
    System.out.println(json);
  }

//  @Test
  public void testJsonToYaml() throws KaramelException, IOException {
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/spark.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    String convertedYaml = api.jsonToYaml(json);
    assertFalse(convertedYaml.isEmpty());
    System.out.println(convertedYaml);
  }

  @Test
  public void testEndToEnd() throws KaramelException, IOException, InterruptedException {
    String clusterName = "spark";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/sparkonvpc.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
//    System.out.println(json);
//    System.out.println("===================================================");
    SshKeyPair sshKeys = api.loadSshKeysIfExist();
    if (sshKeys == null)
      sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
    api.registerSshKeys(sshKeys);
    Ec2Credentials credentials = api.loadEc2CredentialsIfExist();
    api.updateEc2CredentialsIfValid(credentials);
    
//    api.registerSshKeys(clusterName, keypair);
//    api.updateEc2CredentialsIfValid("aaa", confs.getProperty(Settings.EC2_ACCESSKEY_KEY));
    api.startCluster(json);
//    api.processCommand("use hiway");
    long ms1 = System.currentTimeMillis();
    int mins = 0;
    while (ms1 + 6000000 > System.currentTimeMillis()) {
      mins ++;
      CommandResponse response = api.processCommand("status");

      System.out.println(response.getResult());
//      if (mins == 3)
//        api.processCommand("purge");
//      if (mins == 6)
//        api.processCommand("resume");
//      if (mins == 8)
//        api.processCommand("purge");
      Thread.currentThread().sleep(60000);
    }
  }

}
