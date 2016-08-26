/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.util.StringUtils;

/**
 * Represents attributes/default.rb file in cookbook
 *
 * @author kamal
 */
public final class DefaultRb {

  private final List<String> contentLines;
  public static String LINE_PATTERN = "^\\s*default\\s*(\\[:.*\\])+\\s*=\\s*.*\\s*$";
  public static String LINE_PATTERN2 = "^\\s*default\\s*(\\..*)+\\s*=\\s*.*\\s*$";
  public static Pattern SIMPLE_VALUE_PATTERN = Pattern.compile("^((\".*\")|('.*')|(\\d*)|(\\[.*\\]))$");
  public static Pattern SKIP_VALUE_PATTERN = Pattern.compile("^.*\\[:.*\\].*$");
  public static Pattern SKIP_VALUE_PATTERN2 = Pattern.compile("^.*\\..*$");
  public static Pattern ARRAY_VALUE_PATTERN = Pattern.compile("\\s*\\[(.*)\\]s*(,)?\\s*");
  public static Pattern ATTR_DEFAULT_ARRAY_ITEMS = Pattern.compile("[\\'|\\\"]([^\\'|\\\"]*)[\\'|\\\"]");

  private final Map<String, Object> kv = new HashMap<>();

  public DefaultRb(String content) throws CookbookUrlException {
    this.contentLines = StringUtils.toLines(content);
    loadAttributes();
  }

  public void loadAttributes() {
    for (String line : contentLines) {
      if (line.matches(LINE_PATTERN) || line.matches(LINE_PATTERN2)) {
        int indx = line.indexOf("=");
        String key = line.substring(0, indx - 1).trim().substring(9).replaceAll("\\[\\:", "/").
            replaceAll("\\[\\'", "/").replaceAll("\\]", "").replaceAll("\\'", "").
            replaceAll("\\.", "/").trim();
        String value = line.substring(indx + 1).trim();
        Matcher m0 = ARRAY_VALUE_PATTERN.matcher(value);
        Matcher m1 = SIMPLE_VALUE_PATTERN.matcher(value);
        Matcher m2 = SKIP_VALUE_PATTERN.matcher(value);
        if (m0.matches() && !m2.matches()) {
          String sarr = m0.group(1);
          Matcher m921 = ATTR_DEFAULT_ARRAY_ITEMS.matcher(sarr);
          List<String> deflist = new ArrayList<>();
          while (m921.find()) {
            String item = m921.group(1);
            deflist.add(item);
          }
          kv.put(key, deflist);
        } else if (m1.matches() && !m2.matches()) {
          value = m1.group(1);
          if (value.matches("^(\".*\")|('.*')$")) {
            value = value.substring(1, value.length() - 1);
          }
          kv.put(key, value);
        }

      }
    }

  }

  public Object getValue(String key) {
    return kv.get(key);
  }

  public String getExperimentContextFormat() {
    StringBuilder sb = new StringBuilder();

    for (String key : kv.keySet()) {
      String k = key.substring(key.lastIndexOf("/") + 1);
      sb.append(k).append("=").append(kv.get(key)).append(System.lineSeparator());
    }

    return sb.toString();
  }

}
