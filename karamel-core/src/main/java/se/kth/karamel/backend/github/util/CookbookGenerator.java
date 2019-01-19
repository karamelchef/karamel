package se.kth.karamel.backend.github.util;

import java.io.IOException;

public class CookbookGenerator {

  public static StringBuilder instantiateFromTemplate(String filePath, String... pairs) throws IOException {
    StringBuilder sb = new StringBuilder();
    String script = IoUtils.readContentFromClasspath(filePath);
    if (pairs.length > 0) {
      for (int i = 0; i < pairs.length; i += 2) {
        String key = pairs[i];
        String val = pairs[i + 1];
        script = script.replaceAll("%%" + key + "%%", val);
      }
    }
    return sb.append(script);
  }

  public static StringBuilder metadataAttribute(StringBuilder sb, String cbName, String desc, String type) {
    return metadataAttribute(sb, cbName, desc, type, null);
  }

  public static StringBuilder metadataAttribute(StringBuilder sb, String cbName, String desc, String type,
      String defaultValue) {
    desc = (desc == null) ? "" : desc;
    sb.append(cbName).append(System.lineSeparator());
    sb.append(desc).append(System.lineSeparator());
    sb.append(type).append(System.lineSeparator());
    if (defaultValue != null) {
      sb.append(defaultValue).append(System.lineSeparator());
    }
    return sb;
  }

  public static StringBuilder defaultAttribute(StringBuilder sb, String cbName, String attr, String type,
      String value) {

    sb.append("default[:").append(cbName).append("][:").append(attr).append("] = ").append(value);
    return sb;
  }
}
