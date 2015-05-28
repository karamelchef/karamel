/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 *
 * @author kamal
 */
public class YamlPropertyRepresenter extends Representer {

  List<String> CLUSTER_ORDER = new ArrayList<>(Arrays.asList("name", "ec2", "vagrant", "baremetal", "cookbooks", "attrs", "groups"));
  List<String> GROUP_ORDER = new ArrayList<>(Arrays.asList("size", "ec2", "vagrant", "baremetal", "attrs", "recipes"));

  @Override
  protected Set<Property> getProperties(Class<? extends Object> type)
          throws IntrospectionException {
    List<String> order = null;
    if (type.isAssignableFrom(YamlCluster.class)) {
      order = CLUSTER_ORDER;
    } else if (type.isAssignableFrom(YamlGroup.class)) {
      order = GROUP_ORDER;
    }
    if (order != null) {
      Set<Property> standard = super.getProperties(type);
      Set<Property> sorted = new TreeSet<>(new PropertyComparator(order));
      sorted.addAll(standard);
      return sorted;
    } else {
      return super.getProperties(type);
    }
  }

  private class PropertyComparator implements Comparator<Property> {

    List<String> order;

    public PropertyComparator(List<String> order) {
      this.order = order;
    }

    public int compare(Property o1, Property o2) {
      // important go first

      for (String name : order) {
        int c = compareByName(o1, o2, name);
        if (c != 0) {
          return c;
        }
      }
      // all the rest
      return o1.compareTo(o2);
    }

    private int compareByName(Property o1, Property o2, String name) {
      if (o1.getName().equals(name)) {
        return -1;
      } else if (o2.getName().equals(name)) {
        return 1;
      }
      return 0;// compare further
    }
  }

  @Override
  protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
          Object propertyValue, Tag customTag) {
    if (propertyValue == null) {
      return null;
    } else {
      return super
              .representJavaBeanProperty(javaBean, property, propertyValue, customTag);
    }
  }
}
