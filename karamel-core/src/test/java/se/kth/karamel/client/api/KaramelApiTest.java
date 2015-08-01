package se.kth.karamel.client.api;

import se.kth.karamel.common.exception.KaramelException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Test;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.Experiment;
import se.kth.karamel.backend.github.util.ChefExperimentExtractor;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.cookbook.metadata.KaramelizedCookbook;

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

  @Test
  public void commitPushTest() throws KaramelException {
    Experiment exp = new Experiment();

    exp.setBerksfile("ark\njava");
    exp.setClusterDefinition("");
    Experiment.Code ec = new Experiment.Code("experiment", "echo 'jim'", "config/config.props", "jim=dow", "bash");
    Experiment.Code ecL = new Experiment.Code("linda", "python blah", "my.props", "lin=gron", "python");
    ArrayList<Experiment.Code> code = new ArrayList<>();
    code.add(ec);
    code.add(ecL);
    exp.setCode(code);

    exp.setDescription("some repo");
    exp.setExperimentSetupCode("chef code");
    exp.setGithubOwner("jimdowling");
    exp.setGithubRepo("test");
    exp.setGlobalDependencies("hops::nn\nhops::dn");
    exp.setGroup("testG");
    exp.setLocalDependencies("hops::install");
    exp.setUser("testU");
//    api.commitAndPushExperiment(exp);
    ChefExperimentExtractor.parseAttributesAddToGit("jimdowling", "test", exp);
    ChefExperimentExtractor.parseRecipesAddToGit("jimdowling", "test", exp);

    KaramelizedCookbook kc = new KaramelizedCookbook("https://github.com/jimdowling/test", true);
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
    String clusterName = "hopsonenode";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/hops-1node-aws-m3-med.yml"),
        Charsets.UTF_8);
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
      System.out.println(api.processCommand("tdag hopsonenode").getResult());
      Thread.currentThread().sleep(60000);
    }
  }

//   @Test
  public void testStatus() throws KaramelException, IOException, InterruptedException {
    String clusterName = "hiway";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/hiway.yml"), Charsets.UTF_8);
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

//  @Test
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

//  @Test
  public void testBaremetal() throws KaramelException, IOException, InterruptedException {
//    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/flink_baremetal.yml"), Charsets.UTF_8);
//    String json = api.yamlToJson(ymlString);
    String json
        = "{\"name\":\"flink\",\"cookbooks\":[{\"name\":\"hadoop\",\"github\":\"hopshadoop/apache-hadoop-chef\",\"branch\":\"master\",\"attributes\":{},\"recipes\":[]},{\"name\":\"flink\",\"github\":\"hopshadoop/flink-chef\",\"branch\":\"master\",\"attributes\":{},\"recipes\":[]}],\"groups\":[{\"name\":\"namenodes\",\"provider\":\"\",\"attrs\":[],\"instances\":1,\"baremetal\":{\"ips\":[\"192.168.33.11\"]},\"cookbooks\":[{\"name\":\"hadoop\",\"github\":\"hopshadoop/apache-hadoop-chef\",\"branch\":\"master\",\"attributes\":{},\"recipes\":[{\"title\":\"hadoop::nn\"}]},{\"name\":\"flink\",\"github\":\"hopshadoop/flink-chef\",\"branch\":\"master\",\"attributes\":{},\"recipes\":[{\"title\":\"flink::wordcount\"},{\"title\":\"flink::jobmanager\"}]}]},{\"name\":\"datanodes\",\"provider\":\"\",\"attrs\":[],\"instances\":2,\"baremetal\":{\"ips\":[\"192.168.33.12\",\"192.168.33.13\"]},\"cookbooks\":[{\"name\":\"hadoop\",\"github\":\"hopshadoop/apache-hadoop-chef\",\"branch\":\"master\",\"attributes\":{},\"recipes\":[{\"title\":\"hadoop::dn\"}]},{\"name\":\"flink\",\"github\":\"hopshadoop/flink-chef\",\"branch\":\"master\",\"attributes\":{},\"recipes\":[{\"title\":\"flink::taskmanager\"}]}]}],\"ec2\":null,\"baremetal\":{\"mapKey\":\"baremetal\",\"username\":\"vagrant\",\"ips\":[]},\"sshKeyPair\":{\"mapKey\":\"ssh\",\"pubKey\":null,\"priKey\":null,\"pubKeyPath\":null,\"privKeyPath\":null,\"passphrase\":null,\"isValid\":true}}\n"
        + "";
    SshKeyPair keyPair = new SshKeyPair();
    keyPair.setPrivateKeyPath("/Users/kamal/.vagrant.d/insecure_private_key");
    keyPair.setPublicKeyPath("/Users/kamal/.vagrant.d/id_rsa.pub");
    SshKeyPair sshKeys = api.registerSshKeys(keyPair);
    api.registerSshKeys(sshKeys);
//    Ec2Credentials credentials = api.loadEc2CredentialsIfExist();
//    api.updateEc2CredentialsIfValid(credentials);
    api.startCluster(json);
    long ms1 = System.currentTimeMillis();
    int mins = 0;
    while (ms1 + 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
      mins++;
      System.out.println(api.processCommand("status").getResult());
      Thread.currentThread().sleep(60000);
    }
  }

//  @Test
  public void testGce() throws KaramelException, IOException, InterruptedException {
    String clusterName = "flinkgce";
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/flink_gce.yml"), Charsets.UTF_8);
    String json = api.yamlToJson(ymlString);
    System.out.println(json);
    SshKeyPair sshKeys = api.loadSshKeysIfExist("");
    if (sshKeys == null) {
      sshKeys = api.generateSshKeysAndUpdateConf(clusterName);
    }
    api.registerSshKeys(sshKeys);
//    String keyPath = api.loadGceCredentialsIfExist();
    api.updateGceCredentialsIfValid(Settings.KARAMEL_ROOT_PATH + "/gce-key.json");
    api.startCluster(json);
    long ms1 = System.currentTimeMillis();
    while (ms1 + 24 * 60 * 60 * 1000 > System.currentTimeMillis()) {
      System.out.println(api.processCommand("status").getResult());
      Thread.currentThread().sleep(60000);
    }
  }

}
