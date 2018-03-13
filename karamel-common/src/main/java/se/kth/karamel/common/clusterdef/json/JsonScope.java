/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef.json;


import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.clusterdef.Scope;
import se.kth.karamel.common.cookbookmeta.Attribute;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlScope;
import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.cookbookmeta.CookbookCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author kamal
 */
public class JsonScope extends Scope {

  private final List<JsonCookbook> cookbooks = new ArrayList<>();
  public static CookbookCache CACHE;
  
  public JsonScope() {
  }

  public JsonScope(YamlCluster cluster, YamlScope scope) throws KaramelException {
    super(scope);
    Map<String, Object> usedAttrs = cluster.flattenAttrs();
    List<KaramelizedCookbook> allCookbooks = CACHE.loadAllKaramelizedCookbooks(cluster);
    //filtering invalid(not defined in metadata.rb) attributes from yaml model
    for (KaramelizedCookbook kcb : allCookbooks) {
      // Get all the valid attributes, also for transient dependency
      Set<Attribute> allValidAttrs = new HashSet<>(kcb.getMetadataRb().getAttributes());
      for (KaramelizedCookbook depKcb : kcb.getDependencies()) {
        allValidAttrs.addAll(depKcb.getMetadataRb().getAttributes());
      }

      // I think that this map should be <String, Attribute>. But I don't want to see
      // what happen if I change it.
      Map<String, Object> validUsedAttrs = new HashMap<>();
      for (String usedAttr: usedAttrs.keySet()) {
        if (allValidAttrs.contains(new Attribute(usedAttr))) {
          validUsedAttrs.put(usedAttr, usedAttrs.get(usedAttr));
        }
      }

      JsonCookbook jck = new JsonCookbook(kcb.getUrls().id, kcb.getMetadataRb().getName(),
          kcb.getMetadataRb().getName(), validUsedAttrs, kcb);
      cookbooks.add(jck);
    }
    
    Map<String, Object> invalidAttrs = new HashMap<>();
    invalidAttrs.putAll(usedAttrs);
    for (JsonCookbook jc : cookbooks) {
      Map<String, Object> attrs1 = jc.getAttrs();
      for (Map.Entry<String, Object> entry : attrs1.entrySet()) {
        String key = entry.getKey();
        if (invalidAttrs.containsKey(key)) {
          invalidAttrs.remove(key);
        }
      }
    }

    if (!invalidAttrs.isEmpty()) {
      throw new KaramelException(String.format("Invalid attributes, all used attributes must be defined in metadata.rb "
          + "files: %s", invalidAttrs.keySet().toString()));
    }

  }

  public List<JsonCookbook> getCookbooks() {
    return cookbooks;
  }

  public void setCookbooks(List<JsonCookbook> cookbooks) {
    this.cookbooks.addAll(cookbooks);
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
