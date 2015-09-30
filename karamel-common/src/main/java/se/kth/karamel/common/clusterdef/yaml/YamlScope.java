/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef.yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.common.clusterdef.Scope;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.clusterdef.json.JsonScope;
import se.kth.karamel.common.util.CollectionsUtil;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public abstract class YamlScope extends Scope {

  private Map<String, Object> attrs = new HashMap<>();

  public YamlScope() {
  }

  public YamlScope(JsonScope scope) throws MetadataParseException {
    super(scope);
    List<JsonCookbook> cookbooks = scope.getCookbooks();
    for (JsonCookbook cb : cookbooks) {
      Set<Map.Entry<String, Object>> entries = cb.getAttrs().entrySet();
      for (Map.Entry<String, Object> entry : entries) {
        foldOutAttr(entry.getKey(), entry.getValue(), attrs);
      }
    }
  }

  public void foldOutAttr(String key, Object value, Map<String, Object> map) throws MetadataParseException {
    String[] comps = key.split(Settings.ATTR_DELIMITER);
    Map<String, Object> parent = map;
    for (int i = 0; i < comps.length; i++) {
      String comp = comps[i];
      if (i == comps.length - 1) {
        if (parent.containsKey(comp) && !parent.get(comp).equals(value)) {
          throw new MetadataParseException(String.format("Ambiguous value for attribute '%s' 1st '%s' 2nd '%s' ", key,
              parent.get(comp), value));
        } else {
          parent.put(comp, value);
        }
      } else {
        Object next = parent.get(comp);
        if (next == null) {
          next = new HashMap<>();
          parent.put(comp, next);
        } else if (!(next instanceof Map)) {
          throw new MetadataParseException(String.format("Component '%s' in attributed has both simple value '%s' and "
              + "compound value '%s' ", comp, key, next, comps[i + 1]));
        }
        parent = (Map<String, Object>) next;
      }
    }
  }

  @Override
  public Object getAttr(String key) {
    String[] comps = key.split(Settings.ATTR_DELIMITER);
    Map<String, Object> parent = attrs;
    for (int i = 0; i < comps.length; i++) {
      String comp = comps[i];
      Object child = parent.get(comp);
      if (child == null) {
        return null;
      } else if (child instanceof Map) {
        parent = (Map<String, Object>) child;
      } else {
        if (i == comps.length - 1) {
          if (child instanceof List) {
            List<Object> list = (List<Object>) child;
            return CollectionsUtil.asStringList(list);
          } else {
            return child.toString();
          }
        } else {
          return null;
        }

      }
    }
    return null;
  }

  public Map<String, Object> getAttrs() {
    return attrs;
  }

  public void setAttrs(Map<String, Object> attrs) {
    this.attrs = attrs;
  }

  public Map<String, Object> flattenAttrs() throws ValidationException {
    return flattenAttrs(attrs, "");
  }

  public Map<String, Object> flattenAttrs(Map<String, Object> map, String partialName) throws ValidationException {
    Map<String, Object> flatten = new HashMap<>();
    if (map == null) {
      throw new ValidationException("attributes block cannot be empty");
    }
    Set<Map.Entry<String, Object>> entrySet = map.entrySet();

    for (Map.Entry<String, Object> entry : entrySet) {
      String key = ((partialName.isEmpty()) ? "" : partialName + "/") + entry.getKey();
      Object value = entry.getValue();
      if (value instanceof Map) {
        flatten.putAll(flattenAttrs((Map<String, Object>) value, key));
      } else {
        if (value == null) {
          throw new ValidationException(String.format("attribute '%s' doesn't have any value", key));
        } else if (value instanceof List) {
          List<Object> list = (List<Object>) value;
          flatten.put(key, CollectionsUtil.asStringList(list));
        } else {
          flatten.put(key, value.toString());
        }
      }
    }
    return flatten;
  }
}
