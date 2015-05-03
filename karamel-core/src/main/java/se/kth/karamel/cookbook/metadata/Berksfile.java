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
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class Berksfile {

  private static final Logger logger = Logger.getLogger(Berksfile.class);
  private final List<String> fileLines;
  private final Map<String, String> deps = new HashMap<>();
  public static Pattern LINE_PATTERN = Pattern.compile("cookbook\\s*'(.*)'\\s*,\\s*github\\s*:\\s*'(.*)'");
  public static Set<String> validUrls = new HashSet<>();

  public Berksfile(List<String> fileLines) throws CookbookUrlException {
    this.fileLines = fileLines;
    loadDependencies();
    validateGithubUrls();
  }

  private void loadDependencies() {
    for (String line : fileLines) {
      Matcher matcher = LINE_PATTERN.matcher(line);
      if (matcher.matches()) {
        String cbName = matcher.group(1);
        String cbUrl = matcher.group(2);
        deps.put(cbName, cbUrl);
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
      CookbookUrls.Builder builder = new CookbookUrls.Builder();
      CookbookUrls urls = builder.url(address).build();
      String homeUrl = urls.home;
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

}
