/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.dag.TaskRunner;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.running.model.ClusterEntity;
import se.kth.karamel.backend.running.model.GroupEntity;
import se.kth.karamel.backend.running.model.MachineEntity;
import se.kth.karamel.backend.running.model.tasks.AptGetEssentialsTask;
import se.kth.karamel.backend.running.model.tasks.InstallBerkshelfTask;
import se.kth.karamel.backend.running.model.tasks.MakeSoloRbTask;
import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.backend.running.model.tasks.VendorCookbookTask;
import se.kth.karamel.client.api.CookbookCache;
import se.kth.karamel.common.Settings;
import se.kth.karamel.client.model.Ec2;
import se.kth.karamel.client.model.Provider;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonCookbook;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.client.model.json.JsonRecipe;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.cookbook.metadata.GithubCookbook;
import se.kth.karamel.cookbook.metadata.GithubUrls;
import se.kth.karamel.cookbook.metadata.karamelfile.yaml.YamlDependency;

/**
 *
 * @author kamal
 */
public class UserClusterDataExtractor {

  public static int totalMachines(JsonCluster cluster) {
    int total = 0;
    for (JsonGroup g : cluster.getGroups()) {
      total += g.getSize();
    }
    return total;
  }

  public static JsonGroup findGroup(JsonCluster cluster, String groupName) {
    for (JsonGroup g : cluster.getGroups()) {
      if (g.getName().equals(groupName)) {
        return g;
      }
    }
    return null;
  }

  public static Provider getGroupProvider(JsonCluster cluster, String groupName) {
    JsonGroup group = findGroup(cluster, groupName);
    Provider groupScopeProvider = group.getProvider();
    Provider clusterScopeProvider = cluster.getProvider();
    Provider provider = null;
    if (groupScopeProvider == null && clusterScopeProvider == null) {
      provider = Ec2.makeDefault();
    } else if (groupScopeProvider == null && clusterScopeProvider != null) {
      provider = (Provider) clusterScopeProvider.cloneMe();
      provider = provider.applyDefaults();
    } else if (groupScopeProvider != null && clusterScopeProvider != null) {
      provider = groupScopeProvider.applyParentScope(groupScopeProvider);
      provider = provider.applyDefaults();
    }
    return provider;
  }

  /**
   *
   * @param cluster
   * @param clusterEntity
   * @param monitor
   * @param chefJsons
   * @return
   * @throws KaramelException
   */
  public static Dag getInstallationDag(JsonCluster cluster, ClusterEntity clusterEntity, MachinesMonitor monitor, Map<String, JsonObject> chefJsons) throws KaramelException {
    Map<String, Task> mlts = machineLevelTasks(cluster, clusterEntity);
    Map<String, Map<String, Task>> clts = cookbookLevelTasks(cluster, clusterEntity, chefJsons);
    Map<String, Map<String, Task>> rlts = recipeLevelTasks(cluster, clusterEntity, chefJsons);
    Map<String, MachineEntity> allMachines = new HashMap<>();
    for (GroupEntity ge : clusterEntity.getGroups()) {
      for (MachineEntity me : ge.getMachines()) {
        allMachines.put(me.getId(), me);
      }
    }
    Map<String, TaskRunner> allTasks = new HashMap<>();
    Dag dag = new Dag();
    for (GroupEntity ge : clusterEntity.getGroups()) {
      JsonGroup jg = findGroup(cluster, ge.getName());
      for (MachineEntity me : ge.getMachines()) {
        String mid = me.getId();
        String id1 = AptGetEssentialsTask.makeUniqueId(mid);
        Task t1 = mlts.get(id1);
        TaskRunner r1 = getTaskRunner(t1, monitor, allTasks, allMachines);
        String id2 = InstallBerkshelfTask.makeUniqueId(mid);
        Task t2 = mlts.get(id2);
        TaskRunner r2 = getTaskRunner(t2, monitor, allTasks, allMachines);
        String id3 = MakeSoloRbTask.makeUniqueId(mid);
        Task t3 = mlts.get(id3);
        TaskRunner r3 = getTaskRunner(t3, monitor, allTasks, allMachines);
        dag.insert(r1);
        dag.insert(r2, r1);
        dag.insert(r3, r2);
        Map<String, Task> cblt = clts.get(mid);
        for (JsonCookbook cb : jg.getCookbooks()) {
          GithubUrls urls = cb.getUrls();
          String id4 = VendorCookbookTask.makeUniqueId(mid, urls.id);
          Task t4 = cblt.get(id4);
          TaskRunner r4 = getTaskRunner(t4, monitor, allTasks, allMachines);
          dag.insert(r4, r3);
          String id5 = RunRecipeTask.makeUniqueId(mid, cb.getName() + Settings.COOOKBOOK_DELIMITER + Settings.INSTALL_RECIPE);
          Task t5 = cblt.get(id5);
          TaskRunner r5 = getTaskRunner(t5, monitor, allTasks, allMachines);
          dag.insert(r5, r4);
          GithubCookbook ghcb = CookbookCache.get(cb.getUrls().id);

          for (JsonRecipe rec : cb.getRecipes()) {
            String id6 = RunRecipeTask.makeUniqueId(mid, rec.getName());
            Map<String, Task> rlt = rlts.get(rec.getName());
            Task t6 = rlt.get(id6);
            TaskRunner r6 = getTaskRunner(t6, monitor, allTasks, allMachines);
            Set<TaskRunner> deps = new HashSet<>();
            deps.add(r5);
            YamlDependency depenency = ghcb.getKaramelFile().getDepenency(rec.getName());
            if (depenency != null) {
              for (String localRec : depenency.getLocal()) {
                Map<String, Task> rlt2 = rlts.get(localRec);
                String id7 = RunRecipeTask.makeUniqueId(mid, localRec);
                Task t7 = rlt2.get(id7);
                TaskRunner r7 = getTaskRunner(t7, monitor, allTasks, allMachines);
                if (t7 != null) {
                  deps.add(r7);
                }
              }

              for (String globRec : depenency.getGlobal()) {
                Map<String, Task> rlt2 = rlts.get(globRec);
                for (Map.Entry<String, Task> entry : rlt2.entrySet()) {
                  Task t7 = entry.getValue();
                  TaskRunner r7 = getTaskRunner(t7, monitor, allTasks, allMachines);
                  deps.add(r7);
                }
              }
            }
            dag.insert(r6, deps);
          }
        }
      }
    }

    return dag;
  }

  private static TaskRunner getTaskRunner(Task t, MachinesMonitor monitor, Map<String, TaskRunner> allTasks, Map<String, MachineEntity> allMachines) {
    TaskRunner r1 = allTasks.get(t.uniqueId());
    if (r1 == null) {
      MachineEntity me = allMachines.get(t.getMachineId());
      me.addTask(t);
      r1 = new TaskRunner(t, monitor);
      allTasks.put(t.uniqueId(), r1);
    }

    return r1;
  }

  /**
   *
   * @param cluster
   * @param clusterEntity
   *
   * @param chefJsons
   * @return two level mapping of (recipeName -> taskId -> task)
   */
  public static Map<String, Map<String, Task>> recipeLevelTasks(JsonCluster cluster, ClusterEntity clusterEntity, Map<String, JsonObject> chefJsons) {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupEntity ge : clusterEntity.getGroups()) {
      JsonGroup jg = findGroup(cluster, ge.getName());
      for (MachineEntity me : ge.getMachines()) {
        for (JsonCookbook jc : jg.getCookbooks()) {
          String installRecipeName = jc.getName() + Settings.COOOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
          JsonObject json = chefJsons.get(me.getId() + installRecipeName);
          makeRecipeTask(installRecipeName, me.getId(), map, json);
          for (JsonRecipe rec : jc.getRecipes()) {
            JsonObject json1 = chefJsons.get(me.getId() + rec.getName());
            makeRecipeTask(rec.getName(), me.getId(), map, json1);
          }
        }
      }
    }
    return map;
  }

  private static RunRecipeTask makeRecipeTask(String recipeName, String machineId, Map<String, Map<String, Task>> map, JsonObject chefJson) {
    RunRecipeTask t1 = makeRecipeTask(recipeName, machineId, chefJson);
    Map<String, Task> map1 = map.get(recipeName);
    if (map1 == null) {
      map1 = new HashMap<>();
      map.put(recipeName, map1);
    }
    map1.put(t1.uniqueId(), t1);
    return t1;
  }

  private static RunRecipeTask makeRecipeTask(String recipeName, String machineId, JsonObject chefJson) {
    ChefJsonGenerator.addRunListForRecipe(chefJson, recipeName);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String jsonString = gson.toJson(chefJson);
    return new RunRecipeTask(machineId, recipeName, jsonString);
  }

  /**
   *
   * @param cluster
   * @param clusterEntity
   * @param chefJsons
   * @return two level mapping of (machineId -> taskId -> task)
   * @throws KaramelException
   */
  public static Map<String, Map<String, Task>> cookbookLevelTasks(JsonCluster cluster, ClusterEntity clusterEntity, Map<String, JsonObject> chefJsons) throws KaramelException {
    Map<String, Map<String, Task>> map = new HashMap<>();
    for (GroupEntity ge : clusterEntity.getGroups()) {
      JsonGroup jg = findGroup(cluster, ge.getName());
      for (MachineEntity me : ge.getMachines()) {
        Map<String, Task> map1 = new HashMap<>();
        for (JsonCookbook jc : jg.getCookbooks()) {
          GithubUrls urls = jc.getUrls();
          VendorCookbookTask t1 = new VendorCookbookTask(me.getId(), urls.id, Settings.COOKBOOKS_ROOT_VENDOR_PATH, urls.repoName, urls.home, urls.branch);
          map1.put(t1.uniqueId(), t1);
          String recipeName = jc.getName() + Settings.COOOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
          JsonObject json = chefJsons.get(me.getId() + recipeName);
          RunRecipeTask t2 = makeRecipeTask(recipeName, me.getId(), json);
          map1.put(t2.uniqueId(), t2);
        }
        map.put(me.getId(), map1);
      }
    }
    return map;
  }

  /**
   *
   * @param cluster
   * @param clusterEntity
   * @return map of taskId -> task
   */
  public static Map<String, Task> machineLevelTasks(JsonCluster cluster, ClusterEntity clusterEntity) throws KaramelException {
    Map<String, Task> map = new HashMap<>();
    String vendorPath = makeVendorPath(cluster);
    for (GroupEntity ge : clusterEntity.getGroups()) {
      for (MachineEntity me : ge.getMachines()) {
        AptGetEssentialsTask t1 = new AptGetEssentialsTask(me.getId());
        map.put(t1.uniqueId(), t1);
        InstallBerkshelfTask t2 = new InstallBerkshelfTask(me.getId());
        map.put(t2.uniqueId(), t2);
        MakeSoloRbTask t3 = new MakeSoloRbTask(me.getId(), vendorPath);
        map.put(t3.uniqueId(), t3);
      }
    }
    return map;
  }

  public static String makeVendorPath(JsonCluster cluster) throws KaramelException {
    Set<String> paths = new HashSet<>();
    for (JsonGroup gr : cluster.getGroups()) {
      for (JsonCookbook cb : gr.getCookbooks()) {
        GithubUrls urls = cb.getUrls();
        paths.add(Settings.COOKBOOKS_ROOT_VENDOR_PATH + Settings.SLASH + urls.repoName + Settings.SLASH + Settings.COOKBOOKS_VENDOR_SUBFOLDER);
      }
    }
    Object[] arr = paths.toArray();
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buffer.append("\n");
      }
      buffer.append("\"");
      buffer.append(arr[i]);
      buffer.append("\"");
      if (i < paths.size() - 1) {
        buffer.append(",");
      }
    }
    return buffer.toString();
  }

}
