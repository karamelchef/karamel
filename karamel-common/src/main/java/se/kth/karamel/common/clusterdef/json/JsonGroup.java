/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.RecipeNotfoundException;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlGroup;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class JsonGroup extends JsonScope {

  private String name;
  private int size;

  public JsonGroup() {
  }

  public JsonGroup(YamlCluster cluster, YamlGroup group, String name) throws KaramelException {
    super(cluster, group);
    setName(name);
    this.size = group.getSize();
    List<String> recipes = group.getRecipes();
    for (String rec : recipes) {
      String[] comp = rec.split(Settings.COOKBOOK_DELIMITER);
      JsonCookbook cookbook = null;
      for (JsonCookbook cb : getCookbooks()) {
        if (cb.getName().equals(comp[0])) {
          cookbook = cb;
        }
      }
      if (cookbook == null) {
        throw new RecipeNotfoundException(String.format("Opps!! Import cookbook for '%s' ಠ_ಠ", rec));
      }
      JsonRecipe jsonRecipe = new JsonRecipe();
      jsonRecipe.setName(rec);
      cookbook.getRecipes().add(jsonRecipe);
    }
    List<JsonCookbook> cookbooks = new ArrayList<>();
    cookbooks.addAll(getCookbooks());
    for (JsonCookbook cb : cookbooks) {
      if (cb.getRecipes().isEmpty()) {
        getCookbooks().remove(cb);
      }
    }
    Map<String, Object> attrs = new HashMap<>();
    attrs.putAll(group.flattenAttrs());
    for (JsonCookbook jc : cookbooks) {
      Map<String, Object> attrs1 = jc.getAttrs();
      for (Map.Entry<String, Object> entry : attrs1.entrySet()) {
        String key = entry.getKey();
        if (attrs.containsKey(key)) {
          attrs.remove(key);
        }
      }
    }

    if (!attrs.isEmpty()) {
      throw new KaramelException(String.format("Undefined attributes: %s", attrs.keySet().toString()));
    }
  }

  public Set<String> flattenRecipes() {
    Set<String> recipes = new HashSet<>();
    for (JsonCookbook cb : getCookbooks()) {
      Set<JsonRecipe> recipes1 = cb.getRecipes();
      for (JsonRecipe jsonRecipe : recipes1) {
        recipes.add(jsonRecipe.getCanonicalName());
      }
    }
    return recipes;
  }

  public String getName() {
    return name;
  }

  public final void setName(String name) throws ValidationException {
    if (!name.matches(Settings.EC2_GEOUPNAME_PATTERN)) {
      throw new ValidationException("Group name '%s' must start with letter/number and just lowercase ASCII letters, "
          + "numbers and dashes are accepted in the name.");
    }
    this.name = name;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public void validate() throws ValidationException {
    super.validate();
    Baremetal baremetal = getBaremetal();
    if (baremetal != null) {
      int s1 = baremetal.retriveAllIps().size();
      if (s1 != size) {
        throw new ValidationException(
            String.format("Number of ip addresses is not equal to the group size %d != %d", s1, size));
      }
    }
  }

}
