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
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonCookbook;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.client.model.json.JsonRecipe;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class ChefJsonGenerator {

  /**
   * Generates all chef-jsons per machine&recipe pair
   *
   * @param definition
   * @param clusterEntity
   * @return map of machineId-recipeName->json
   */
  public static Map<String, JsonObject> generateClusterChefJsons(JsonCluster definition, 
      ClusterRuntime clusterEntity) throws KaramelException {
    Map<String, JsonObject> chefJsons = new HashMap<>();
    JsonObject root = new JsonObject();
    aggregateIpAddresses(root, definition, clusterEntity);
    for (GroupRuntime groupEntity : clusterEntity.getGroups()) {
      JsonObject clone = cloneJsonObject(root);
      JsonGroup jsonGroup = UserClusterDataExtractor.findGroup(definition, groupEntity.getName());
      for (JsonCookbook cb : jsonGroup.getCookbooks()) {
        Map<String, JsonObject> gj = generateRecipesChefJsons(clone, cb, groupEntity);
        chefJsons.putAll(gj);
      }
    }
    return chefJsons;
  }

  public static Map<String, JsonObject> generateRecipesChefJsons(JsonObject json, JsonCookbook cb, 
      GroupRuntime groupEntity) throws KaramelException {
    Map<String, JsonObject> groupJsons = new HashMap<>();
    addCookbookAttributes(cb, json);
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

  public static void addRunListForRecipe(JsonObject chefJson, String recipeName) {
    JsonArray jarr = new JsonArray();
    jarr.add(new JsonPrimitive(recipeName));
    chefJson.add(Settings.CHEF_JSON_RUNLIST_TAG, jarr);
  }

  public static void addMachineIps(JsonObject json, MachineRuntime machineEntity) {
    JsonArray ips = new JsonArray();
    ips.add(new JsonPrimitive(machineEntity.getPrivateIp()));
    json.add("private_ips", ips);

    ips = new JsonArray();
    ips.add(new JsonPrimitive(machineEntity.getPublicIp()));
    json.add("public_ips", ips);
  }

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

  public static void aggregateIpAddresses(JsonObject json, JsonCluster definition, ClusterRuntime clusterEntity) {
    Map<String, Set<String>> privateIps = new HashMap<>();
    Map<String, Set<String>> publicIps = new HashMap<>();
    for (GroupRuntime ge : clusterEntity.getGroups()) {
      JsonGroup jg = UserClusterDataExtractor.findGroup(definition, ge.getName());
      for (MachineRuntime me : ge.getMachines()) {
        for (JsonCookbook jc : jg.getCookbooks()) {
          for (JsonRecipe recipe : jc.getRecipes()) {
            if (!recipe.getCanonicalName().endsWith(Settings.COOKBOOK_DELIMITER + Settings.INSTALL_RECIPE)) {
              String privateAttr = recipe.getCanonicalName() + Settings.ATTR_DELIMITER + Settings.CHEF_PRIVATE_IPS;
              String publicAttr = recipe.getCanonicalName() + Settings.ATTR_DELIMITER + Settings.CHEF_PUBLIC_IPS;
              if (!privateIps.containsKey(privateAttr)) {
                privateIps.put(privateAttr, new HashSet<String>());
                publicIps.put(publicAttr, new HashSet<String>());
              }
              privateIps.get(privateAttr).add(me.getPrivateIp());
              publicIps.get(publicAttr).add(me.getPublicIp());
            }
          }
        }
      }
    }

    attr2Json(json, privateIps);
    attr2Json(json, publicIps);
  }

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
