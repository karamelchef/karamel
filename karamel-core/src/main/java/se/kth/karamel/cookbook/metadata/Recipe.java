/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author kamal
 */
public class Recipe {

  public static Pattern LINK = Pattern.compile("#link:(.*)");
  String name;
  String description;
  Set<String> links = new HashSet<>();

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Set<String> getLinks() {
    return links;
  }

  public void addLink(String link) {
    this.links.add(link);
  }

  public void parseComments(List<String> comments) {
    for (String line : comments) {
      Matcher m = LINK.matcher(line);
      if (m.matches()) {
        addLink(m.group(1));
      }
    }
  }

}
