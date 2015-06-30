package se.kth.karamel.cookbook.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.backend.ExperimentContext.ScriptType;
import se.kth.karamel.common.exception.RecipeParseException;

public class ExperimentRecipeParser {

  public static Pattern PRE_SCRIPT = Pattern.compile(".*script 'run_experiment'.*");
  public static Pattern EXPERIMENT_SCRIPT = Pattern.compile("script 'run_experiment' do.*EOM.*end");

  /**
   *
   * @param content
   * @return an experiment recipe
   * @throws se.kth.karamel.common.exception.RecipeParseException
   */
  public static ExperimentRecipe parse(String content) throws RecipeParseException {
//    String[] lines = content.split("\\r?\\n");

    Matcher m = EXPERIMENT_SCRIPT.matcher(content);
    boolean foundScript = m.find();
    if (!foundScript) {
      throw new RecipeParseException(
          "Could not find in the recipe a script resource like \"script 'run_experiment' do\" ");
    }
    String script = m.group(1);

    Matcher mp = PRE_SCRIPT.matcher(content);
    boolean foundPreScript = mp.find();
    if (!foundPreScript) {
      throw new RecipeParseException(
          "Could not find in the recipe any chef code before a script resource like \"script 'run_experiment' do\" ");
    }
    String preScript = mp.group(1);

    return new ExperimentRecipe("experiment", ScriptType.bash, script, preScript);
  }

}
