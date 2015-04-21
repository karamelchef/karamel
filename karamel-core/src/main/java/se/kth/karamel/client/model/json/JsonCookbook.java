/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.json;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import se.kth.karamel.client.model.Cookbook;

/**
 *
 * @author kamal
 */
// TODO - Make this class thread-safe and mutable, so that results can be returned from recipes.
public class JsonCookbook extends Cookbook {

  String name;
  ConcurrentHashMap<String, String> attrs = new ConcurrentHashMap<>();
  ConcurrentSkipListSet<JsonRecipe> recipes = new ConcurrentSkipListSet<>();
  
  public JsonCookbook() {
  }

  public JsonCookbook(Cookbook cb, String name, Map<String, String> attrs) {
    super(cb);
    this.name = name;
    this.attrs.putAll(attrs);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ConcurrentHashMap<String, String> getAttrs() {
    return attrs;
  }

  public void setAttrs(Map<String, String> attrs) {
    this.attrs.clear();
    this.attrs.putAll(attrs);
  }

  public ConcurrentSkipListSet<JsonRecipe> getRecipes() {
    return recipes;
  }

  public void setRecipes(ConcurrentSkipListSet<JsonRecipe> recipes) {
    this.recipes.clear();
    this.recipes.addAll(recipes);
  }
  
  public void addAttr(String attr, String val) {
      this.attrs.put(attr, val);
  }
  
}