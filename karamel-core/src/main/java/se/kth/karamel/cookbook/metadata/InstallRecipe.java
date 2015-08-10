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
  private final String configCode;

  public InstallRecipe(String setupCode, String configCode) {
    this.setupCode = setupCode;
    this.configCode = configCode;
  }

  public String getSetupCode() {
    return setupCode;
  }

  public String getConfigCode() {
    return configCode;
  }
  

}
