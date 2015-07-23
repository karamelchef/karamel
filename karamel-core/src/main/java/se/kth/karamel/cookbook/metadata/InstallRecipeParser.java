package se.kth.karamel.cookbook.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.common.exception.RecipeParseException;

public class InstallRecipeParser {

  public static final String SETUP_MARKER="Pre-Experiment Code";
  public static final String CONFIG_MARKER="Configuration Files";
  public static Pattern EXPERIMENT_SETUP = Pattern.compile( SETUP_MARKER + "(.*)" + CONFIG_MARKER);
  public static Pattern CONFIG_FILES = Pattern.compile( CONFIG_MARKER + "(.*)");

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
          + "Missing one or both of these markers in the file: '" +  SETUP_MARKER + "' or '" + CONFIG_MARKER + "'");
    }
    String setupCode = recipeContent.substring(mp.start(), mp.end());

    Matcher mConfig = CONFIG_FILES.matcher(recipeContent);
    String configCode = recipeContent.substring(mConfig.start());

    return new InstallRecipe(setupCode, configCode);
  }

}
