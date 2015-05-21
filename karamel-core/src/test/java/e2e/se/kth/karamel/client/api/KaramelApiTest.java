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
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.running.model.ClusterRuntime;
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
  
  @Test
  public void dummyTest() {
    //just that we dont need to ignore this class all the time
  }

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

//  @Test
  public void testPauseResumePurge() throws KaramelException, IOException, InterruptedException {
    String clusterName = "spark";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/spark.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    SshKeyPair sshKeys = api.loadSshKeysIfExist("");
    if (sshKeys == null) {
      sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
    }
    api.registerSshKeys(sshKeys);
    Ec2Credentials credentials = api.loadEc2CredentialsIfExist();
    api.updateEc2CredentialsIfValid(credentials);
    api.startCluster(json);
    long ms1 = System.currentTimeMillis();
    int mins = 0;
    while (ms1 + 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
      mins++;
      System.out.println(api.processCommand("status").getResult());
      if (mins == 3) {
        api.processCommand("pause");
        System.out.println(api.processCommand("status").getResult());
      }
      if (mins == 5) {
        api.processCommand("resume");
        System.out.println(api.processCommand("status").getResult());
      }
      if (mins == 7) {
        api.processCommand("purge");
        System.out.println(api.processCommand("status").getResult());
      }
      Thread.currentThread().sleep(60000);
    }
  }

//  @Test
  public void testForkMachineScale() throws KaramelException, IOException, InterruptedException {
    String clusterName = "bigspark";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/bigspark.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    SshKeyPair sshKeys = api.loadSshKeysIfExist("");
    if (sshKeys == null) {
      sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
    }
    api.registerSshKeys(sshKeys);
    Ec2Credentials credentials = api.loadEc2CredentialsIfExist();
    api.updateEc2CredentialsIfValid(credentials);
    api.startCluster(json);
    long ms1 = System.currentTimeMillis();
    int mins = 0;
    while (ms1 + 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
      mins++;
      ClusterRuntime clusterRuntime = ClusterService.getInstance().clusterStatus(clusterName);
      if (clusterRuntime.getPhase().ordinal() > ClusterRuntime.ClusterPhases.FORKING_MACHINES.ordinal()) {
        api.processCommand("purge " + clusterName);
      }
      System.out.println(api.processCommand("machines").getResult());
      Thread.currentThread().sleep(60000);
    }
  }

//  @Test
  public void testVcpMachines() throws KaramelException, IOException, InterruptedException {
    String clusterName = "sparkonvpc";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/sparkonvpc.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    SshKeyPair sshKeys = api.loadSshKeysIfExist("");
    if (sshKeys == null) {
      sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
    }
    api.registerSshKeys(sshKeys);
    Ec2Credentials credentials = api.loadEc2CredentialsIfExist();
    api.updateEc2CredentialsIfValid(credentials);
    api.startCluster(json);
    long ms1 = System.currentTimeMillis();
    int mins = 0;
    while (ms1 + 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
      mins++;
      ClusterRuntime clusterRuntime = ClusterService.getInstance().clusterStatus(clusterName);
      if (clusterRuntime.getPhase().ordinal() > ClusterRuntime.ClusterPhases.INSTALLED.ordinal()) {
        api.processCommand("purge " + clusterName);
      }
      System.out.println(api.processCommand("machines").getResult());
      Thread.currentThread().sleep(30000);
    }
  }

//  @Test
  public void testDag() throws KaramelException, IOException, InterruptedException {
    String clusterName = "spark";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/spark.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    SshKeyPair sshKeys = api.loadSshKeysIfExist("");
    if (sshKeys == null) {
      sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
    }
    api.registerSshKeys(sshKeys);
    Ec2Credentials credentials = api.loadEc2CredentialsIfExist();
    api.updateEc2CredentialsIfValid(credentials);
    api.startCluster(json);
    long ms1 = System.currentTimeMillis();
    int mins = 0;
    while (ms1 + 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
      mins++;
      System.out.println(api.processCommand("dag spark").getResult());
      Thread.currentThread().sleep(60000);
    }
  }
  
//   @Test
  public void testStatus() throws KaramelException, IOException, InterruptedException {
    String clusterName = "flink";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/flink.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    SshKeyPair sshKeys = api.loadSshKeysIfExist("");
    if (sshKeys == null) {
      sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
    }
    api.registerSshKeys(sshKeys);
    Ec2Credentials credentials = api.loadEc2CredentialsIfExist();
    api.updateEc2CredentialsIfValid(credentials);
    api.startCluster(json);
    long ms1 = System.currentTimeMillis();
    int mins = 0;
    while (ms1 + 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
      mins++;
      System.out.println(api.processCommand("status").getResult());
      Thread.currentThread().sleep(60000);
    }
  }

  @Test
  public void testReturnResults() throws KaramelException, IOException, InterruptedException {
    String clusterName = "ndb";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/ndb.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    SshKeyPair sshKeys = api.loadSshKeysIfExist("");
    if (sshKeys == null) {
      sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
    }
    api.registerSshKeys(sshKeys);
    Ec2Credentials credentials = api.loadEc2CredentialsIfExist();
    api.updateEc2CredentialsIfValid(credentials);
    api.startCluster(json);
    long ms1 = System.currentTimeMillis();
    int mins = 0;
    while (ms1 + 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
      mins++;
      System.out.println(api.processCommand("status").getResult());
      Thread.currentThread().sleep(60000);
    }
  }

}
