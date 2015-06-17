/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import java.util.ArrayList;
import java.util.List;
import se.kth.karamel.client.model.Baremetal;
import se.kth.karamel.client.model.Provider;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class YamlGroup extends YamlScope {

  private int size;
  private final List<String> recipes = new ArrayList<>();

  public YamlGroup() {
  }

  YamlGroup(JsonGroup jsonGroup) throws MetadataParseException {
    super(jsonGroup);
    this.size = jsonGroup.getSize();
    recipes.addAll(jsonGroup.flattenRecipes());
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
    for (String recipe : recipes) {
      setRecipe(recipe);
    }
  }

  public void setRecipe(String recipe) {
    this.recipes.add(recipe);
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
