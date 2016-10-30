/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import se.kth.karamel.backend.running.model.tasks.tablespoon.StopTablespoonTask;
import se.kth.karamel.backend.running.model.tasks.tablespoon.TopicTablespoonTask;
import se.kth.karamel.backend.running.model.tasks.tablespoon.StartTablespoonTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.converter.ChefJsonGenerator;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.common.launcher.aws.InstanceType;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.json.JsonRecipe;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.DagConstructionException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.cookbookmeta.CookbookUrls;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.KaramelFileYamlDeps;
import se.kth.karamel.common.util.Confs;
import se.kth.tablespoon.client.topics.Topic;

/**
 *
 * @author kamal
 */
public class DagBuilder {

  private static final Logger logger = Logger.getLogger(DagBuilder.class);

  /**
   * 1. Machine-level tasks such as: - AptGetEssential - PrepareStorage - InstallBerkshelf - MakeSoloRb 2.Cookbook-level
   * tasks such as: - CloneAndVendorCookbook - RunRecipeTask for purge
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param submitter
   * @param chefJsons
   * @return
   * @throws KaramelException
   */
  public static Dag getPurgingDag(JsonCluster cluster, ClusterRuntime clusterEntity, ClusterStats clusterStats,
      TaskSubmitter submitter, Map<String, JsonObject> chefJsons) throws KaramelException {
    Dag dag = new Dag("purge");
    Map<String, RunRecipeTask> allRecipeTasks = new HashMap<>();
    CookbookCache cache = ClusterDefinitionService.CACHE;
    List<KaramelizedCookbook> kcbs = cache.loadRootKaramelizedCookbooks(cluster);
    machinePreparationTasks(cluster, clusterEntity, clusterStats, submitter, dag, kcbs);
    cookbookLevelPurgingTasks(cluster, clusterEntity, clusterStats, chefJsons, submitter, allRecipeTasks, dag, kcbs);
    return dag;
  }

  /**
   * 1. Machine-level tasks such as: - AptGetEssential - PrepareStorage - InstallBerkshelf - MakeSoloRb 2.Cookbook-level
   * tasks such as: - CloneAndVendorCookbook - RunRecipeTask for Install 3.Recipe-level tasks such as: - RunRecipe tasks
   * for all recipes except install 4.Applies dependencies that are defined in the Karamelfile
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param submitter
   * @param chefJsons
   * @return
   * @throws KaramelException
   */
  public static Dag getInstallationDag(JsonCluster cluster, ClusterRuntime clusterEntity, ClusterStats clusterStats,
      TaskSubmitter submitter, Map<String, JsonObject> chefJsons) throws KaramelException {
    Dag dag = new Dag("install");
    Map<String, RunRecipeTask> allRecipeTasks = new HashMap<>();
    CookbookCache cache = ClusterDefinitionService.CACHE;
    List<KaramelizedCookbook> kcbs = cache.loadRootKaramelizedCookbooks(cluster);
    machinePreparationTasks(cluster, clusterEntity, clusterStats, submitter, dag, kcbs);
    cookbookLevelInstallationTasks(cluster, clusterEntity, clusterStats, chefJsons, submitter, allRecipeTasks, dag,
        kcbs);
    Map<String, Map<String, Task>> rlts = recipeLevelTasks(cluster, clusterEntity, clusterStats, chefJsons, submitter,
        allRecipeTasks, dag, kcbs);
    updateKaramelDependencies(allRecipeTasks, dag, rlts);
    return dag;
  }

  public static Dag getJoinDagForMachine(JsonCluster cluster, ClusterRuntime clusterEntity,
      ClusterStats clusterStats, TaskSubmitter submitter, Map<String, JsonObject> chefJsons, String groupId,
      String machineId, String tsConfig) throws KaramelException {
    String dagName = "join node";
    CookbookCache cache = ClusterDefinitionService.CACHE;
    List<KaramelizedCookbook> kcbs = cache.loadRootKaramelizedCookbooks(cluster);
    MachineRuntime me = ClusterDefinitionService.findMachine(clusterEntity, groupId, machineId);
    Dag dag = new Dag(dagName);
    Map<String, RunRecipeTask> allRecipeTasks = new HashMap<>();
    machinePreparationTasks(cluster, clusterEntity, clusterStats, submitter, dag, groupId, machineId, kcbs);
    cookbookLevelInstallationTasksForMachine(cluster, clusterEntity, clusterStats, chefJsons, submitter, allRecipeTasks,
        dag, groupId, machineId, kcbs);
    InstallCollectlTask t1 = new InstallCollectlTask(dagName, me, clusterStats, submitter);
    InstallTablespoonAgent t2 = new InstallTablespoonAgent(dagName, me, clusterStats, submitter, tsConfig);
    StartTablespoonTask t3 = new StartTablespoonTask(dagName, me, clusterStats, submitter);
    dag.addTasks(t1, t2, t3);
    Map<String, Map<String, Task>> rlts = recipeLevelTasks(cluster, clusterEntity, clusterStats, chefJsons, submitter,
        allRecipeTasks, dag, groupId, machineId, kcbs);
    updateKaramelDependencies(allRecipeTasks, dag, rlts);
    return dag;
  }

  public static Dag getInstallTablespoonDag(ClusterRuntime clusterEntity, ClusterStats clusterStats,
      TaskSubmitter submitter, String tsConfig) throws KaramelException {
    String dagName = "install tablespoon";
    Dag dag = new Dag(dagName);
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      for (MachineRuntime me : ge.getMachines()) {
        FindOsTypeTask t1 = new FindOsTypeTask(dagName, me, clusterStats, submitter);
        AptGetEssentialsTask t2 = new AptGetEssentialsTask(dagName, me, clusterStats, submitter, false);
        InstallCollectlTask t3 = new InstallCollectlTask(dagName, me, clusterStats, submitter);
        InstallTablespoonAgent t4 = new InstallTablespoonAgent(dagName, me, clusterStats, submitter, tsConfig);
        dag.addTasks(t1, t2, t3, t4);
      }
    }
    return dag;
  }

  public static Dag getStartTablespoonDag(ClusterRuntime clusterEntity, ClusterStats clusterStats,
      TaskSubmitter submitter, String tsConfig) throws KaramelException {
    String dagName = "tablespoon start";
    Dag dag = new Dag(dagName);
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      for (MachineRuntime me : ge.getMachines()) {
        FindOsTypeTask t1 = new FindOsTypeTask(dagName, me, clusterStats, submitter);
        AptGetEssentialsTask t2 = new AptGetEssentialsTask(dagName, me, clusterStats, submitter, false);
        InstallCollectlTask t3 = new InstallCollectlTask(dagName, me, clusterStats, submitter);
        InstallTablespoonAgent t4 = new InstallTablespoonAgent(dagName, me, clusterStats, submitter, tsConfig);
        StartTablespoonTask t5 = new StartTablespoonTask(dagName, me, clusterStats, submitter);
        dag.addTasks(t1, t2, t3, t4, t5);
      }
    }
    return dag;
  }

  public static Dag getStopTablespoonDag(ClusterRuntime clusterEntity, ClusterStats clusterStats,
      TaskSubmitter submitter, String tsConfig) throws KaramelException {
    String dagName = "launch tablespoon";
    Dag dag = new Dag(dagName);
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      for (MachineRuntime me : ge.getMachines()) {
        FindOsTypeTask t1 = new FindOsTypeTask(dagName, me, clusterStats, submitter);
        AptGetEssentialsTask t2 = new AptGetEssentialsTask(dagName, me, clusterStats, submitter, false);
        InstallCollectlTask t3 = new InstallCollectlTask(dagName, me, clusterStats, submitter);
        InstallTablespoonAgent t4 = new InstallTablespoonAgent(dagName, me, clusterStats, submitter, tsConfig);
        StopTablespoonTask t5 = new StopTablespoonTask(dagName, me, clusterStats, submitter);
        dag.addTasks(t1, t2, t3, t4, t5);
      }
    }
    return dag;
  }

  public static Dag getCreateTablespoonTopicDag(ClusterRuntime clusterEntity, ClusterStats clusterStats,
      TaskSubmitter submitter, Set<String> vmIds, String json, Topic topic, String tsConfig)
      throws KaramelException {
    String dagName = String.format("tablespoon topic (%s)", topic.toString());
    Dag dag = new Dag(dagName);
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      for (MachineRuntime me : ge.getMachines()) {
        if (vmIds.contains(me.getVmId())) {
          FindOsTypeTask t1 = new FindOsTypeTask(dagName, me, clusterStats, submitter);
          AptGetEssentialsTask t2 = new AptGetEssentialsTask(dagName, me, clusterStats, submitter, false);
          InstallCollectlTask t3 = new InstallCollectlTask(dagName, me, clusterStats, submitter);
          InstallTablespoonAgent t4 = new InstallTablespoonAgent(dagName, me, clusterStats, submitter, tsConfig);
          TopicTablespoonTask t5 = new TopicTablespoonTask(dagName, me, clusterStats, submitter, json,
              topic.getUniqueId());
          dag.addTasks(t1, t2, t3, t4, t5);
        }
      }
    }
    return dag;
  }

  private static boolean updateKaramelDependencies(Map<String, RunRecipeTask> allRecipeTasks, Dag dag,
      Map<String, Map<String, Task>> rlts) throws KaramelException {
    boolean newDepFound = false;
    HashSet<String> cbids = new HashSet<>();
    for (RunRecipeTask task : allRecipeTasks.values()) {
      cbids.add(task.getCookbookId());
    }
    CookbookCache cache = ClusterDefinitionService.CACHE;
    cache.prepareParallel(cbids);
    for (RunRecipeTask task : allRecipeTasks.values()) {
      String tid = task.uniqueId();
      KaramelizedCookbook kcb = cache.get(task.getCookbookId());
      KaramelFileYamlDeps dependency = kcb.getKaramelFile().getDependency(task.getRecipeCanonicalName());
      if (dependency != null) {
        for (String depRec : dependency.getLocal()) {
          String depId = RunRecipeTask.makeUniqueId(task.getMachineId(), depRec);
          newDepFound |= dag.addDependency(depId, tid);
        }

        for (String depRec : dependency.getGlobal()) {
          Map<String, Task> rlt2 = rlts.get(depRec);
          if (rlt2 != null) {
            for (Map.Entry<String, Task> entry : rlt2.entrySet()) {
              Task t7 = entry.getValue();
              newDepFound |= dag.addDependency(t7.uniqueId(), tid);
            }
          }
        }
      }
    }
    return newDepFound;
  }

  /**
   * Creates all recipe tasks for cluster and groups them by recipe-name. In other words, by having a recipe-name such
   * as hadoop::dn you fetch all the tasks in the cluster that are running hadoop::dn. recipeName -> taskid(recipe +
   * machineid) -> task
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param chefJsons
   * @param submitter
   * @param allRecipeTasks
   * @param dag
   * @param rootCookbooks
   * @return
   * @throws KaramelException
   */
  public static Map<String, Map<String, Task>> recipeLevelTasks(JsonCluster cluster, ClusterRuntime clusterEntity,
      ClusterStats clusterStats, Map<String, JsonObject> chefJsons, TaskSubmitter submitter,
      Map<String, RunRecipeTask> allRecipeTasks, Dag dag,
      List<KaramelizedCookbook> rootCookbooks) throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      JsonGroup jg = ClusterDefinitionService.findGroup(cluster, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        for (JsonCookbook jc : jg.getCookbooks()) {
          for (JsonRecipe rec : jc.getRecipes()) {
            JsonObject json1 = chefJsons.get(me.getId() + rec.getCanonicalName());
            addRecipeTaskForMachineIntoRecipesMap(rec.getCanonicalName(), me, clusterStats, map, json1, submitter,
                jc.getId(), jc.getName(), allRecipeTasks, dag, rootCookbooks);
          }
        }
      }
    }
    return map;
  }

  public static Map<String, Map<String, Task>> recipeLevelTasks(JsonCluster cluster, ClusterRuntime clusterEntity,
      ClusterStats clusterStats, Map<String, JsonObject> chefJsons, TaskSubmitter submitter,
      Map<String, RunRecipeTask> allRecipeTasks, Dag dag, String groupId, String machineId,
      List<KaramelizedCookbook> rootCookbooks) throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      if (ge.getId().equals(groupId)) {
        JsonGroup jg = ClusterDefinitionService.findGroup(cluster, ge.getName());
        for (MachineRuntime me : ge.getMachines()) {
          if (me.getVmId().equals(machineId)) {
            for (JsonCookbook jc : jg.getCookbooks()) {
              CookbookUrls urls = jc.getUrls();
              for (JsonRecipe rec : jc.getRecipes()) {
                JsonObject json1 = chefJsons.get(me.getId() + rec.getCanonicalName());
                addRecipeTaskForMachineIntoRecipesMap(rec.getCanonicalName(), me, clusterStats, map, json1, submitter,
                    urls.id, jc.getName(), allRecipeTasks, dag, rootCookbooks);
              }
            }
            break;
          }
        }
        break;
      }
    }
    return map;
  }

  /*
   * Makes sure recipe-task for machine exists both in the DAG and in the grouping map of recipes
   */
  private static RunRecipeTask addRecipeTaskForMachineIntoRecipesMap(String recipeName, MachineRuntime machine,
      ClusterStats clusterStats, Map<String, Map<String, Task>> map, JsonObject chefJson, TaskSubmitter submitter,
      String cookbookId, String cookbookName, Map<String, RunRecipeTask> allRecipeTasks, Dag dag,
      List<KaramelizedCookbook> rootCookbooks)
      throws DagConstructionException {
    RunRecipeTask t1 = makeRecipeTaskIfNotExist(recipeName, machine, clusterStats, chefJson, submitter, cookbookId,
        cookbookName, allRecipeTasks, dag, rootCookbooks);
    Map<String, Task> map1 = map.get(recipeName);
    if (map1 == null) {
      map1 = new HashMap<>();
      map.put(recipeName, map1);
    }
    map1.put(t1.uniqueId(), t1);
    return t1;
  }

  /*
   * Finds recipe task for machine if it has been already created otherwise makes a new one and adds it into the DAG
   */
  private static RunRecipeTask makeRecipeTaskIfNotExist(String recipeName, MachineRuntime machine,
      ClusterStats clusterStats, JsonObject chefJson,
      TaskSubmitter submitter, String cookbookId, String cookbookName, Map<String, RunRecipeTask> allRecipeTasks,
      Dag dag, List<KaramelizedCookbook> rootCookbooks) throws DagConstructionException {
    String recId = RunRecipeTask.makeUniqueId(machine.getId(), recipeName);
    RunRecipeTask runRecipeTask = allRecipeTasks.get(recId);
    if (!allRecipeTasks.containsKey(recId)) {
      ChefJsonGenerator.addRunListForRecipe(chefJson, recipeName);
      GsonBuilder builder = new GsonBuilder();
      builder.disableHtmlEscaping();
      Gson gson = builder.setPrettyPrinting().create();
      String jsonString = gson.toJson(chefJson);
      runRecipeTask
          = new RunRecipeTask(dag.getName(), machine, clusterStats, recipeName, jsonString, submitter, cookbookId,
              cookbookName, rootCookbooks);
      dag.addTask(runRecipeTask);
    }
    allRecipeTasks.put(recId, runRecipeTask);
    return runRecipeTask;
  }

  /**
   * machine -> taskid -> task
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param chefJsons
   * @param submitter
   * @param allRecipeTasks
   * @param dag
   * @param rootCookbooks
   * @return
   * @throws KaramelException
   */
  public static Map<String, Map<String, Task>> cookbookLevelPurgingTasks(JsonCluster cluster,
      ClusterRuntime clusterEntity, ClusterStats clusterStats, Map<String, JsonObject> chefJsons,
      TaskSubmitter submitter, Map<String, RunRecipeTask> allRecipeTasks, Dag dag,
      List<KaramelizedCookbook> rootCookbooks) throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      JsonGroup jg = ClusterDefinitionService.findGroup(cluster, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        Map<String, Task> map1 = new HashMap<>();
        for (KaramelizedCookbook rcb : rootCookbooks) {
          CookbookUrls urls = rcb.getUrls();
          VendorCookbookTask t1 = new VendorCookbookTask(dag.getName(), me, clusterStats, submitter, urls.id,
              Settings.REMOTE_CB_VENDOR_PATH,
              urls.repoUrl, urls.repoName, urls.cookbookRelPath, urls.branch);
          dag.addTask(t1);
          map1.put(t1.uniqueId(), t1);
        }
        for (JsonCookbook jc : jg.getCookbooks()) {
          CookbookUrls urls = jc.getUrls();
          String recipeName = jc.getName() + Settings.COOKBOOK_DELIMITER + Settings.PURGE_RECIPE;
          JsonObject json = chefJsons.get(me.getId() + recipeName);
          RunRecipeTask t2 = makeRecipeTaskIfNotExist(recipeName, me, clusterStats, json, submitter, urls.id,
              jc.getName(), allRecipeTasks, dag, rootCookbooks);
          map1.put(t2.uniqueId(), t2);
        }
        logger.debug(String.format("Cookbook-level tasks for the machine '%s' in the group '%s' are: %s",
            me.getPublicIp(), ge.getName(), map1.keySet()));
        if (map.get(me.getId()) != null) {
          map.get(me.getId()).putAll(map1);
        } else {
          map.put(me.getId(), map1);
        }
      }
    }
    return map;
  }

  /**
   * machine -> taskid -> task
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param chefJsons
   * @param submitter
   * @param allRecipeTasks
   * @param dag
   * @param rootCookbooks
   * @return
   * @throws KaramelException
   */
  public static Map<String, Map<String, Task>> cookbookLevelInstallationTasks(JsonCluster cluster,
      ClusterRuntime clusterEntity, ClusterStats clusterStats, Map<String, JsonObject> chefJsons,
      TaskSubmitter submitter, Map<String, RunRecipeTask> allRecipeTasks, Dag dag,
      List<KaramelizedCookbook> rootCookbooks) throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      JsonGroup jg = ClusterDefinitionService.findGroup(cluster, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        Map<String, Task> map1 = new HashMap<>();
        for (KaramelizedCookbook rcb : rootCookbooks) {
          CookbookUrls urls = rcb.getUrls();
          VendorCookbookTask t1 = new VendorCookbookTask(dag.getName(), me, clusterStats, submitter, urls.id,
              Settings.REMOTE_CB_VENDOR_PATH,
              urls.repoUrl, urls.repoName, urls.cookbookRelPath, urls.branch);
          dag.addTask(t1);
          map1.put(t1.uniqueId(), t1);
        }
        for (JsonCookbook jc : jg.getCookbooks()) {
          CookbookUrls urls = jc.getUrls();
          String recipeName = jc.getName() + Settings.COOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
          JsonObject json = chefJsons.get(me.getId() + recipeName);
          RunRecipeTask t2 = makeRecipeTaskIfNotExist(recipeName, me, clusterStats,
              json, submitter, urls.id, jc.getName(), allRecipeTasks, dag, rootCookbooks);
          map1.put(t2.uniqueId(), t2);
        }
        logger.debug(String.format("Cookbook-level tasks for the machine '%s' in the group '%s' are: %s",
            me.getPublicIp(), ge.getName(), map1.keySet()));
        if (map.get(me.getId()) != null) {
          map.get(me.getId()).putAll(map1);
        } else {
          map.put(me.getId(), map1);
        }
      }
    }
    return map;
  }

  public static Map<String, Map<String, Task>> cookbookLevelInstallationTasksForMachine(JsonCluster cluster,
      ClusterRuntime clusterEntity, ClusterStats clusterStats, Map<String, JsonObject> chefJsons,
      TaskSubmitter submitter, Map<String, RunRecipeTask> allRecipeTasks, Dag dag, String groupId, String machineId,
      List<KaramelizedCookbook> rootCookbooks)
      throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      if (ge.getId().equals(groupId)) {
        JsonGroup jg = ClusterDefinitionService.findGroup(cluster, ge.getName());
        for (MachineRuntime me : ge.getMachines()) {
          if (me.getVmId().equals(machineId)) {
            Map<String, Task> map1 = new HashMap<>();
            for (KaramelizedCookbook rcb : rootCookbooks) {
              CookbookUrls urls = rcb.getUrls();
              VendorCookbookTask t1 = new VendorCookbookTask(dag.getName(), me, clusterStats, submitter, urls.id,
                  Settings.REMOTE_CB_VENDOR_PATH,
                  urls.repoUrl, urls.repoName, urls.cookbookRelPath, urls.branch);
              dag.addTask(t1);
              map1.put(t1.uniqueId(), t1);
            }
            for (JsonCookbook jc : jg.getCookbooks()) {
              CookbookUrls urls = jc.getUrls();
              String recipeName = jc.getName() + Settings.COOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
              JsonObject json = chefJsons.get(me.getId() + recipeName);
              RunRecipeTask t2 = makeRecipeTaskIfNotExist(recipeName, me, clusterStats,
                  json, submitter, urls.id, jc.getName(),
                  allRecipeTasks, dag, rootCookbooks);
              map1.put(t2.uniqueId(), t2);
            }
            logger.debug(String.format("Cookbook-level tasks for the machine '%s' in the group '%s' are: %s",
                me.getPublicIp(), ge.getName(), map1.keySet()));
            if (map.get(me.getId()) != null) {
              map.get(me.getId()).putAll(map1);
            } else {
              map.put(me.getId(), map1);
            }
          }
        }
      }
    }
    return map;
  }

  /**
   * Tasks that are machine specific, specifically those that are run in the very start preparation phase. For example:
   * - AptGetEssential - PrepareStorage - InstallBerkshelf - MakeSoloRb
   *
   * @param cluster
   * @param clusterEntity
   * @param clusterStats
   * @param submitter
   * @param dag
   * @param rootCookbooks
   * @throws KaramelException
   */
  public static void machinePreparationTasks(JsonCluster cluster, ClusterRuntime clusterEntity,
      ClusterStats clusterStats,
      TaskSubmitter submitter, Dag dag, List<KaramelizedCookbook> rootCookbooks) throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    String prepStoragesConf = confs.getProperty(Settings.PREPARE_STORAGES_KEY);
    String vendorPath = ClusterDefinitionService.makeVendorPath(rootCookbooks);
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      for (MachineRuntime me : ge.getMachines()) {
        FindOsTypeTask findOs = new FindOsTypeTask(dag.getName(), me, clusterStats, submitter);
        dag.addTask(findOs);
        Provider provider = ClusterDefinitionService.getGroupProvider(cluster, ge.getName());
        boolean storagePreparation = (prepStoragesConf != null && prepStoragesConf.equalsIgnoreCase("true")
            && (provider instanceof Ec2));
        if (storagePreparation) {
          String model = ((Ec2) provider).getType();
          InstanceType instanceType = InstanceType.valueByModel(model);
          PrepareStoragesTask st
              = new PrepareStoragesTask(dag.getName(), me, clusterStats, submitter, instanceType.getStorageDevices());
          dag.addTask(st);
        }
        AptGetEssentialsTask t1 = new AptGetEssentialsTask(dag.getName(), me, clusterStats, submitter,
            storagePreparation);
        InstallChefdkTask t2 = new InstallChefdkTask(dag.getName(), me, clusterStats, submitter);
        MakeSoloRbTask t3 = new MakeSoloRbTask(dag.getName(), me, vendorPath, clusterStats, submitter);
        dag.addTask(t1);
        dag.addTask(t2);
        dag.addTask(t3);
      }
    }
  }

  public static void machinePreparationTasks(JsonCluster cluster, ClusterRuntime clusterEntity,
      ClusterStats clusterStats, TaskSubmitter submitter, Dag dag, String groupId, String machineId,
      List<KaramelizedCookbook> rootCookbooks)
      throws KaramelException {
    Confs confs = Confs.loadKaramelConfs();
    String prepStoragesConf = confs.getProperty(Settings.PREPARE_STORAGES_KEY);
    String vendorPath = ClusterDefinitionService.makeVendorPath(rootCookbooks);
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      if (ge.getId().equals(groupId)) {
        for (MachineRuntime me : ge.getMachines()) {
          if (me.getVmId().equals(machineId)) {
            FindOsTypeTask findOs = new FindOsTypeTask(dag.getName(), me, clusterStats, submitter);
            dag.addTask(findOs);
            Provider provider = ClusterDefinitionService.getGroupProvider(cluster, ge.getName());
            boolean storagePreparation = (prepStoragesConf != null && prepStoragesConf.equalsIgnoreCase("true")
                && (provider instanceof Ec2));
            if (storagePreparation) {
              String model = ((Ec2) provider).getType();
              InstanceType instanceType = InstanceType.valueByModel(model);
              PrepareStoragesTask st
                  = new PrepareStoragesTask(dag.getName(), me, clusterStats, submitter,
                      instanceType.getStorageDevices());
              dag.addTask(st);
            }
            AptGetEssentialsTask t1 = new AptGetEssentialsTask(dag.getName(), me, clusterStats, submitter,
                storagePreparation);
            InstallChefdkTask t2 = new InstallChefdkTask(dag.getName(), me, clusterStats, submitter);
            MakeSoloRbTask t3 = new MakeSoloRbTask(dag.getName(), me, vendorPath, clusterStats, submitter);
            dag.addTask(t1);
            dag.addTask(t2);
            dag.addTask(t3);
          }
        }
      }
    }
  }
}
