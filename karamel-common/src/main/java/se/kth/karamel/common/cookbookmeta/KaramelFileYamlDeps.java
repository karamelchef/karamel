/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public class KaramelFileYamlDeps {

  private String recipe;
  private List<String> local = Collections.EMPTY_LIST;
  private List<String> global = Collections.EMPTY_LIST;

  public String getRecipeCanonicalName() {
    return Settings.RECIPE_CANONICAL_NAME(recipe);
  }

  public String getRecipe() {
    return recipe;
  }

  public void setRecipe(String recipe) {
    this.recipe = recipe;
  }

  public List<String> getGlobal() {
    return global;
  }

  public void setGlobal(List<String> global) {
    this.global = new ArrayList<>();
    if (global != null) {
      for (String gl : global) {
        this.global.add(Settings.RECIPE_CANONICAL_NAME(gl));
      }
    } else {
      this.global = Collections.EMPTY_LIST;
    }
  }

  public List<String> getLocal() {
    return local;
  }

  public void setLocal(List<String> local) {
    this.local = new ArrayList<>();
    if (local != null) {
      for (String loc : local) {
        this.local.add(Settings.RECIPE_CANONICAL_NAME(loc));
      }
    }else {
      this.local = Collections.EMPTY_LIST;
    }
  }

}
