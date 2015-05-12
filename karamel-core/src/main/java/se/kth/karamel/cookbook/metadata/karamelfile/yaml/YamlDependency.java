/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata.karamelfile.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class YamlDependency {

  private String recipe;
  private List<String> local = Collections.EMPTY_LIST;
  private List<String> global = Collections.EMPTY_LIST;

  public String getRecipe() {
    return recipe;
  }

  public String getRecipeCanonicalName() {
    return Settings.RECIPE_CANONICAL_NAME(recipe);
  }

  public void setRecipe(String recipe) {
    this.recipe = recipe;
  }

  public List<String> getGlobal() {
    return global;
  }

  public void setGlobal(List<String> global) {
    this.global = new ArrayList<>();
    for (String gl : global) {
      this.global.add(Settings.RECIPE_CANONICAL_NAME(gl));
    }
  }

  public List<String> getLocal() {
    return local;
  }

  public void setLocal(List<String> local) {
    this.local = new ArrayList<>();
    for (String loc : local) {
      this.local.add(Settings.RECIPE_CANONICAL_NAME(loc));
    }
  }

}
