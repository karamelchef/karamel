/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;

/**
 *
 * @author kamal
 */
public class YamlCluster extends YamlScope {

  private String name;
  private boolean useContainers;
  private int hosts;
  private Map<String, YamlGroup> groups = new HashMap<>();
  private final Map<String, Cookbook> cookbooks = new HashMap<>();

  public YamlCluster() {
  }

  public YamlCluster(JsonCluster jsonCluster) throws MetadataParseException, KaramelException {
    super(jsonCluster);
    this.name = jsonCluster.getName();
    this.useContainers = jsonCluster.getUseContainers();
    this.hosts = jsonCluster.getHosts();
    List<JsonGroup> jsonGroups = jsonCluster.getGroups();
    List<JsonCookbook> allCbs = new ArrayList<>();
    for (JsonGroup jsonGroup : jsonGroups) {
      YamlGroup yamlGroup = new YamlGroup(jsonGroup);
      allCbs.addAll(jsonGroup.getCookbooks());
      groups.put(jsonGroup.getName(), yamlGroup);
    }
    allCbs.addAll(jsonCluster.getCookbooks());
    for (JsonCookbook jck : allCbs) {
      Cookbook ck = new Cookbook();
      KaramelizedCookbook kc = jck.getKaramelizedCookbook();
      ck.setBranch(kc.getUrls().branch);
      ck.setCookbook(kc.getUrls().cookbookRelPath);
      ck.setGithub(kc.getUrls().orgRepo);
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

  @Override
  public void validate() throws ValidationException {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  public boolean getUseContainers() {
    return useContainers;
  }

  public void setUseContainers(boolean useContainers) {
    this.useContainers = useContainers;
  }

  public int getHosts() {
    return hosts;
  }

  public void setHosts(int hosts) {
    this.hosts = hosts;
  }
}
