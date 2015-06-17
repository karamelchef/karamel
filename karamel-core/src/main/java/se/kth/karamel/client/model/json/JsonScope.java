/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.client.api.CookbookCache;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.client.model.Cookbook;
import se.kth.karamel.client.model.Scope;
import se.kth.karamel.cookbook.metadata.Attribute;
import se.kth.karamel.cookbook.metadata.KaramelizedCookbook;
import se.kth.karamel.client.model.yaml.YamlCluster;
import se.kth.karamel.client.model.yaml.YamlScope;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class JsonScope extends Scope {

  private final List<JsonCookbook> cookbooks = new ArrayList<>();

  public JsonScope() {
  }

  public JsonScope(YamlCluster cluster, YamlScope scope) throws KaramelException {
    super(scope);
    Map<String, String> attrs = cluster.flattenAttrs();
    Set<Map.Entry<String, Cookbook>> cks = cluster.getCookbooks().entrySet();
    for (Map.Entry<String, Cookbook> entry : cks) {
      String key = entry.getKey();
      Cookbook cb = entry.getValue();

      KaramelizedCookbook metadata = CookbookCache.get(cb.getUrls().id);
      List<Attribute> allAttrs = metadata.getMetadataRb().getAttributes();
      Map<String, String> filteredAttrs = new HashMap<>();
      for (Attribute att : allAttrs) {
        if (attrs.containsKey(att.getName())) {
          filteredAttrs.put(att.getName(), attrs.get(att.getName()));
        }
      }
      JsonCookbook jck = new JsonCookbook(cb, key, filteredAttrs);
      cookbooks.add(jck);
    }

    Map<String, String> tempattrs = new HashMap<>();
    tempattrs.putAll(attrs);
    for (JsonCookbook jc : cookbooks) {
      Map<String, String> attrs1 = jc.getAttrs();
      for (Map.Entry<String, String> entry : attrs1.entrySet()) {
        String key = entry.getKey();
        if (tempattrs.containsKey(key)) {
          tempattrs.remove(key);
        }
      }
    }

    if (!tempattrs.isEmpty()) {
      throw new KaramelException(String.format("Undefined attributes: %s", attrs.keySet().toString()));
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
