/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author kamal
 */
public class IoUtils {

  static class Worker extends Thread {

    ConcurrentHashMap<String, String> map;
    String url;

    public Worker(ConcurrentHashMap<String, String> map, String url) {
      this.map = map;
      this.url = url;
    }

    @Override
    public void run() {
      try {
        String content = readContent(url);
        map.put(url, content);
      } catch (IOException ex) {
      }
    }
  }

  public static List<String> readLines(String url) throws IOException {
    if (Settings.CB_CLASSPATH_MODE) {
      return readLinesFromClasspath(url);
    } else if (Settings.USE_CLONED_REPO_FILES) {
      return readLinesFromPath(url);
    } else {
      return readLinesFromWeb(url);
    }
  }

  public static String readContent(String path) throws IOException {
    if (Settings.CB_CLASSPATH_MODE) {
      return readContentFromClasspath(path);
    } else if (Settings.USE_CLONED_REPO_FILES) {
      return readContentFromPath(path);
    } else {
      return readContentFromWeb(path);
    }
  }

  public static Map<String, String> readContentParallel(Set<String> paths, ExecutorService tp) {
    ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    Set<Worker> workers = new HashSet<>();
    for (String path : paths) {
      Worker worker = new Worker(map, path);
      workers.add(worker);
      tp.execute(worker);
    }
    for (Worker worker : workers) {
      try {
        worker.join();
      } catch (InterruptedException ex) {
      }
    }
    return map;
  }

  public static String readContentFromClasspath(String path) throws IOException {
    URL url = Resources.getResource(path);
    if (url == null) {
      throw new IOException("No config.props file found in cookbook");
    }
    return Resources.toString(url, Charsets.UTF_8);
  }

  public static String readContentFromPath(String path) throws IOException {
    return Files.toString(new File(path), Charsets.UTF_8);
  }

  public static List<String> readLinesFromClasspath(String url) throws IOException {
    return Resources.readLines(Resources.getResource(url), Charsets.UTF_8);
  }

  public static List<String> readLinesFromPath(String url) throws IOException {
    return Files.readLines(new File(url), Charsets.UTF_8);
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
