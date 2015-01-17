/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 * Represents attributes/default.rb file in cookbook
 *
 * @author kamal
 */
public final class DefaultRb {

  private final String url;
  public static String LINE_PATTERN = "^\\s*default\\s*(\\[:.*\\])+\\s*=\\s*.*\\s*$";
  public static Pattern VALUE_PATTERN = Pattern.compile("^((\".*\")|('.*')|(\\d*)|(\\[.*\\]))$");
  public static Pattern SKIP_VALUE_PATTERN = Pattern.compile("^.*\\[:.*\\].*$");

  private final Map<String, String> kv = new HashMap<>();

  public DefaultRb(String rawFileUrl) throws CookbookUrlException {
    this.url = rawFileUrl;
    loadAttributes();
  }

  public void loadAttributes() throws CookbookUrlException {
    URL fileUrl;
    try {
      fileUrl = new URL(url);
      List<String> lines = Resources.readLines(fileUrl, Charset.forName("UTF-8"));

      for (String line : lines) {
        if (line.matches(LINE_PATTERN)) {
          int indx = line.indexOf("=");
          String key = line.substring(0, indx - 1).trim().substring(9).replaceAll("\\[\\:", "/").replaceAll("\\[\\'", "/").replaceAll("\\]", "").replaceAll("\\'", "").trim();
          String value = line.substring(indx + 1).trim();
          Matcher m1 = VALUE_PATTERN.matcher(value);
          Matcher m2 = SKIP_VALUE_PATTERN.matcher(value);
          if (m1.matches() && !m2.matches()) {
            value = m1.group(1);
            if (value.matches("^(\".*\")|('.*')$")) {
              value = value.substring(1, value.length() - 1);
            }
            kv.put(key, value);
          }

        }
      }
    } catch (MalformedURLException ex) {
      throw new CookbookUrlException("Atribute url is malford " + url, ex);
    } catch (IOException ex) {
      throw new CookbookUrlException("Cannot parse the attribute file " + url, ex);
    }

  }

  public String getValue(String key) {
    return kv.get(key);
  }

}
