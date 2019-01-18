/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.scanner.ScannerException;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.Nova;
import se.kth.karamel.common.clusterdef.Occi;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlGroup;
import se.kth.karamel.common.clusterdef.yaml.YamlPropertyRepresenter;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.FilesystemUtil;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.clusterdef.ClusterDefinitionValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import se.kth.karamel.client.api.CookbookCacheIml;
import se.kth.karamel.common.clusterdef.json.JsonScope;
import se.kth.karamel.common.cookbookmeta.CookbookCache;

/**
 * Stores/reads cluster definitions from Karamel home folder, does conversions between yaml and json definitions.
 *
 * @author kamal
 */
public class ClusterDefinitionService {

  public static final CookbookCache CACHE = new CookbookCacheIml();

  static {
    JsonScope.CACHE = CACHE;
    YamlCluster.CACHE = CACHE;
  }

  public static String jsonToYaml(JsonCluster jsonCluster) throws KaramelException {
    YamlCluster yamlCluster = new YamlCluster(jsonCluster);
    DumperOptions options = new DumperOptions();
    options.setIndent(2);
    options.setWidth(120);
    options.setExplicitEnd(false);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setPrettyFlow(true);
    options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    YamlPropertyRepresenter yamlPropertyRepresenter = new YamlPropertyRepresenter();
    yamlPropertyRepresenter.addClassTag(YamlCluster.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Ec2.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Baremetal.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Gce.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Nova.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Occi.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(Cookbook.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(YamlGroup.class, Tag.MAP);
    yamlPropertyRepresenter.addClassTag(HashSet.class, Tag.MAP);
    Yaml yaml = new Yaml(yamlPropertyRepresenter, options);
    return yaml.dump(yamlCluster);
  }

  public static void saveYaml(String yaml) throws KaramelException {
    try {
      YamlCluster cluster = yamlToYamlObject(yaml);
      String name = cluster.getName().toLowerCase();
      File folder = new File(Settings.CLUSTER_ROOT_PATH(name));
      if (!folder.exists()) {
        folder.mkdirs();
      }
      File file = new File(Settings.CLUSTER_YAML_PATH(name));
      Files.write(yaml, file, Charset.forName("UTF-8"));
    } catch (IOException ex) {
      throw new KaramelException("Could not convert yaml to java ", ex);
    }
  }

  public static String loadYaml(String clusterName) throws KaramelException {
    try {
      String name = clusterName.toLowerCase();
      File folder = new File(Settings.CLUSTER_ROOT_PATH(name));
      if (!folder.exists()) {
        throw new KaramelException(String.format("cluster '%s' is not available", name));
      }
      String yamlPath = Settings.CLUSTER_YAML_PATH(name);
      File file = new File(yamlPath);
      if (!file.exists()) {
        throw new KaramelException(String.format("yaml '%s' is not available", yamlPath));
      }
      return Files.toString(file, Charsets.UTF_8);
    } catch (IOException ex) {
      throw new KaramelException("Could not save the yaml ", ex);
    }
  }

  public static void removeDefinition(String clusterName) throws KaramelException {
    String name = clusterName.toLowerCase();
    try {
      FilesystemUtil.deleteRecursive(Settings.CLUSTER_ROOT_PATH(name));
    } catch (FileNotFoundException ex) {
      throw new KaramelException(ex);
    }
  }

  public static List<String> listClusters() {
    List<String> clusters = new ArrayList<>();
    File folder = new File(Settings.KARAMEL_ROOT_PATH);
    if (folder.exists()) {
      File[] files = folder.listFiles();
      for (File file : files) {
        if (file.isDirectory()) {
          File[] files2 = file.listFiles();
          for (File file2 : files2) {
            if (file2.isFile() && file2.getName().equals(Settings.YAML_FILE_NAME)) {
              clusters.add(file.getName());
            }
          }
        }
      }
    }
    return clusters;
  }

  public static String jsonToYaml(String json) throws KaramelException {
    Gson gson = new Gson();
    JsonCluster jsonCluster = gson.fromJson(json, JsonCluster.class);
    return jsonToYaml(jsonCluster);
  }

  public static JsonCluster jsonToJsonObject(String json) {
    Gson gson = new Gson();
    return gson.fromJson(json, JsonCluster.class);
  }

  public static JsonCluster yamlToJsonObject(String yaml) throws KaramelException {
    YamlCluster cluster = yamlToYamlObject(yaml);
    JsonCluster jsonCluster = new JsonCluster(cluster);
    ClusterDefinitionValidator.validate(jsonCluster);
    return jsonCluster;
  }

  public static YamlCluster yamlToYamlObject(String ymlString) throws KaramelException {
    try {
      Yaml yaml = new Yaml(new Constructor(YamlCluster.class));
      Object document = yaml.load(ymlString);
      return ((YamlCluster) document);
    } catch (ScannerException ex) {
      throw new KaramelException("Syntax error in the yaml!!", ex);
    }
  }

  public static String yamlToJson(String yaml) throws KaramelException {
    JsonCluster jsonObj = yamlToJsonObject(yaml);
    return serializeJson(jsonObj);
  }

  public static String serializeJson(JsonCluster jsonCluster) {
    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();
    Gson gson = builder.setPrettyPrinting().create();
    return gson.toJson(jsonCluster);
  }
}
