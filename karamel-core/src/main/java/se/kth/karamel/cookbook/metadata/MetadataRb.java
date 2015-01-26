/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Chef metadata.rb file
 *
 * @author kamal
 */
public class MetadataRb {

  String url;
  String name;
  String description;
  String version;
  List<Recipe> recipes = new ArrayList<>();
  List<Attribute> attributes = new ArrayList<>();

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setRecipes(List<Recipe> recipes) {
    this.recipes = recipes;
  }

  public List<Recipe> getRecipes() {
    return recipes;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  public void setDefaults(DefaultRb defaultRb) {
    for (Attribute attr : attributes) {
      if (defaultRb.getValue(attr.getName()) != null) {
        attr.setDefault(defaultRb.getValue(attr.getName()));
      }
    }
  }

}
