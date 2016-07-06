/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.mode.task;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.converter.ChefJsonGenerator;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.backend.running.model.tasks.DagBuilder;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.backend.mocking.MockingUtil;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.util.Confs;

/**
 *
 * @author kamal
 */
public class DagBuilderTest {

  @Test
  public void testHopsworksInstallationDag() throws IOException, KaramelException {
    TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

      @Override
      public void submitTask(Task task) throws KaramelException {
        System.out.println(task.uniqueId());
        task.succeed();
      }

      @Override
      public void prepareToStart(Task task) throws KaramelException {
      }

      @Override
      public void killMe(Task task) throws KaramelException {
      }

      @Override
      public void retryMe(Task task) throws KaramelException {
      }

      @Override
      public void skipMe(Task task) throws KaramelException {
      }

      @Override
      public void terminate(Task task) throws KaramelException {
      }
    };

    Settings.CB_CLASSPATH_MODE = true;
    String ymlString = Resources.toString(Resources.getResource("se/kth/karamel/client/model/test-definitions/hopsworks.yml"), Charsets.UTF_8);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(ymlString);
    ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(definition);
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsonsForInstallation(definition, dummyRuntime);
    ClusterStats clusterStats = new ClusterStats();
    Dag dag = DagBuilder.getInstallationDag(definition, dummyRuntime, clusterStats, dummyTaskSubmitter, chefJsons);
    dag.validate();
    System.out.println(dag.print());
//    dag.start();
  }

  @Test
  public void testHopsworksPurgingDag() throws IOException, KaramelException {
    TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

      @Override
      public void submitTask(Task task) throws KaramelException {
        System.out.println(task.uniqueId());
        task.succeed();
      }

      @Override
      public void prepareToStart(Task task) throws KaramelException {
      }

      @Override
      public void killMe(Task task) throws KaramelException {
      }

      @Override
      public void retryMe(Task task) throws KaramelException {
      }

      @Override
      public void skipMe(Task task) throws KaramelException {
      }

      @Override
      public void terminate(Task task) throws KaramelException {
      }
    };

    Settings.CB_CLASSPATH_MODE = true;
    String ymlString = Resources.toString(Resources.getResource("se/kth/karamel/client/model/test-definitions/hopsworks.yml"), Charsets.UTF_8);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(ymlString);
    ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(definition);
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsonsForPurge(definition, dummyRuntime);
    ClusterStats clusterStats = new ClusterStats();
    Dag dag = DagBuilder.getPurgingDag(definition, dummyRuntime, clusterStats, dummyTaskSubmitter, chefJsons);
    dag.validate();
    System.out.println(dag.print());
//    dag.start();
  }

  @Test
  public void testFlinkDag() throws IOException, KaramelException {
    TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

      @Override
      public void submitTask(Task task) throws KaramelException {
        System.out.println(task.uniqueId());
        task.succeed();
      }

      @Override
      public void prepareToStart(Task task) throws KaramelException {
      }

      @Override
      public void killMe(Task task) throws KaramelException {
      }

      @Override
      public void retryMe(Task task) throws KaramelException {
      }

      @Override
      public void skipMe(Task task) throws KaramelException {
      }

      @Override
      public void terminate(Task task) throws KaramelException {
      }
    };

    Settings.CB_CLASSPATH_MODE = true;
    Confs confs = new Confs();
    confs.put(Settings.PREPARE_STORAGES_KEY, "false");
    Confs.setMemConfs(confs);

    String ymlString = Resources.toString(Resources.getResource("se/kth/karamel/client/model/test-definitions/flink.yml"), Charsets.UTF_8);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(ymlString);
    ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(definition);
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsonsForInstallation(definition, dummyRuntime);
    ClusterStats clusterStats = new ClusterStats();
    Dag dag = DagBuilder.getInstallationDag(definition, dummyRuntime, clusterStats, dummyTaskSubmitter, chefJsons);
    dag.validate();
    System.out.println(dag.print());

    Assert.assertTrue(dag.isRoot("find os-type on namenodes1"));
    Assert.assertTrue(dag.hasDependency("find os-type on namenodes1", "apt-get essentials on namenodes1"));
//    Assert.assertTrue(dag.hasDependency("apt-get essentials on namenodes1", "install collectl on namenodes1"));
//    Assert.assertTrue(dag.hasDependency("install collectl on namenodes1", "install tablespoon agent on namenodes1"));
    Assert.assertTrue(dag.hasDependency("apt-get essentials on namenodes1", "install berkshelf on namenodes1"));
    Assert.assertTrue(dag.hasDependency("install berkshelf on namenodes1", "make solo.rb on namenodes1"));
    Assert.assertTrue(dag.hasDependency("make solo.rb on namenodes1", "clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on namenodes1"));
    Assert.assertTrue(dag.hasDependency("clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on namenodes1", "flink::install on namenodes1"));
    Assert.assertTrue(dag.hasDependency("flink::install on namenodes1", "flink::jobmanager on namenodes1"));
    Assert.assertTrue(dag.hasDependency("flink::install on namenodes1", "flink::wordcount on namenodes1"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on namenodes1", "hadoop::nn on namenodes1"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on namenodes1", "flink::install on namenodes1"));

    Assert.assertTrue(dag.isRoot("find os-type on datanodes1"));
    Assert.assertTrue(dag.hasDependency("find os-type on datanodes1", "apt-get essentials on datanodes1"));
//    Assert.assertTrue(dag.hasDependency("apt-get essentials on datanodes1", "install collectl on datanodes1"));
//    Assert.assertTrue(dag.hasDependency("install collectl on datanodes1", "install tablespoon agent on datanodes1"));
    Assert.assertTrue(dag.hasDependency("apt-get essentials on datanodes1", "install berkshelf on datanodes1"));
    Assert.assertTrue(dag.hasDependency("install berkshelf on datanodes1", "make solo.rb on datanodes1"));
    Assert.assertTrue(dag.hasDependency("make solo.rb on datanodes1", "clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on datanodes1"));
    Assert.assertTrue(dag.hasDependency("clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on datanodes1", "flink::install on datanodes1"));
    Assert.assertTrue(dag.hasDependency("flink::install on datanodes1", "flink::taskmanager on datanodes1"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on datanodes1", "hadoop::dn on datanodes1"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on datanodes1", "flink::install on datanodes1"));

    Assert.assertTrue(dag.isRoot("find os-type on datanodes2"));
    Assert.assertTrue(dag.hasDependency("find os-type on datanodes2", "apt-get essentials on datanodes2"));
//    Assert.assertTrue(dag.hasDependency("apt-get essentials on datanodes2", "install collectl on datanodes2"));
//    Assert.assertTrue(dag.hasDependency("install collectl on datanodes2", "install tablespoon agent on datanodes2"));
    Assert.assertTrue(dag.hasDependency("apt-get essentials on datanodes2", "install berkshelf on datanodes2"));
    Assert.assertTrue(dag.hasDependency("install berkshelf on datanodes2", "make solo.rb on datanodes2"));
    Assert.assertTrue(dag.hasDependency("make solo.rb on datanodes2", "clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on datanodes2"));
    Assert.assertTrue(dag.hasDependency("clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on datanodes2", "flink::install on datanodes2"));
    Assert.assertTrue(dag.hasDependency("flink::install on datanodes2", "flink::taskmanager on datanodes2"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on datanodes2", "hadoop::dn on datanodes2"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on datanodes2", "flink::install on datanodes2"));
  }

  @Test
  public void testFlinkDagPrepStorage() throws IOException, KaramelException {
    TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

      @Override
      public void submitTask(Task task) throws KaramelException {
        System.out.println(task.uniqueId());
        task.succeed();
      }

      @Override
      public void prepareToStart(Task task) throws KaramelException {
      }

      @Override
      public void killMe(Task task) throws KaramelException {
      }

      @Override
      public void retryMe(Task task) throws KaramelException {
      }

      @Override
      public void skipMe(Task task) throws KaramelException {
      }

      @Override
      public void terminate(Task task) throws KaramelException {
      }
    };

    Settings.CB_CLASSPATH_MODE = true;
    Confs confs = new Confs();
    confs.put(Settings.PREPARE_STORAGES_KEY, "true");
    Confs.setMemConfs(confs);

    String ymlString = Resources.toString(Resources.getResource("se/kth/karamel/client/model/test-definitions/flink.yml"), Charsets.UTF_8);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(ymlString);
    ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(definition);
    Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsonsForInstallation(definition, dummyRuntime);
    ClusterStats clusterStats = new ClusterStats();
    Dag dag = DagBuilder.getInstallationDag(definition, dummyRuntime, clusterStats, dummyTaskSubmitter, chefJsons);
    dag.validate();
    System.out.println(dag.print());

    Assert.assertTrue(dag.isRoot("find os-type on namenodes1"));
    Assert.assertTrue(dag.hasDependency("find os-type on namenodes1", "prepare storages on namenodes1"));
    Assert.assertTrue(dag.hasDependency("find os-type on namenodes1", "apt-get essentials on namenodes1"));
    Assert.assertTrue(dag.hasDependency("prepare storages on namenodes1", "apt-get essentials on namenodes1"));
//    Assert.assertTrue(dag.hasDependency("apt-get essentials on namenodes1", "install collectl on namenodes1"));
//    Assert.assertTrue(dag.hasDependency("install collectl on namenodes1", "install tablespoon agent on namenodes1"));
    Assert.assertTrue(dag.hasDependency("apt-get essentials on namenodes1", "install berkshelf on namenodes1"));
    Assert.assertTrue(dag.hasDependency("install berkshelf on namenodes1", "make solo.rb on namenodes1"));
    Assert.assertTrue(dag.hasDependency("make solo.rb on namenodes1", "clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on namenodes1"));
    Assert.assertTrue(dag.hasDependency("clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on namenodes1", "flink::install on namenodes1"));
    Assert.assertTrue(dag.hasDependency("flink::install on namenodes1", "flink::jobmanager on namenodes1"));
    Assert.assertTrue(dag.hasDependency("flink::install on namenodes1", "flink::wordcount on namenodes1"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on namenodes1", "hadoop::nn on namenodes1"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on namenodes1", "flink::install on namenodes1"));

    Assert.assertTrue(dag.isRoot("find os-type on datanodes1"));
    Assert.assertTrue(dag.hasDependency("find os-type on datanodes1", "prepare storages on datanodes1"));
    Assert.assertTrue(dag.hasDependency("find os-type on datanodes1", "apt-get essentials on datanodes1"));
//    Assert.assertTrue(dag.hasDependency("apt-get essentials on datanodes1", "install collectl on datanodes1"));
//    Assert.assertTrue(dag.hasDependency("install collectl on datanodes1", "install tablespoon agent on datanodes1"));
    Assert.assertTrue(dag.hasDependency("apt-get essentials on datanodes1", "install berkshelf on datanodes1"));
    Assert.assertTrue(dag.hasDependency("install berkshelf on datanodes1", "make solo.rb on datanodes1"));
    Assert.assertTrue(dag.hasDependency("make solo.rb on datanodes1", "clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on datanodes1"));
    Assert.assertTrue(dag.hasDependency("clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on datanodes1", "flink::install on datanodes1"));
    Assert.assertTrue(dag.hasDependency("flink::install on datanodes1", "flink::taskmanager on datanodes1"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on datanodes1", "hadoop::dn on datanodes1"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on datanodes1", "flink::install on datanodes1"));

    Assert.assertTrue(dag.isRoot("find os-type on datanodes2"));
    Assert.assertTrue(dag.hasDependency("find os-type on datanodes2", "prepare storages on datanodes2"));
    Assert.assertTrue(dag.hasDependency("find os-type on datanodes2", "apt-get essentials on datanodes2"));
//    Assert.assertTrue(dag.hasDependency("apt-get essentials on datanodes2", "install collectl on datanodes2"));
//    Assert.assertTrue(dag.hasDependency("install collectl on datanodes2", "install tablespoon agent on datanodes2"));
    Assert.assertTrue(dag.hasDependency("apt-get essentials on datanodes2", "install berkshelf on datanodes2"));
    Assert.assertTrue(dag.hasDependency("install berkshelf on datanodes2", "make solo.rb on datanodes2"));
    Assert.assertTrue(dag.hasDependency("make solo.rb on datanodes2", "clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on datanodes2"));
    Assert.assertTrue(dag.hasDependency("clone and vendor https://github.com/testorg/testrepo/tree/master/cookbooks/flink-chef on datanodes2", "flink::install on datanodes2"));
    Assert.assertTrue(dag.hasDependency("flink::install on datanodes2", "flink::taskmanager on datanodes2"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on datanodes2", "hadoop::dn on datanodes2"));
    Assert.assertTrue(dag.hasDependency("hadoop::install on datanodes2", "flink::install on datanodes2"));
  }

  @Test
  public void testTablespoonInstallationDag() throws IOException, KaramelException {
    TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

      @Override
      public void submitTask(Task task) throws KaramelException {
        System.out.println(task.uniqueId());
        task.succeed();
      }

      @Override
      public void prepareToStart(Task task) throws KaramelException {
      }

      @Override
      public void killMe(Task task) throws KaramelException {
      }

      @Override
      public void retryMe(Task task) throws KaramelException {
      }

      @Override
      public void skipMe(Task task) throws KaramelException {
      }

      @Override
      public void terminate(Task task) throws KaramelException {
      }
    };
    String ymlString = Resources.toString(Resources.getResource("se/kth/karamel/client/model/test-definitions/flink.yml"), Charsets.UTF_8);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(ymlString);
    ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(definition);
    ClusterStats clusterStats = new ClusterStats();
    Dag dag = DagBuilder.getInstallTablespoonDag(dummyRuntime, clusterStats, dummyTaskSubmitter);
    dag.validate();
    Assert.assertTrue(dag.hasDependency("apt-get essentials on namenodes1", "install collectl on namenodes1"));
    Assert.assertTrue(dag.hasDependency("install collectl on namenodes1", "install tablespoon agent on namenodes1"));
  }

  @Test
  public void testTablespoonStartDag() throws IOException, KaramelException {
    TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

      @Override
      public void submitTask(Task task) throws KaramelException {
        System.out.println(task.uniqueId());
        task.succeed();
      }

      @Override
      public void prepareToStart(Task task) throws KaramelException {
      }

      @Override
      public void killMe(Task task) throws KaramelException {
      }

      @Override
      public void retryMe(Task task) throws KaramelException {
      }

      @Override
      public void skipMe(Task task) throws KaramelException {
      }

      @Override
      public void terminate(Task task) throws KaramelException {
      }
    };
    String ymlString = Resources.toString(Resources.getResource("se/kth/karamel/client/model/test-definitions/flink.yml"), Charsets.UTF_8);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(ymlString);
    ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(definition);
    ClusterStats clusterStats = new ClusterStats();
    Dag dag = DagBuilder.getStartTablespoonDag(dummyRuntime, clusterStats, dummyTaskSubmitter);
    dag.validate();
    Assert.assertTrue(dag.hasDependency("install tablespoon agent on namenodes1", "start tablespoon on namenodes1"));
  }
  
  @Test
  public void testTablespoonTopicDag() throws IOException, KaramelException {
    TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

      @Override
      public void submitTask(Task task) throws KaramelException {
        System.out.println(task.uniqueId());
        task.succeed();
      }

      @Override
      public void prepareToStart(Task task) throws KaramelException {
      }

      @Override
      public void killMe(Task task) throws KaramelException {
      }

      @Override
      public void retryMe(Task task) throws KaramelException {
      }

      @Override
      public void skipMe(Task task) throws KaramelException {
      }

      @Override
      public void terminate(Task task) throws KaramelException {
      }
    };
    String ymlString = Resources.toString(Resources.getResource("se/kth/karamel/client/model/test-definitions/flink.yml"), Charsets.UTF_8);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(ymlString);
    ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(definition);
    ClusterStats clusterStats = new ClusterStats();
    String json = "{example: json}";
    String uniqueId = "123456789";
    HashSet<String> vmids = Sets.newHashSet("namenodes1", "datanodes1", "datanodes2");
    Dag dag = DagBuilder.getCreateTablespoonTopicDag(dummyRuntime, clusterStats, dummyTaskSubmitter,vmids, json, uniqueId);
    dag.validate();
    Assert.assertTrue(dag.hasDependency("install tablespoon agent on namenodes1", "update tablespoon topic on namenodes1"));
  }
  
}
