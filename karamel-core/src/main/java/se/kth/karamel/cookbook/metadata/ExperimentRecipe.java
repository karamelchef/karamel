/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import se.kth.karamel.backend.Experiment;

/**
 *
 */
public class ExperimentRecipe {

  private final String recipeName;
  private final String scriptContents;
  private final Experiment.ScriptType scriptType;
  private final String configFileName;
  private final String configFileContents;

  public ExperimentRecipe(String recipeName, Experiment.ScriptType scriptType, 
      String scriptContents, String configFileName, String configFileContents) {
    this.recipeName = recipeName;
    this.scriptContents = scriptContents;
    this.scriptType = scriptType;
    this.configFileName = configFileName;
    this.configFileContents = configFileContents;
  }

  public String getScriptContents() {
    return scriptContents;
  }

  public String getRecipeName() {
    return recipeName;
  }

  public Experiment.ScriptType getScriptType() {
    return scriptType;
  }

  public String getConfigFileContents() {
    return configFileContents;
  }

  public String getConfigFileName() {
    return configFileName;
  }

}
