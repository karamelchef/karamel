/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.client.model.Scope;
import se.kth.karamel.common.Settings;
import se.kth.karamel.client.model.json.JsonCookbook;
import se.kth.karamel.client.model.json.JsonScope;
import se.kth.karamel.common.exception.MetadataParseException;

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
            Set<Map.Entry<String, String>> entries = cb.getAttrs().entrySet();
            System.out.println("size:" + entries.size());
            for (Map.Entry<String, String> entry : entries) {
                System.out.println("------------");
                foldOutAttr(entry.getKey(), entry.getValue(), attrs);
            }
        }
    }

    protected void foldOutAttr(String key, String value, Map<String, Object> map) throws MetadataParseException {
        System.out.println("<<<<<<<<   " + key + " " + value);
        String[] comps = key.split(Settings.ATTR_DELIMITER);
        Map<String, Object> parent = map;
        for (int i = 0; i < comps.length; i++) {
            String comp = comps[i];
            if (i == comps.length - 1) {
                if (parent.containsKey(comp) && !parent.get(comp).equals(value)) {
                    throw new MetadataParseException(String.format("Ambiguous value for attribute '%s' 1st '%s' 2nd '%s' ", key, parent.get(comp), value));
                } else {
                    parent.put(comp, value);
                }
            } else {
                Object next = parent.get(comp);
                if (next == null) {
                    next = new HashMap<>();
                    parent.put(comp, next);
                } else if (!(next instanceof Map)) {
                    throw new MetadataParseException(String.format("Component '%s' in attributed has both simple value '%s' and compound value '%s' ", comp, key, next, comps[i + 1]));
                }
                parent = (Map<String, Object>) next;
            }
        }
    }

    @Override
    public String getAttr(String key) {
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
                    return child.toString();
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

    public Map<String, String> flattenAttrs() {
        return flattenAttrs(attrs, "");
    }

    public Map<String, String> flattenAttrs(Map<String, Object> map, String partialName) {
        Map<String, String> flatten = new HashMap<>();
        Set<Map.Entry<String, Object>> entrySet = map.entrySet();

        for (Map.Entry<String, Object> entry : entrySet) {
            String key = ((partialName.isEmpty()) ? "" : partialName + "/") + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatten.putAll(flattenAttrs((Map<String, Object>) value, key));
            } else {
                flatten.put(key, value.toString());
            }
        }
        return flatten;
    }
}
