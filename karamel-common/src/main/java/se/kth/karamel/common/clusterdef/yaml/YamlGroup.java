package se.kth.karamel.common.clusterdef.yaml;

import java.util.List;
import java.util.stream.Collectors;

import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.json.JsonRecipe;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.ValidationException;

public class YamlGroup extends YamlScope {

  private int size;
  private List<String> recipes = null;

  public YamlGroup() {
  }

  YamlGroup(JsonGroup jsonGroup) throws MetadataParseException {
    super(jsonGroup);
    this.size = jsonGroup.getSize();
    attrs.putAll(jsonGroup.getAttributes());
    recipes = jsonGroup.getRecipes().stream().map(JsonRecipe::getCanonicalName).collect(Collectors.toList());
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public List<String> getRecipes() {
    return recipes;
  }

  public void setRecipes(List<String> recipes) {
    this.recipes = recipes;
  }

  @Override
  public void validate() throws ValidationException {
    super.validate();
    Baremetal baremetal = getBaremetal();
    if (baremetal != null) {
      int s1 = baremetal.retriveAllIps().size();
      if (s1 != size)
        throw new ValidationException(
            String.format("Number of ip addresses is not equal to the group size %d != %d", s1, size));
    }
  }

}
