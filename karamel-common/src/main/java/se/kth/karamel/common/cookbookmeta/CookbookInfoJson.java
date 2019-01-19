/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.common.cookbookmeta;

import java.util.List;

/**
 *
 * @author kamal
 */
public class CookbookInfoJson {
  private final String name;
  private final String description;
  private final String version;
  private final List<Attribute> attributes;
  private final List<Recipe> recipes;

  public CookbookInfoJson(MetadataRb metadataRb) {
    this.name = metadataRb.getName();
    this.description = metadataRb.getDescription();
    this.version = metadataRb.getVersion();
    this.attributes = metadataRb.getAttributes();
    this.recipes = metadataRb.getRecipes();
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getVersion() {
    return version;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public List<Recipe> getRecipes() {
    return recipes;
  }
}
