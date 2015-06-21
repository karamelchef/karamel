/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.ExperimentContext;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 */
public class ExperimentRecipe {

  private static final Logger logger = Logger.getLogger(ExperimentRecipe.class);
  private final List<String> fileLines;
  private final String recipeName;
//  public static Pattern LINE_PATTERN_BASIC = Pattern.compile(
//      "cookbook\\s*'([^,^'^\"]*)'\\s*,\\s*github\\s*:\\s*'([^,^'^\"]*)'");

  public ExperimentRecipe(String recipeName, List<String> fileLines) throws CookbookUrlException {
    this.fileLines = fileLines;
    this.recipeName = recipeName;
    loadDependencies();
  }

  public String getRecipeName() {
    return recipeName;
  }
  
  public String getPreChefScript() {
    StringBuilder contents = new StringBuilder();
    return contents.toString();
  }
  
  public ExperimentContext.ScriptType getScriptType() {
    ExperimentContext.ScriptType st = ExperimentContext.ScriptType.bash;
    
    return st;
  }
  
  public String getScript() {
    StringBuilder contents = new StringBuilder();
    return contents.toString();
  }
  
  private void loadDependencies() {
    for (String line : fileLines) {
//      boolean found = false;
//      String cbName = null;
//      String cbUrl = null;
//      String branch = null;
//      Matcher matcher = LINE_PATTERN_WITH_TAG.matcher(line);
//      if (!found && matcher.matches()) {
//        found = true;
//        cbName = matcher.group(1);
//        cbUrl = matcher.group(2);
//        branch = matcher.group(3);
//      }
//      if (found) {
//        deps.put(cbName, cbUrl);
//        branches.put(cbName, branch);
//      }
    }
  }

}
