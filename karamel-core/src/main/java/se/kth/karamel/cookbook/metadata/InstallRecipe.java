/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

/**
 *
 */
public class InstallRecipe {

  private final String setupCode;

  public InstallRecipe(String setupCode) {
    this.setupCode = setupCode;
  }

  public String getSetupCode() {
    return setupCode;
  }


}
