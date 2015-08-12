/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.client.api.CookbookCache;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.cookbook.metadata.KaramelizedCookbook;

/**
 *
 * @author kamal
 */
public class JsonCookbook {

  String id;
  //values of attrs could be string or array of string 
  Map<String, Object> attrs = new HashMap<>();
  Set<JsonRecipe> recipes = new HashSet<>();
  @JsonIgnore
  KaramelizedCookbook karamelizedCookbook;

  public JsonCookbook() {
  }

  public JsonCookbook(String id, Map<String, Object> attrs) {
    this.id = id;
    this.attrs = attrs;
  }

  public String getName() throws KaramelException {
    return getKaramelizedCookbook().getMetadataRb().getName();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, Object> getAttrs() {
    return attrs;
  }

  public void setAttrs(Map<String, Object> attrs) {
    this.attrs = attrs;
  }

  public Set<JsonRecipe> getRecipes() {
    return recipes;
  }

  public void setRecipes(Set<JsonRecipe> recipes) {
    this.recipes = recipes;
  }

  public KaramelizedCookbook getKaramelizedCookbook() throws KaramelException {
    if (karamelizedCookbook == null) {
      karamelizedCookbook = CookbookCache.get(id);
    }
    return karamelizedCookbook;
  }

}
