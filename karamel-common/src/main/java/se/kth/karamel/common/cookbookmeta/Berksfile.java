/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

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
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.util.StringUtils;

/**
 *
 * @author kamal
 */
public class Berksfile {

  private static final Logger logger = Logger.getLogger(Berksfile.class);
  private final List<String> fileLines;
  private final Map<String, Cookbook> deps = new HashMap<>();
  public static Pattern LINE_PATTERN = Pattern.compile(
      "^cookbook\\s*(\\'[^,^'^\\\"]+\\'|\\\"[^,^'^\\\"]+\\\")\\s*,"
      + "\\s*github\\s*:\\s*(\\'[^,^'^\\\"]+\\'|\\\"[^,^'^\\\"]+\\\")\\s*"
      + "(,\\s*(branch|tag|version)\\s*:\\s*(\\'[^,^'^\\\"]+\\'|\\\"[^,^'^\\\"]+\\\")\\s*)?$");
  public static Set<String> validUrls = new HashSet<>();

  public Berksfile(String content) throws CookbookUrlException {
    this.fileLines = StringUtils.toLines(content);
    loadDependencies();
    validateGithubUrls();
  }

  public Map<String, Cookbook> getDeps() {
    return deps;
  }

  private void loadDependencies() {
    for (String line : fileLines) {
      String cbName = null;
      String cbUrl = null;
      String branch = null;
      Matcher matcher = LINE_PATTERN.matcher(line);
      if (matcher.matches()) {
        cbName = matcher.group(1);
        cbName = cbName.replaceAll("'|\"", "");
        cbUrl = matcher.group(2);
        cbUrl = cbUrl.replaceAll("'|\"", "");
        if (matcher.group(3) != null) {
          branch = matcher.group(5);
          branch = branch.replaceAll("'|\"", "");
        } else {
          branch = Settings.GITHUB_DEFAULT_BRANCH;
        }
        Cookbook cb = new Cookbook();
        cb.setGithub(cbUrl);
        cb.setBranch(branch);
        deps.put(cbName, cb);
      }

    }
  }

  private void validateGithubUrls() throws CookbookUrlException {
    if (Settings.CB_CLASSPATH_MODE) {
      logger.debug("Skip cookbook dependency check in the classpath mode");
      return;
    }

    for (Map.Entry<String, Cookbook> entry : deps.entrySet()) {
      String name = entry.getKey();
      Cookbook cb = entry.getValue();
      String homeUrl = cb.getUrls().cookbookUrl;
      String errorMsg = String.format("Cookbook-dependency '%s' doesn't refer to a valid url in Berksfile", name);
      try {
        if (validUrls.contains(homeUrl)) {
          logger.debug(String.format("Skip validating url '%s' since it was already validated", homeUrl));
        } else {
          logger.debug(String.format("Validating url '%s'", homeUrl));
          URL u = new URL(homeUrl);
          HttpURLConnection huc = (HttpURLConnection) u.openConnection();
          huc.setRequestMethod("GET");
          huc.connect();
          int code = huc.getResponseCode();
          if (code != 200) {
            throw new CookbookUrlException(errorMsg);
          }
          validUrls.add(homeUrl);
        }
      } catch (IOException ex) {
        throw new CookbookUrlException(errorMsg, ex);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    boolean skipLines = true;

    // append all lines that appear after 'metadata' in the Berksfile template
    for (String s : fileLines) {
      if (!skipLines) {
        sb.append(s).append(System.lineSeparator());
      }
      if (s.compareToIgnoreCase("metadata") == 0) {
        skipLines = false;
      }
    }
    return sb.toString();
  }

}
