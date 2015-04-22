/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class Berksfile {

  private static final Logger logger = Logger.getLogger(Berksfile.class);
  private final String url;
  private final Map<String, String> deps = new HashMap<>();
  public static Pattern LINE_PATTERN = Pattern.compile("cookbook\\s*'(.*)'\\s*,\\s*github\\s*:\\s*'(.*)'");
  public static Set<String> validUrls = new HashSet<>();

  public Berksfile(String rawUrl) throws CookbookUrlException {
    this.url = rawUrl;
    loadDependencies();
    validateGithubUrls();
  }

  private void loadDependencies() throws CookbookUrlException {
    URL fileUrl;
    try {
      fileUrl = new URL(url);
      List<String> lines = Resources.readLines(fileUrl, Charset.forName("UTF-8"));

      for (String line : lines) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        if (matcher.matches()) {
          String cbName = matcher.group(1);
          String cbUrl = matcher.group(2);
          deps.put(cbName, cbUrl);
        }
      }
    } catch (IOException ex) {
      throw new CookbookUrlException("Could not find Berksfile at " + url, ex);
    }
  }

  private void validateGithubUrls() throws CookbookUrlException {
    for (Map.Entry<String, String> entry : deps.entrySet()) {
      String name = entry.getKey();
      String address = entry.getValue();
      GithubUrls.Builder builder = new GithubUrls.Builder();
      GithubUrls urls = builder.url(address).build();
      String homeUrl = urls.home;
      String errorMsg = String.format("Cookbook-dependency '%s' doesn't refer to a valid url in Berksfile '%s'", name, url);
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
