/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model;

import java.util.HashMap;
import java.util.Map;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class Cluster extends Scope {

  private String name;
  private Map<String, Group> groups;
  private final Map<String, Cookbook> cookbooks = new HashMap<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Group> getGroups() {
    return groups;
  }

  public void setGroups(Map<String, Group> groups) {
    this.groups = groups;
  }

  public Map<String, Cookbook> getCookbooks() {
    return cookbooks;
  }

  public void setCookbooks(Map<String, Cookbook> cookbooks) {
    this.cookbooks.putAll(cookbooks);
  }

  @Override
  public String getAttr(String key) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void validate() throws ValidationException {
    super.validate();
    for (Group jg : groups.values()) {
      jg.validate();
    }
  }

}
