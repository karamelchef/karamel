/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.client.model.json;

import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class JsonRecipe implements Comparable<JsonRecipe>{

  String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getCanonicalName() {
    return Settings.RECIPE_CANONICAL_NAME(name);
  }

  @Override
  public int compareTo(JsonRecipe o) {
    return getCanonicalName().compareTo(o.getCanonicalName());
  }
    
}
