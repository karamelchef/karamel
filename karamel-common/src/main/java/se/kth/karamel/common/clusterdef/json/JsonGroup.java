/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.cookbookmeta.Attribute;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
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
    // We are doing the same work several times here
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
          break;
        }
      }
      if (cookbook == null) {
        throw new RecipeNotfoundException(String.format("Opps!! Import cookbook for '%s'", rec));
      }
      JsonRecipe jsonRecipe = new JsonRecipe();
      jsonRecipe.setName(rec);
      cookbook.getRecipes().add(jsonRecipe);
    }

    getCookbooks().removeIf(jsonCb -> jsonCb.getRecipes().isEmpty());

    Map<String, Object> groupAttrs = new HashMap<>(group.flattenAttrs());
    Set<String> usedAttributes = new HashSet<>();
    for (JsonCookbook jc : getCookbooks()) {
      KaramelizedCookbook kcb = jc.getKaramelizedCookbook();
      Set<Attribute> allValidAttrs = new HashSet<>(kcb.getMetadataRb().getAttributes());
      for (KaramelizedCookbook depKcb : kcb.getDependencies()) {
        allValidAttrs.addAll(depKcb.getMetadataRb().getAttributes());
      }

      // I think that this map should be <String, Attribute>. But I don't want to see
      // what happen if I change it.
      Map<String, Object> validUsedAttrs = new HashMap<>();
      for (String usedAttr: groupAttrs.keySet()) {
        if (allValidAttrs.contains(new Attribute(usedAttr))) {
          validUsedAttrs.put(usedAttr, groupAttrs.get(usedAttr));
          usedAttributes.add(usedAttr);
        }
      }
      jc.setAttrs(validUsedAttrs);
    }

    if (!usedAttributes.containsAll(groupAttrs.keySet())){
      Set<String> invalidAttrs = groupAttrs.keySet();
      invalidAttrs.removeAll(usedAttributes);
      throw new KaramelException(String.format("Undefined attributes: %s", invalidAttrs.toString()));
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
    if (!name.matches(Settings.AWS_GEOUPNAME_PATTERN)) {
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
