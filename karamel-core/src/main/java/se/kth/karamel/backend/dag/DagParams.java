/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.dag;

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jdowling
 */
public class DagParams {

  private static final Map<String, Map<String, Set<JsonElement>>> cookbooks = new HashMap<>();
  private static final Set<JsonElement> globalParams = new HashSet<>();

  public static synchronized void setGlobalParams(JsonElement obj) {
    globalParams.add(obj);
  }

  public static synchronized Set<JsonElement> getGlobalParams() {
    if (globalParams.isEmpty()) {
      return null;
    }
    return globalParams;
  }

  public static synchronized void setLocalParams(String cookbook, String recipe, JsonElement obj) {
    Map<String, Set<JsonElement>> recipes = cookbooks.get(cookbook);
    if (recipes == null) {
      recipes = new HashMap<>();
      cookbooks.put(cookbook, recipes);
    }
    Set<JsonElement> params = recipes.get(recipe);
    if (params == null) {
      params = new HashSet<>();
    }
    params.add(obj);
    recipes.put(recipe, params);
  }

  public static synchronized Set<JsonElement> getLocalParams(String cookbook, String recipe) {
    if (cookbooks.containsKey(cookbook)) {
      if (cookbooks.get(cookbook).get(recipe) != null || cookbooks.get(cookbook).get(recipe).size() > 0) {
        return cookbooks.get(cookbook).get(recipe);
      }
    }
    return null;
  }
}
