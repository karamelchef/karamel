/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.cookbook.metadata.karamelfile.yaml;

import java.util.Collections;
import java.util.List;

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

  public void setRecipe(String recipe) {
    this.recipe = recipe;
  }

  public List<String> getGlobal() {
    return global;
  }

  public void setGlobal(List<String> global) {
    this.global = global;
  }

  public List<String> getLocal() {
    return local;
  }

  public void setLocal(List<String> local) {
    this.local = local;
  }
  
  
}
