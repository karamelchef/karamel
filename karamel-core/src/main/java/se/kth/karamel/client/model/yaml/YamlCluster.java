/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.kth.karamel.client.model.Cookbook;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonCookbook;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.exception.MetadataParseException;

/**
 *
 * @author kamal
 */
public class YamlCluster extends YamlScope {

  private String name;
  private Map<String, YamlGroup> groups = new HashMap<>();
  private final Map<String, Cookbook> cookbooks = new HashMap<>();

  public YamlCluster() {
  }

  public YamlCluster(JsonCluster jsonCluster) throws MetadataParseException {
    super(jsonCluster);
    this.name = jsonCluster.getName();
    List<JsonGroup> jsonGroups = jsonCluster.getGroups();
    for (JsonGroup jsonGroup : jsonGroups) {
      YamlGroup yamlGroup = new YamlGroup(jsonGroup);
      groups.put(jsonGroup.getName(), yamlGroup);
    }
    List<JsonCookbook> jsonCookbooks = jsonCluster.getCookbooks();
    for (JsonCookbook jck : jsonCookbooks) {
      Cookbook ck = new Cookbook(jck);
      cookbooks.put(jck.getName(), ck);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, YamlGroup> getGroups() {
    return groups;
  }

  public void setGroups(Map<String, YamlGroup> groups) {
    this.groups = groups;
  }

  public Map<String, Cookbook> getCookbooks() {
    return cookbooks;
  }

  public void setCookbooks(Map<String, Cookbook> cookbooks) {
    this.cookbooks.putAll(cookbooks);
  }

}
