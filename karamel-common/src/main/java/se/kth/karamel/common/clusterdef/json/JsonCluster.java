/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef.json;

import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class JsonCluster extends JsonScope {

  private String name;

  private boolean useContainers;

  private int hosts;

  private List<JsonGroup> groups = new ArrayList<>();

  public JsonCluster() {
  }

  public JsonCluster(YamlCluster cluster) throws KaramelException {
    super(cluster, cluster);
    this.name = cluster.getName();
    this.useContainers = cluster.getUseContainers();
    this.hosts = cluster.getHosts();
    Set<Map.Entry<String, YamlGroup>> entrySet = cluster.getGroups().entrySet();
    for (Map.Entry<String, YamlGroup> entry : entrySet) {
      String gName = entry.getKey();
      YamlGroup group = entry.getValue();
      JsonGroup jsonGroup = new JsonGroup(cluster, group, gName);
      this.groups.add(jsonGroup);
    }

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<JsonGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<JsonGroup> groups) {
    this.groups = groups;
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
