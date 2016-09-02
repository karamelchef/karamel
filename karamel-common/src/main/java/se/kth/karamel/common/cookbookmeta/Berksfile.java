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
  private final Map<String, String> deps = new HashMap<>();
  private final Map<String, String> branches = new HashMap<>();
  public static Pattern LINE_PATTERN_WITH_TAG = Pattern.compile(
      "cookbook\\s*'([^,^'^\"]*)'\\s*,\\s*github\\s*:\\s*'([^,^'^\"]*)',\\s*tag\\s*:\\s*'([^,^'^\"]*)'");
  public static Pattern LINE_PATTERN_WITH_VERSION = Pattern.compile(
      "cookbook\\s*'([^,^'^\"]*)'\\s*,\\s*github\\s*:\\s*'([^,^'^\"]*)',\\s*version\\s*:\\s*'([^,^'^\"]*)'");
  public static Pattern LINE_PATTERN_WITH_BRANCH = Pattern.compile(
      "cookbook\\s*'([^,^'^\"]*)'\\s*,\\s*github\\s*:\\s*'([^,^'^\"]*)',\\s*branch\\s*:\\s*'([^,^'^\"]*)'");
  public static Pattern LINE_PATTERN_BASIC = Pattern.compile(
      "cookbook\\s*'([^,^'^\"]*)'\\s*,\\s*github\\s*:\\s*'([^,^'^\"]*)'");
  public static Set<String> validUrls = new HashSet<>();

  public Berksfile(String content) throws CookbookUrlException {
    this.fileLines = StringUtils.toLines(content);
    loadDependencies();
    validateGithubUrls();
  }

  public Map<String, String> getDeps() {
    return deps;
  }

  public Map<String, String> getBranches() {
    return branches;
  }

  private void loadDependencies() {
    for (String line : fileLines) {
      boolean found = false;
      String cbName = null;
      String cbUrl = null;
      String branch = null;
      Matcher matcher = LINE_PATTERN_WITH_TAG.matcher(line);
      if (!found && matcher.matches()) {
        found = true;
        cbName = matcher.group(1);
        cbUrl = matcher.group(2);
        branch = matcher.group(3);
      }
      matcher = LINE_PATTERN_WITH_BRANCH.matcher(line);
      if (matcher.matches()) {
        found = true;
        cbName = matcher.group(1);
        cbUrl = matcher.group(2);
        branch = matcher.group(3);
      }
      matcher = LINE_PATTERN_WITH_VERSION.matcher(line);
      if (matcher.matches()) {
        found = true;
        cbName = matcher.group(1);
        cbUrl = matcher.group(2);
        branch = matcher.group(3);
      }
      matcher = LINE_PATTERN_BASIC.matcher(line);
      if (matcher.matches()) {
        found = true;
        cbName = matcher.group(1);
        cbUrl = matcher.group(2);
        branch = Settings.GITHUB_DEFAULT_BRANCH;
      }

      if (found) {
        deps.put(cbName, cbUrl);
        branches.put(cbName, branch);
      }
    }
  }

  private void validateGithubUrls() throws CookbookUrlException {
    if (Settings.CB_CLASSPATH_MODE) {
      logger.info("Skip cookbook dependency check in the classpath mode");
      return;
    }

    for (Map.Entry<String, String> entry : deps.entrySet()) {
      String name = entry.getKey();
      String address = entry.getValue();
      String branch = branches.get(name);
      CookbookUrls.Builder builder = new CookbookUrls.Builder();
      CookbookUrls urls = builder.url(address).branchOrVersion(branch).build();
      String homeUrl = urls.cookbookUrl;
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
