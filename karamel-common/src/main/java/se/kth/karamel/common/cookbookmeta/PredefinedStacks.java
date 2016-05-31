/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kamal
 */
public enum PredefinedStacks {
  TABLESPOON_SERVER("tablespoon", "karamel-lab/riemann", Lists.newArrayList("riemann::server"));
    
  private String stackName;
  private String githubUrl;
  private List<String> recipes = new ArrayList<>();

  private PredefinedStacks(String stackName, String githubUrl, List<String> recipes) {
    this.stackName = stackName;
    this.githubUrl = githubUrl;
    this.recipes = recipes;
  }

  public String getStackName() {
    return stackName;
  }

  public String getGithubUrl() {
    return githubUrl;
  }

  public List<String> getRecipes() {
    return recipes;
  }
}
