package se.kth.karamel.cookbook.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.backend.Experiment.ScriptType;
import se.kth.karamel.common.exception.RecipeParseException;

public class ExperimentRecipeParser {

  public static Pattern EXPERIMENT_SCRIPT = Pattern.compile("script \'run_experiment\'");
  public static Pattern EXPERIMENT_DELIMITER = Pattern.compile("EOM");
  public static Pattern EXPERIMENT_INTERPRETER = Pattern.compile("interpreter \"(.*)\"");

  /**
   *
   * @param name
   * @param recipeContent
   * @param configFileName
   * @param configFileContents
   * @return an experiment recipe
   * @throws se.kth.karamel.common.exception.RecipeParseException
   */
  public static ExperimentRecipe parse(String name, String recipeContent, String configFileName,
      String configFileContents) throws RecipeParseException {

    Matcher mp = EXPERIMENT_SCRIPT.matcher(recipeContent);
    boolean foundPreScript = mp.find();
    if (!foundPreScript) {
      throw new RecipeParseException(
          "Could not find in the recipe any chef code before a script resource like \"script 'run_experiment' do\" ");
    }
    String preScript = recipeContent.substring(0, mp.start());
    String postScript = recipeContent.substring(mp.start());
    
    Matcher ms = EXPERIMENT_DELIMITER.matcher(postScript);
    boolean foundStart = ms.find();
    if (!foundStart) {
      throw new RecipeParseException(
          "Could not find in the recipe a script resource like \"script 'run_experiment' do\" ");
    }
    int startPos = ms.end();
    
    boolean foundEnd = ms.find();
    if (!foundEnd) {
      throw new RecipeParseException(
          "Could not find in the recipe a script resource like \"script 'run_experiment' do\" ");
    }
    int endPos = ms.start();
    
    String script = postScript.substring(startPos, endPos);

    Matcher mi = EXPERIMENT_INTERPRETER.matcher(postScript);
    boolean foundInterpreter = mi.find();
    if (!foundInterpreter) {
      throw new RecipeParseException(
          "Could not find in the Interpreter in script experiment recipe.");
    }
    String interpreter = mi.group(1);
    
    
    return new ExperimentRecipe(name, ScriptType.valueOf(interpreter), script, configFileName, configFileContents);
  }

}
