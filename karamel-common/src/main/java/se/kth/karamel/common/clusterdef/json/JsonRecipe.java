package se.kth.karamel.common.clusterdef.json;

import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.util.Settings;

public class JsonRecipe implements Comparable<JsonRecipe>{

  private KaramelizedCookbook cookbook;
  private String name;

  public JsonRecipe(KaramelizedCookbook cookbook, String name) {
    this.cookbook = cookbook;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public KaramelizedCookbook getCookbook() {
    return cookbook;
  }

  public void setCookbook(KaramelizedCookbook cookbook) {
    this.cookbook = cookbook;
  }

  public String getCanonicalName() {
    return cookbook.getCookbookName() + Settings.COOKBOOK_DELIMITER + name;
  }

  @Override
  public int compareTo(JsonRecipe o) {
    return name.compareTo(o.name);
  }

}
