/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.common.cookbookmeta;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author kamal
 */
public class Attribute {
  String name;
  String displayName;
  String type;
  String description;
  @SerializedName("default")
  Object defaultVal;
  String required;

  public Attribute(String name) {
    this.name = name;
  }

  public Attribute() { }

  public void setDefault(Object defaultVal) {
    this.defaultVal = defaultVal;
  }

  public Object getDefault() {
    return defaultVal;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setRequired(String required) {
    this.required = required;
  }

  public String getRequired() {
    return required;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Attribute) {
      return name.equals(((Attribute) o).getName());
    }

    return false;
  }
}
