/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model;

import java.util.HashSet;
import java.util.Set;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class Group extends Scope {

  private String name;
  private String size;
  private final Set<String> recipes = new HashSet<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public Set<String> getRecipes() {
    return recipes;
  }

  public void setRecipes(Set<String> recipes) {
    for (String recipe : recipes) {
      setRecipe(recipe);
    }
  }

  public void setRecipe(String recipe) {
    this.recipes.add(recipe);
  }

  @Override
  public String getAttr(String key) {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  @Override
  public void validate() throws ValidationException {
    super.validate();
  }

}
