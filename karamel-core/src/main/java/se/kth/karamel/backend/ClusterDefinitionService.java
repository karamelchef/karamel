/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
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
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlGroup;
import se.kth.karamel.common.clusterdef.yaml.YamlPropertyRepresenter;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.FilesystemUtil;
import se.kth.karamel.common.util.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.client.api.CookbookCache;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.clusterdef.json.JsonRecipe;
import se.kth.karamel.common.cookbookmeta.CookbookUrls;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.cookbookmeta.Recipe;
import se.kth.karamel.common.exception.InconsistentDeploymentException;
import se.kth.karamel.common.exception.ValidationException;

/**
 * Stores/reads cluster definitions from Karamel home folder, does conversions between yaml and json definitions.
 *
 * @author kamal
 */
public class ClusterDefinitionService {

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
    String content = yaml.dump(yamlCluster);
    return content;
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
      String yaml = Files.toString(file, Charsets.UTF_8);
      return yaml;
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

  public static List<String> listClusters() throws KaramelException {
    List<String> clusters = new ArrayList();
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

  public static JsonCluster jsonToJsonObject(String json) throws KaramelException {
    Gson gson = new Gson();
    JsonCluster jsonCluster = gson.fromJson(json, JsonCluster.class);
    return jsonCluster;
  }

  public static JsonCluster yamlToJsonObject(String yaml) throws KaramelException {
    YamlCluster cluster = yamlToYamlObject(yaml);
    JsonCluster jsonCluster = new JsonCluster(cluster);
    ////validate(jsonCluster);
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


  public static String serializeJson(JsonCluster jsonCluster) throws KaramelException {
    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();
    Gson gson = builder.setPrettyPrinting().create();
    String json = gson.toJson(jsonCluster);
    return json;
  }

  public static void validate(JsonCluster cluster) throws ValidationException {
    cluster.validate();

    boolean autoscale = false;
    String tablespoonSeverGroup = null;

    for (JsonGroup group : cluster.getGroups()) {
      Provider provider = getGroupProvider(cluster, group.getName());
      if (provider instanceof Baremetal) {
        Baremetal baremetal = (Baremetal) provider;
        int s1 = baremetal.retriveAllIps().size();
        if (s1 != group.getSize()) {
          throw new ValidationException(
                  String.format("Number of ip addresses is not equal to the group size %d != %d", s1, group.getSize()));
        }
      }
      autoscale |= group.isAutoScale();
      for (JsonCookbook jc : group.getCookbooks()) {
        ArrayList<JsonRecipe> recs = Lists.newArrayList(jc.getRecipes());
        for (int i = 0; i < recs.size(); i++) {
          String recName = recs.get(i).getCanonicalName();
          for (int j = i + 1; j < recs.size(); j++) {
            if (recName.equals(recs.get(j).getCanonicalName())) {
              throw new ValidationException(String.format("More than one %s in the group %s",
                      recs.get(i).getCanonicalName(), group.getName()));
            }
          }
          if (recName.equals("tablespoon-riemann::server")) {
            if (tablespoonSeverGroup == null && group.getSize() == 1) {
              tablespoonSeverGroup = group.getName();
            } else if (tablespoonSeverGroup != null) {
              throw new InconsistentDeploymentException("Assigning tablespoon-riemann::server in more than one group "
                      + "is not consistent");
            } else if (tablespoonSeverGroup == null && group.getSize() > 1) {
              throw new InconsistentDeploymentException("Assigning tablespoon-riemann::server into a group with more "
                      + "than one machine is not consistent");
            }
          }
        }
      }
    }

    if (autoscale && tablespoonSeverGroup == null) {
      throw new InconsistentDeploymentException(
              "To enable autoscaling you must locate tablespoon-riemann::server in a group");
    }

  }

  public static String clusterLinks(JsonCluster cluster, ClusterRuntime clusterEntity) throws KaramelException {
    StringBuilder builder = new StringBuilder();
    for (JsonGroup jg : cluster.getGroups()) {
      for (JsonCookbook jc : jg.getCookbooks()) {
        for (JsonRecipe rec : jc.getRecipes()) {
          String cbid = jc.getId();
          KaramelizedCookbook cb = CookbookCache.get(cbid);
          MetadataRb metadataRb = cb.getMetadataRb();
          List<Recipe> recipes = metadataRb.getRecipes();
          for (Recipe recipe : recipes) {
            if (recipe.getCanonicalName().equalsIgnoreCase(rec.getCanonicalName())) {
              Set<String> links = recipe.getLinks();
              for (String link : links) {
                if (link.contains(Settings.METADATA_INCOMMENT_HOST_KEY)) {
                  if (clusterEntity != null) {
                    GroupRuntime ge = findGroup(clusterEntity, jg.getName());
                    if (ge != null) {
                      List<MachineRuntime> machines = ge.getMachines();
                      if (machines != null) {
                        for (MachineRuntime me : ge.getMachines()) {
                          String l = link.replaceAll(Settings.METADATA_INCOMMENT_HOST_KEY, me.getPublicIp());
                          builder.append(l).append("\n");
                        }
                      }
                    }
                  }
                } else {
                  builder.append(link).append("\n");
                }

              }

            }
          }
        }
      }
    }
    return builder.toString();
  }

  public static int totalMachines(JsonCluster cluster) {
    int total = 0;
    for (JsonGroup g : cluster.getGroups()) {
      total += g.getSize();
    }
    return total;
  }

  public static JsonGroup findGroup(JsonCluster cluster, String groupName) {
    for (JsonGroup g : cluster.getGroups()) {
      if (g.getName().equals(groupName)) {
        return g;
      }
    }
    return null;
  }

  public static GroupRuntime findGroup(ClusterRuntime clusterEntity, String groupName) {
    for (GroupRuntime g : clusterEntity.getGroups()) {
      if (g.getName().equals(groupName)) {
        return g;
      }
    }
    return null;
  }

  public static Provider getGroupProvider(JsonCluster cluster, String groupName) {
    JsonGroup group = findGroup(cluster, groupName);
    Provider groupScopeProvider = group.getProvider();
    Provider clusterScopeProvider = cluster.getProvider();
    Provider provider = null;
    if (groupScopeProvider == null && clusterScopeProvider == null) {
      provider = Ec2.makeDefault();
    } else if (groupScopeProvider == null && clusterScopeProvider != null) {
      provider = (Provider) clusterScopeProvider.cloneMe();
      provider = provider.applyDefaults();
    } else if (groupScopeProvider != null && clusterScopeProvider != null) {
      provider = groupScopeProvider.applyParentScope(clusterScopeProvider);
      provider = provider.applyDefaults();
    }
    return provider;
  }

  public static String makeVendorPath(JsonCluster cluster) throws KaramelException {
    Set<String> paths = new HashSet<>();
    for (JsonGroup gr : cluster.getGroups()) {
      for (JsonCookbook cb : gr.getCookbooks()) {
        CookbookUrls urls = cb.getKaramelizedCookbook().getUrls();
        String cookbookPath = urls.repoName;
        if (urls.cookbookRelPath != null && !urls.cookbookRelPath.isEmpty()) {
          cookbookPath += Settings.SLASH + urls.cookbookRelPath;
        }
        paths.add(Settings.REMOTE_CB_VENDOR_PATH + Settings.SLASH + cookbookPath + Settings.SLASH
                + Settings.REMOTE_CB_VENDOR_SUBFOLDER);
      }
    }
    Object[] arr = paths.toArray();
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buffer.append("\n");
      }
      buffer.append("\"");
      buffer.append(arr[i]);
      buffer.append("\"");
      if (i < paths.size() - 1) {
        buffer.append(",");
      }
    }
    return buffer.toString();
  }

  public static boolean hasHoneyTap(JsonCluster cluster) {
    for (JsonGroup jg : cluster.getGroups()) {
      if (jg.isAutoScale()) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasTablespoon(JsonCluster cluster) {
    for (JsonGroup jg : cluster.getGroups()) {
      if (jg.isAutoScale()) {
        return true;
      }
    }
    return false;
  }

}