package se.kth.karamel.common.clusterdef.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.cookbookmeta.Attribute;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.RecipeNotfoundException;
import se.kth.karamel.common.clusterdef.yaml.YamlGroup;
import se.kth.karamel.common.exception.ValidationException;

public class JsonGroup extends JsonScope {

  private String name;
  private int size;

  private List<JsonRecipe> recipes = new ArrayList<>();

  public JsonGroup() {
  }

  public JsonGroup(YamlGroup group, String name) throws KaramelException {
    super(group);
    setName(name);
    this.size = group.getSize();

    List<String> clusterDefRecipes = group.getRecipes();
    for (String rec : clusterDefRecipes) {
      String[] comp = rec.split(Settings.COOKBOOK_DELIMITER);
      KaramelizedCookbook cookbook = null;
      for (KaramelizedCookbook cb : getCookbooks()) {
        if (cb.getCookbookName().equals(comp[0])) {
          cookbook = cb;
          break;
        }
      }
      if (cookbook == null) {
        throw new RecipeNotfoundException(String.format("Opps!! Import cookbook for '%s'", rec));
      } else {
        recipes.add(new JsonRecipe(cookbook, comp.length == 2 ? comp[1] : "default"));
        cookbooks.add(cookbook);
      }
    }

    attributes = new HashMap<>(group.flattenAttrs());
    Set<Attribute> allValidAttrs = new HashSet<>();
    for (KaramelizedCookbook kcb : cookbooks) {
      allValidAttrs.addAll(kcb.getMetadataRb().getAttributes());
    }

    // I think that this map should be <String, Attribute>. But I don't want to see
    // what happen if I change it.
    Set<String> invalidAttrs = new HashSet<>();

    for (String usedAttr: attributes.keySet()) {
      if (!allValidAttrs.contains(new Attribute(usedAttr))) {
        invalidAttrs.add(usedAttr);
      }
    }

    if (!invalidAttrs.isEmpty()) {
      throw new KaramelException(String.format("Undefined attributes: %s", invalidAttrs.toString()));
    }
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

  public List<JsonRecipe> getRecipes() {
    return recipes;
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
