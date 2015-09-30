/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kamal
 */
public class KaramelFileYamlRep {

  private List<KaramelFileYamlDeps> dependencies = new ArrayList<>();

  public List<KaramelFileYamlDeps> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<KaramelFileYamlDeps> dependencies) {
    if (dependencies != null) {
      this.dependencies = dependencies;
    }
  }

}
