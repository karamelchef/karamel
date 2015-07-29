/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 *
 * @author kamal
 */
public class IoUtils {

  public static List<String> readLines(String url) throws IOException {
    if (Settings.CB_CLASSPATH_MODE) {
      return readLinesFromClasspath(url);
    } else {
      return readLinesFromWeb(url);
    }
  }

  public static String readContent(String path) throws IOException {
    if (Settings.CB_CLASSPATH_MODE) {
      return readContentFromClasspath(path);
    } else {
      return readContentFromWeb(path);
    }
  }

  public static String readContentFromClasspath(String path) throws IOException {
    URL url = Resources.getResource(path);
    if (url == null) {
      throw new IOException("No config.props file found in cookbook");
    }
    return Resources.toString(url, Charsets.UTF_8);
  }

  public static List<String> readLinesFromClasspath(String url) throws IOException {
    return Resources.readLines(Resources.getResource(url), Charsets.UTF_8);
  }

  public static List<String> readLinesFromWeb(String url) throws IOException {
    URL fileUrl = new URL(url);
    return Resources.readLines(fileUrl, Charsets.UTF_8);
  }
  
  public static String readContentFromWeb(String url) throws IOException {
    URL fileUrl = new URL(url);
    return Resources.toString(fileUrl, Charsets.UTF_8);
  }
}
