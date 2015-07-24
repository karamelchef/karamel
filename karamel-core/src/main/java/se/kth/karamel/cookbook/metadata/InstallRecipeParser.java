package se.kth.karamel.cookbook.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.common.exception.RecipeParseException;

public class InstallRecipeParser {

  public static final String SETUP_MARKER = "Pre-Experiment Code";
  public static final String CONFIG_MARKER = "# Configuration Files";
  public static Pattern EXPERIMENT_SETUP_START = Pattern.compile(SETUP_MARKER + "(.*)");
  public static Pattern EXPERIMENT_SETUP_END = Pattern.compile("(.*)" + CONFIG_MARKER);
  public static Pattern CONFIG_FILES = Pattern.compile(CONFIG_MARKER + "(.*)");

  /**
   *
   * @param recipeContent
   * @return an experiment recipe
   * @throws se.kth.karamel.common.exception.RecipeParseException
   */
  public static InstallRecipe parse(String recipeContent) throws RecipeParseException {

    Matcher mStart = EXPERIMENT_SETUP_START.matcher(recipeContent);
    boolean foundMarker = mStart.find();
    if (!foundMarker) {
      throw new RecipeParseException("Could not find in the install recipe any experiment setup chef code. "
          + "Missing this marker in the file: '" + SETUP_MARKER);
    }
    Matcher mEnd = EXPERIMENT_SETUP_END.matcher(recipeContent);
    foundMarker = mEnd.find();
    if (!foundMarker) {
      throw new RecipeParseException("Could not find in the install recipe any experiment setup chef code. "
          + "Missing this marker in the file: '" + CONFIG_MARKER);
    }
    // skip over the SETUP_MARKER and +1 for the newline
    String setupCode = recipeContent.substring(mStart.start() + SETUP_MARKER.length() + 1,
        mEnd.end() - CONFIG_MARKER.length());

    Matcher mConfig = CONFIG_FILES.matcher(recipeContent);
    foundMarker = mConfig.find();
    if (!foundMarker) {
      throw new RecipeParseException("Could not find in the install recipe any config file code. "
          + "Missing this marker in the file: '" + CONFIG_MARKER);
    }
    // skip over the CONFIG_MARKER and +1 for the newline
    String configCode = recipeContent.substring(mConfig.start() + CONFIG_MARKER.length() + 1);

    return new InstallRecipe(setupCode, configCode);
  }

}
