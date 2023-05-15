package se.kth.karamel.common.clusterdef.yaml;

import java.util.HashMap;
import java.util.Map;

public class RuntimeConfiguration {
  private Map<String, Integer> recipesParallelism = new HashMap<>();

  public Map<String, Integer> getRecipesParallelism() {
    return recipesParallelism;
  }

  public void setRecipesParallelism(Map<String, Integer> recipeParallelism) {
    this.recipesParallelism = recipeParallelism;
  }
}
