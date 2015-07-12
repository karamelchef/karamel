package se.kth.karamel.cookbook.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.common.exception.RecipeParseException;

public class InstallRecipeParser {

  public static Pattern EXPERIMENT_SETUP = Pattern.compile("Pre-Experiment Code(.*)");

  /**
   *
   * @param recipeContent
   * @return an experiment recipe
   * @throws se.kth.karamel.common.exception.RecipeParseException
   */
  public static InstallRecipe parse(String recipeContent) throws RecipeParseException {

    Matcher mp = EXPERIMENT_SETUP.matcher(recipeContent);
    boolean isSetupCode = mp.find();
    if (!isSetupCode) {
      throw new RecipeParseException("Could not find in the install recipe any experiment setup chef code. "
          + "Re-enter the line directly before the setup code \"Pre-Experiment Code\" ");
    }
    String postScript = recipeContent.substring(mp.start());

    return new InstallRecipe(postScript);
  }

}
