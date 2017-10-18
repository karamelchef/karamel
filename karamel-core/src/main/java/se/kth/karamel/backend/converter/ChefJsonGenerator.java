/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.converter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.json.JsonRecipe;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class ChefJsonGenerator {

    /**
   * Generates all purge chef-jsons per machine&purge pair through the following steps:
   *  1. root-json: makes an empty json object as root
   *  2. group-jsons: per group in the cluster clones a new json from the root json
   *     2.1 all cookbooks' attributes: adds all attributes related to all cookbooks in that group
   *     2.2 purge-json: clones the group-json and adds machine-ips and run-list for that recipe
   *     2.3 returns all generated jsons for all machine-cookbook-purge combination
   *
   * @param definition
   * @param clusterEntity
   * @return map of machineId-recipeName->json
   */
  public static Map<String, JsonObject> generateClusterChefJsonsForPurge(JsonCluster definition,
      ClusterRuntime clusterEntity) throws KaramelException {
    Map<String, JsonObject> chefJsons = new HashMap<>();
    JsonObject root = new JsonObject();
    for (GroupRuntime groupEntity : clusterEntity.getGroups()) {
      JsonObject clone = cloneJsonObject(root);
      JsonGroup jsonGroup = UserClusterDataExtractor.findGroup(definition, groupEntity.getName());
      //Adding all attribtues to all chef-jsons
      for (JsonCookbook cb : jsonGroup.getCookbooks()) {
        addCookbookAttributes(cb, clone);
      }
      for (JsonCookbook cb : jsonGroup.getCookbooks()) {
        Map<String, JsonObject> gj = generatePurgeChefJsons(clone, cb, groupEntity);
        chefJsons.putAll(gj);
      }
    }
    return chefJsons;
  }
  
  /**
   * Generates all chef-jsons per machine&recipe pair through the following steps:
   *  1. root-json: makes an empty json object as root
   *  2. all-ips: adds all recipe private and public ips into the root json
   *  3. group-jsons: per group in the cluster clones a new json from the root json
   *     3.1 all cookbooks' attributes: adds all attributes related to all cookbooks in that group
   *     3.2 recipe-json: clones the group-json per recipe and adds machine-ips and run-list for that recipe
   *     3.3 returns all generated jsons for all machine-cookbook-recipe combination
   *
   * @param definition
   * @param clusterEntity
   * @return map of machineId-recipeName->json
   */
  public static Map<String, JsonObject> generateClusterChefJsonsForInstallation(JsonCluster definition,
      ClusterRuntime clusterEntity) throws KaramelException {
    Map<String, JsonObject> chefJsons = new HashMap<>();
    JsonObject root = new JsonObject();
    aggregateIpAddresses(root, definition, clusterEntity);
    for (GroupRuntime groupEntity : clusterEntity.getGroups()) {
      JsonObject clone = cloneJsonObject(root);
      JsonGroup jsonGroup = UserClusterDataExtractor.findGroup(definition, groupEntity.getName());
      //Adding all attribtues to all chef-jsons
      for (JsonCookbook cb : jsonGroup.getCookbooks()) {
        addCookbookAttributes(cb, clone);
      }
      for (JsonCookbook cb : jsonGroup.getCookbooks()) {
        Map<String, JsonObject> gj = generateRecipesChefJsons(clone, cb, groupEntity);
        chefJsons.putAll(gj);
      }
    }
    return chefJsons;
  }

    /**
   * For a specific cookbook, generates chef-json of purge for all the combinations of machine-purge. 
   * @param json
   * @param cb
   * @param groupEntity
   * @return
   * @throws KaramelException 
   */
  public static Map<String, JsonObject> generatePurgeChefJsons(JsonObject json, JsonCookbook cb,
      GroupRuntime groupEntity) throws KaramelException {
    Map<String, JsonObject> groupJsons = new HashMap<>();

    for (MachineRuntime me : groupEntity.getMachines()) {
      String purgeRecipeName = cb.getName() + Settings.COOKBOOK_DELIMITER + Settings.PURGE_RECIPE;
      JsonObject clone = addMachineNRecipeToJson(json, me, purgeRecipeName);
      groupJsons.put(me.getId() + purgeRecipeName, clone);
    }
    return groupJsons;
  }
  
  /**
   * For a specific cookbook, generates all chef-jsons for all the combinations of machine-recipe. 
   * @param json
   * @param cb
   * @param groupEntity
   * @return
   * @throws KaramelException 
   */
  public static Map<String, JsonObject> generateRecipesChefJsons(JsonObject json, JsonCookbook cb,
      GroupRuntime groupEntity) throws KaramelException {
    Map<String, JsonObject> groupJsons = new HashMap<>();

    for (MachineRuntime me : groupEntity.getMachines()) {
      for (JsonRecipe recipe : cb.getRecipes()) {
        JsonObject clone = addMachineNRecipeToJson(json, me, recipe.getCanonicalName());
        groupJsons.put(me.getId() + recipe.getCanonicalName(), clone);
      }
      String installRecipeName = cb.getName() + Settings.COOKBOOK_DELIMITER + Settings.INSTALL_RECIPE;
      JsonObject clone = addMachineNRecipeToJson(json, me, installRecipeName);
      groupJsons.put(me.getId() + installRecipeName, clone);
    }
    return groupJsons;
  }

  public static JsonObject addMachineNRecipeToJson(JsonObject json, MachineRuntime me, String recipeName) {
    JsonObject clone = cloneJsonObject(json);
    addMachineIps(clone, me);
    addRunListForRecipe(clone, recipeName);
    return clone;
  }

  /**
   * Takes a machine-specific json with a recipe-name that has to be run on that machine, it then generates
   * the run_list section into the json with the recipe-name. 
   * @param chefJson
   * @param recipeName 
   */
  public static void addRunListForRecipe(JsonObject chefJson, String recipeName) {
    JsonArray jarr = new JsonArray();
    jarr.add(new JsonPrimitive(recipeName));
    chefJson.add(Settings.REMOTE_CHEFJSON_RUNLIST_TAG, jarr);
  }

  /**
   * It takes a machine-specfic json and adds private_ips and public_ips into it. 
   * @param json
   * @param machineEntity 
   */
  public static void addMachineIps(JsonObject json, MachineRuntime machineEntity) {
    JsonArray ips = new JsonArray();
    ips.add(new JsonPrimitive(machineEntity.getPrivateIp()));
    json.add("private_ips", ips);

    ips = new JsonArray();
    ips.add(new JsonPrimitive(machineEntity.getPublicIp()));
    json.add("public_ips", ips);

    JsonObject hosts = new JsonObject();
    hosts.add(machineEntity.getPublicIp(), new JsonPrimitive(machineEntity.getName()));
    json.add("hosts", hosts);
  }

  /**
   * It adds those attributes related to one cookbook into the json object. 
   * For example [ndb/ports=[123, 134, 145], ndb/DataMemory=111]
   * @param jc
   * @param root 
   */
  public static void addCookbookAttributes(JsonCookbook jc, JsonObject root) {
    Set<Map.Entry<String, Object>> entrySet = jc.getAttrs().entrySet();
    for (Map.Entry<String, Object> entry : entrySet) {
      String[] keyComps = entry.getKey().split(Settings.ATTR_DELIMITER);
      Object value = entry.getValue();
//      Object value = valStr;
//      if (valStr.startsWith("$")) {
//        if (valStr.contains(".")) {
//          value = cluster.getVariable(valStr.substring(1));
//        } else {
//          value = getVariable(valStr.substring(1));
//        }
//      }
      JsonObject o1 = root;
      for (int i = 0; i < keyComps.length; i++) {
        String comp = keyComps[i];
        if (i == keyComps.length - 1) {
          if (value instanceof Collection) {
            JsonArray jarr = new JsonArray();
            for (Object valElem : ((Collection) value)) {
              jarr.add(new JsonPrimitive(valElem.toString()));
            }
            o1.add(comp, jarr);
          } else {
            o1.addProperty(comp, value.toString());
          }
        } else {
          JsonElement o2 = o1.get(comp);
          if (o2 == null) {
            JsonObject o3 = new JsonObject();
            o1.add(comp, o3);
            o1 = o3;
          } else {
            o1 = o2.getAsJsonObject();
          }
        }
      }
    }
  }

  /**
   * Adds private_ips and public_ips of all machines per each recipe as an attribute. 
   * For example 
   *            hadoop::dn/private_ips: [192.168.0.1, 192.168.0.2]
   *            hadoop::dn/public_ips: [80.70.33.22, 80.70.33.23]
   * install recipes are ignored here
   * @param json
   * @param definition
   * @param clusterEntity 
   */
  public static void aggregateIpAddresses(JsonObject json, JsonCluster definition, ClusterRuntime clusterEntity) {
    Map<String, Set<String>> privateIps = new HashMap<>();
    Map<String, Set<String>> publicIps = new HashMap<>();
    Map<String, Map<String, String>> hosts = new HashMap<>();

    for (GroupRuntime ge : clusterEntity.getGroups()) {
      JsonGroup jg = UserClusterDataExtractor.findGroup(definition, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        for (JsonCookbook jc : jg.getCookbooks()) {
          for (JsonRecipe recipe : jc.getRecipes()) {
            if (!recipe.getCanonicalName().endsWith(Settings.COOKBOOK_DELIMITER + Settings.INSTALL_RECIPE)) {
              String privateAttr = recipe.getCanonicalName() + Settings.ATTR_DELIMITER + 
                  Settings.REMOTE_CHEFJSON_PRIVATEIPS_TAG;
              String publicAttr = recipe.getCanonicalName() + Settings.ATTR_DELIMITER + 
                  Settings.REMOTE_CHEFJSON_PUBLICIPS_TAG;
              String hostsAttr = recipe.getCanonicalName() + Settings.ATTR_DELIMITER + 
                  Settings.REMOTE_CHEFJSON_HOSTS_TAG;
              if (!privateIps.containsKey(privateAttr)) {
                privateIps.put(privateAttr, new HashSet<String>());
                publicIps.put(publicAttr, new HashSet<String>());
                hosts.put(hostsAttr, new HashMap<String, String>());
              }
              privateIps.get(privateAttr).add(me.getPrivateIp());
              publicIps.get(publicAttr).add(me.getPublicIp());
              hosts.get(hostsAttr).put(me.getPublicIp(), me.getName());
              hosts.get(hostsAttr).put(me.getPrivateIp(), me.getName());
            }
          }
        }
      }
    }
    attr2Json(json, privateIps);
    attr2Json(json, publicIps);
    attrMap2Json(json, hosts);
  }

  /**
   * It converts attributes into the json format and adds them into the root json object. 
   * For example hadoop::dn/hosts: {192.168.0.1: node-name} is converted into 
   * {"hadoop":{"dn":{"hosts": {"192.168.0.1": "node-name"},}}}
   * @param root
   * @param attrs 
   */
  public static void attrMap2Json(JsonObject root, Map<String, Map<String, String>> attrs) {
    for (Map.Entry<String, Map<String, String>> entry : attrs.entrySet()) {
      String[] keyComps = entry.getKey().split(Settings.COOKBOOK_DELIMITER + "|" + Settings.ATTR_DELIMITER);
      JsonObject o1 = root;
      for (int i = 0; i < keyComps.length; i++) {
        String comp = keyComps[i];
        if (i == keyComps.length - 1) {
          JsonObject jobj = new JsonObject();
          for (Map.Entry<String, String> e2 : entry.getValue().entrySet()) {
            jobj.add(e2.getKey(), new JsonPrimitive(e2.getValue().toString()));
          }
          o1.add(comp, jobj);
        } else {
          JsonElement o2 = o1.get(comp);
          if (o2 == null) {
            JsonObject o3 = new JsonObject();
            o1.add(comp, o3);
            o1 = o3;
          } else {
            o1 = o2.getAsJsonObject();
          }
        }
      }
    }
  }



  /**
   * It converts attributes into the json format and adds them into the root json object. 
   * For example hadoop::dn/private_ips: [192.168.0.1, 192.168.0.2] is converted into 
   * {"hadoop":{"dn":{"private_ips":["192.168.0.1", "192.168.0.2"]}}}
   * @param root
   * @param attrs 
   */
  public static void attr2Json(JsonObject root, Map<String, Set<String>> attrs) {
    Set<Map.Entry<String, Set<String>>> entrySet = attrs.entrySet();
    for (Map.Entry<String, Set<String>> entry : entrySet) {
      String[] keyComps = entry.getKey().split(Settings.COOKBOOK_DELIMITER + "|" + Settings.ATTR_DELIMITER);
      JsonObject o1 = root;
      for (int i = 0; i < keyComps.length; i++) {
        String comp = keyComps[i];
        if (i == keyComps.length - 1) {
          JsonArray jarr = new JsonArray();
          for (Object valElem : entry.getValue()) {
            jarr.add(new JsonPrimitive(valElem.toString()));
          }
          o1.add(comp, jarr);
        } else {
          JsonElement o2 = o1.get(comp);
          if (o2 == null) {
            JsonObject o3 = new JsonObject();
            o1.add(comp, o3);
            o1 = o3;
          } else {
            o1 = o2.getAsJsonObject();
          }
        }
      }
    }
  }

  public static JsonObject cloneJsonObject(JsonObject jo) {
    Gson gson = new Gson();
    JsonElement jelem = gson.fromJson(jo.toString(), JsonElement.class);
    JsonObject clone = jelem.getAsJsonObject();
    return clone;
  }

}
