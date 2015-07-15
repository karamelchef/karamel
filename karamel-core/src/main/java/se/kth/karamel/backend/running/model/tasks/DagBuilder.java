/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ChefJsonGenerator;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.client.api.CookbookCache;
import se.kth.karamel.client.model.Baremetal;
import se.kth.karamel.client.model.Provider;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonCookbook;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.client.model.json.JsonRecipe;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.DagConstructionException;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.cookbook.metadata.CookbookUrls;
import se.kth.karamel.cookbook.metadata.KaramelizedCookbook;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlDependency;

/**
 *
 * @author kamal
 */
public class DagBuilder {

  private static final Logger logger = Logger.getLogger(DagBuilder.class);

  public static Dag getInstallationDag(JsonCluster cluster, ClusterRuntime clusterEntity, TaskSubmitter submitter,
      Map<String, JsonObject> chefJsons) throws KaramelException {
    Dag dag = new Dag();
    Map<String, RunRecipeTask> allRecipeTasks = new HashMap<>();
    machineLevelTasks(cluster, clusterEntity, submitter, dag);
    cookbookLevelTasks(cluster, clusterEntity, chefJsons, submitter, allRecipeTasks, dag);
    Map<String, Map<String, Task>> rlts = recipeLevelTasks(cluster, clusterEntity, chefJsons, submitter, allRecipeTasks,
        dag);
    updateKaramelDependecnies(allRecipeTasks, dag, rlts);
    return dag;
  }

  private static boolean updateKaramelDependecnies(Map<String, RunRecipeTask> allRecipeTasks, Dag dag, 
      Map<String, Map<String, Task>> rlts) throws KaramelException {
    boolean newDepFound = false;
    for (RunRecipeTask task : allRecipeTasks.values()) {
      String tid = task.uniqueId();
      KaramelizedCookbook kcb = CookbookCache.get(task.getCookbookId());
      YamlDependency depenency = kcb.getKaramelFile().getDepenency(task.getRecipeCanonicalName());
      if (depenency != null) {
        for (String depRec : depenency.getLocal()) {
          String depId = RunRecipeTask.makeUniqueId(task.getMachineId(), depRec);
          newDepFound |= dag.addDependency(depId, tid);
        }

        for (String depRec : depenency.getGlobal()) {
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
   * Creates all recipe tasks for cluster and groups them by recipe-name. In other words, by having a recipe-name 
   * such as hadoop::dn you fetch all the tasks in the cluster that are running hadoop::dn. 
   * recipeName -> taskid(recipe + machineid) -> task
   *
   * @param cluster
   * @param clusterEntity
   * @param chefJsons
   * @param submitter
   * @param allRecipeTasks
   * @param dag
   * @return
   * @throws KaramelException
   */
  public static Map<String, Map<String, Task>> recipeLevelTasks(JsonCluster cluster, ClusterRuntime clusterEntity,
      Map<String, JsonObject> chefJsons, TaskSubmitter submitter, Map<String, RunRecipeTask> allRecipeTasks,
      Dag dag) throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      JsonGroup jg = UserClusterDataExtractor.findGroup(cluster, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        for (JsonCookbook jc : jg.getCookbooks()) {
          CookbookUrls urls = jc.getUrls();
          for (JsonRecipe rec : jc.getRecipes()) {
            JsonObject json1 = chefJsons.get(me.getId() + rec.getCanonicalName());
            addRecipeTaskForMachineIntoRecipesMap(rec.getCanonicalName(), me, map, json1, submitter, urls.id, 
                jc.getName(), allRecipeTasks, dag);
          }
        }
      }
    }
    return map;
  }

  /*
   * Makes sure recipe-task for machine exists both in the DAG and in the grouping map of recipes
   */
  private static RunRecipeTask addRecipeTaskForMachineIntoRecipesMap(String recipeName, MachineRuntime machine,
      Map<String, Map<String, Task>> map, JsonObject chefJson, TaskSubmitter submitter,
      String cookbookId, String cookbookName, Map<String, RunRecipeTask> allRecipeTasks, Dag dag)
      throws DagConstructionException {
    RunRecipeTask t1 = makeRecipeTaskIfNotExist(recipeName, machine, chefJson, submitter, cookbookId, cookbookName,
        allRecipeTasks, dag);
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
  private static RunRecipeTask makeRecipeTaskIfNotExist(String recipeName, MachineRuntime machine, JsonObject chefJson,
      TaskSubmitter submitter, String cookbookId, String cookbookName, Map<String, RunRecipeTask> allRecipeTasks,
      Dag dag) throws DagConstructionException {
    String recId = RunRecipeTask.makeUniqueId(machine.getId(), recipeName);
    RunRecipeTask runRecipeTask = allRecipeTasks.get(recId);
    if (!allRecipeTasks.containsKey(recId)) {
      ChefJsonGenerator.addRunListForRecipe(chefJson, recipeName);
      GsonBuilder builder = new GsonBuilder();
      builder.disableHtmlEscaping();
      Gson gson = builder.setPrettyPrinting().create();
      String jsonString = gson.toJson(chefJson);
      runRecipeTask = new RunRecipeTask(machine, recipeName, jsonString, submitter, cookbookId, cookbookName);
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
   * @param chefJsons
   * @param submitter
   * @param allRecipeTasks
   * @param dag
   * @return
   * @throws KaramelException
   */
  public static Map<String, Map<String, Task>> cookbookLevelTasks(JsonCluster cluster, ClusterRuntime clusterEntity,
      Map<String, JsonObject> chefJsons, TaskSubmitter submitter, Map<String, RunRecipeTask> allRecipeTasks,
      Dag dag) throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      JsonGroup jg = UserClusterDataExtractor.findGroup(cluster, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        Map<String, Task> map1 = new HashMap<>();
        for (JsonCookbook jc : jg.getCookbooks()) {
          CookbookUrls urls = jc.getUrls();
          VendorCookbookTask t1 = new VendorCookbookTask(me, submitter, urls.id, Settings.COOKBOOKS_ROOT_VENDOR_PATH,
              urls.repoName, urls.home, urls.branch);
          dag.addTask(t1);
          map1.put(t1.uniqueId(), t1);
          String recipeName = jc.getName() + Settings.COOOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
          JsonObject json = chefJsons.get(me.getId() + recipeName);
          RunRecipeTask t2 = makeRecipeTaskIfNotExist(recipeName, me, json, submitter, urls.id, jc.getName(), 
              allRecipeTasks, dag);
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

  public static void machineLevelTasks(JsonCluster cluster, ClusterRuntime clusterEntity, TaskSubmitter submitter,
      Dag dag) throws KaramelException {
    String vendorPath = UserClusterDataExtractor.makeVendorPath(cluster);
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      for (MachineRuntime me : ge.getMachines()) {

        Provider p = UserClusterDataExtractor.getGroupProvider(cluster, ge.getName());
        if (p instanceof Baremetal) {
          Baremetal baremetal = (Baremetal) p;
          SudoPasswordCheckTask t0 = new SudoPasswordCheckTask(me, submitter);
          dag.addTask(t0);
        }
        AptGetEssentialsTask t1 = new AptGetEssentialsTask(me, submitter);
        InstallBerkshelfTask t2 = new InstallBerkshelfTask(me, submitter);
        MakeSoloRbTask t3 = new MakeSoloRbTask(me, vendorPath, submitter);
        dag.addTask(t1);
        dag.addTask(t2);
        dag.addTask(t3);
      }
    }
  }

}
