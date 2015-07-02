/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import se.kth.karamel.backend.ExperimentContext;

/**
 *
 */
public class ExperimentRecipe {

  private final String recipeName;
  private final String preScriptContents;
  private final String scriptContents;
  private final ExperimentContext.ScriptType scriptType;

  public ExperimentRecipe(String recipeName, ExperimentContext.ScriptType scriptType, String preScriptContents,
      String scriptContents) {
    this.recipeName = recipeName;
    this.scriptContents = scriptContents;
    this.preScriptContents = preScriptContents;
    this.scriptType = scriptType;
  }

  public String getPreScriptContents() {
    return preScriptContents;
  }

  public String getScriptContents() {
    return scriptContents;
  }

  public String getRecipeName() {
    return recipeName;
  }

  public ExperimentContext.ScriptType getScriptType() {
    return scriptType;
  }

}
