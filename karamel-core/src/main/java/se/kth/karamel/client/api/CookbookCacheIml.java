/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * It caches cookbooks' metadata that being read from Github
 */
package se.kth.karamel.client.api;


import org.apache.log4j.Logger;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.dag.DagNode;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.cookbookmeta.CookbookUrls;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.util.IoUtils;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;
import se.kth.karamel.common.util.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author kamal
 */
public class CookbookCacheIml implements CookbookCache {

  private static final Logger logger = Logger.getLogger(CookbookCacheIml.class);

  public Map<String, KaramelizedCookbook> cookbooks = new HashMap<>();
  public Set<String> problematics = new HashSet<>();

  @Override
  public KaramelizedCookbook readNew(String cookbookUrl) throws KaramelException {
    if (problematics.contains(cookbookUrl)) {
      problematics.remove(cookbookUrl);
    }
    try {
      KaramelizedCookbook cookbook = new KaramelizedCookbook(cookbookUrl, false);
      cookbooks.put(cookbookUrl, cookbook);
      return cookbook;
    } catch (Exception e) {
      problematics.add(cookbookUrl);
      throw new NoKaramelizedCookbookException(
          String.format("Cookbook should problem in the metadata '%s'", cookbookUrl), e);
    }
  }

  @Override
  public KaramelizedCookbook get(String cookbookUrl) throws KaramelException {
    if (!problematics.contains(cookbookUrl)) {
      KaramelizedCookbook cb = cookbooks.get(cookbookUrl);
      if (cb == null) {
        cb = readNew(cookbookUrl);
      }
      return cb;
    } else {
      throw new NoKaramelizedCookbookException(String.format("Cookbook has problem in the metadata '%s'", cookbookUrl));
    }
  }

  @Override
  public synchronized void prepareParallel(Set<String> urlsToPrepare) throws KaramelException {
    Set<String> allUrls = new HashSet<>();
    for (String cookbookUrl : urlsToPrepare) {
      KaramelizedCookbook cb = cookbooks.get(cookbookUrl);
      if (cb == null) {
        CookbookUrls.Builder builder = new CookbookUrls.Builder();
        CookbookUrls urls = builder.url(cookbookUrl).build();
        allUrls.add(urls.attrFile);
        allUrls.add(urls.metadataFile);
        allUrls.add(urls.karamelFile);
        allUrls.add(urls.berksFile);
      }
    }
    Map<String, String> contents = IoUtils.readContentParallel(allUrls, ClusterService.SHARED_GLOBAL_TP);
    for (String cookbookUrl : urlsToPrepare) {
      KaramelizedCookbook cb = cookbooks.get(cookbookUrl);
      if (cb == null) {
        CookbookUrls.Builder builder = new CookbookUrls.Builder();
        CookbookUrls urls = builder.url(cookbookUrl).build();
        if (contents.containsKey(urls.attrFile)
            && contents.containsKey(urls.metadataFile)
            && contents.containsKey(urls.karamelFile)
            && contents.containsKey(urls.berksFile)) {
          KaramelizedCookbook kc = new KaramelizedCookbook(urls, contents.get(urls.attrFile),
              contents.get(urls.metadataFile), contents.get(urls.karamelFile), contents.get(urls.berksFile));
          cookbooks.put(cookbookUrl, kc);
        } else {
          problematics.add(cookbookUrl);
        }
      }
    }
  }

  @Override
  public void prepareNewParallel(Set<String> cookbookUrls) throws KaramelException {
    Set<String> allUrls = new HashSet<>();
    for (String cookbookUrl : cookbookUrls) {
      CookbookUrls.Builder builder = new CookbookUrls.Builder();
      CookbookUrls urls = builder.url(cookbookUrl).build();
      allUrls.add(urls.attrFile);
      allUrls.add(urls.metadataFile);
      allUrls.add(urls.karamelFile);
      allUrls.add(urls.berksFile);
    }
    Map<String, String> contents = IoUtils.readContentParallel(allUrls, ClusterService.SHARED_GLOBAL_TP);
    for (String cookbookUrl : cookbookUrls) {
      CookbookUrls.Builder builder = new CookbookUrls.Builder();
      CookbookUrls urls = builder.url(cookbookUrl).build();
      if (contents.containsKey(urls.attrFile)
          && contents.containsKey(urls.metadataFile)
          && contents.containsKey(urls.karamelFile)
          && contents.containsKey(urls.berksFile)) {
        KaramelizedCookbook kc = new KaramelizedCookbook(urls, contents.get(urls.attrFile),
            contents.get(urls.metadataFile), contents.get(urls.karamelFile), contents.get(urls.berksFile));
        cookbooks.put(cookbookUrl, kc);
      }
    }
  }

  @Override
  public List<KaramelizedCookbook> loadRootKaramelizedCookbooks(JsonCluster jsonCluster) throws KaramelException {
    List<JsonGroup> jsonGroups = jsonCluster.getGroups();
    Set<String> toLoad = new HashSet<>();
    for (JsonGroup jsonGroup : jsonGroups) {
      for (JsonCookbook cb : jsonGroup.getCookbooks()) {
        toLoad.add(cb.getId());
      }
    }
    for (JsonCookbook cb : jsonCluster.getCookbooks()) {
      toLoad.add(cb.getId());
    }
    Dag dag = new Dag();
    List<KaramelizedCookbook> kcbs = loadAllKaramelizedCookbooks(jsonCluster.getName(), toLoad, dag);
    List<KaramelizedCookbook> roots = new ArrayList<>();
    for (DagNode node : dag.findRootNodes()) {
      String id = node.getId();
      boolean found = false;
      for (KaramelizedCookbook kcb : kcbs) {
        if (kcb.getUrls().id.equals(id)) {
          roots.add(kcb);
          found = true;
          break;
        }
      }
      if (!found) {
        throw new NoKaramelizedCookbookException("Could not load a root cookbook, "
            + "make sure it is correctly karamelized " + id);
      }
    }
    return roots;
  }

  @Override
  public List<KaramelizedCookbook> loadAllKaramelizedCookbooks(JsonCluster jsonCluster) throws KaramelException {
    List<JsonGroup> jsonGroups = jsonCluster.getGroups();
    Set<String> toLoad = new HashSet<>();
    for (JsonGroup jsonGroup : jsonGroups) {
      for (JsonCookbook cb : jsonGroup.getCookbooks()) {
        toLoad.add(cb.getId());
      }
    }
    for (JsonCookbook cb : jsonCluster.getCookbooks()) {
      toLoad.add(cb.getId());
    }
    return loadAllKaramelizedCookbooks(jsonCluster.getName(), toLoad, new Dag());
  }

  @Override
  public List<KaramelizedCookbook> loadAllKaramelizedCookbooks(YamlCluster cluster) throws KaramelException {
    Set<String> toLoad = new HashSet<>();
    for (Cookbook cb : cluster.getCookbooks().values()) {
      toLoad.add(cb.getUrls().id);
    }

    Dag dag = new Dag();
    // An hacky fix for a crappy code. The thing is that, where this function is called, I'd like to have
    // also the attributes of dependencies (including the transient ones) for the filtering of valid attributes.
    // So here we build a Topological sort of the dependency graph, reverse it, and traverse it.
    // For each KaramelizedCookbook we add in a set all the *references* to the dependency, both direct and transient
    // that's the reason of using the topological sort.
    // As we are using references to the same set of KaramelizedCookbooks, the default methods for equals and hashcode
    // (comparing addresses) works fine.

    // Result ignored on purpose
    loadAllKaramelizedCookbooks(cluster.getName(), toLoad, dag);
    List<KaramelizedCookbook> topologicalSort = new ArrayList<>();

    Set<DagNode> rootNodes = dag.findRootNodes();
    while (!rootNodes.isEmpty()) {
      for (DagNode dagNode : rootNodes) {
        KaramelizedCookbook kcb = cookbooks.get(dagNode.getId());
        if (kcb != null) {
          topologicalSort.add(cookbooks.get(dagNode.getId()));
        }
        dag.removeNode(dagNode);
      }
      rootNodes = dag.findRootNodes();
    }

    // Reverse the list
    Collections.reverse(topologicalSort);

    for (KaramelizedCookbook kcb : topologicalSort) {
      Map<String, Cookbook> dependencies = kcb.getBerksFile().getDeps();
      for (Cookbook cookbook : dependencies.values()) {
        KaramelizedCookbook depKbc = cookbooks.get(cookbook.getUrls().id);
        if (depKbc != null) {
          kcb.addDependency(depKbc);
          kcb.addDependencies(depKbc.getDependencies());
        }
      }
    }

    return topologicalSort;
  }

  private List<KaramelizedCookbook> loadAllKaramelizedCookbooks(String clusterName, Set<String> toLoad, Dag dag)
      throws KaramelException {
    Set<String> loaded = new HashSet<>();
    List<KaramelizedCookbook> all = new ArrayList<>();

    int level = 0;

    while (!toLoad.isEmpty()) {
      logger.info(String.format("%d-level cookbooks for %s is %d", level++, clusterName, toLoad.size()));
      prepareParallel(toLoad);
      for (String tl : toLoad) {
        dag.addNode(tl);
        if (problematics.contains(tl)) {
          dag.updateLabel(tl, "NON_KARAMELIZED");
        } else {
          dag.updateLabel(tl, "OK");
        }
      }
      toLoad.removeAll(problematics);
      Set<String> depsToLoad = new HashSet();
      for (String cbid : toLoad) {
        KaramelizedCookbook kc = get(cbid);
        all.add(kc);
        if (!Settings.CB_CLASSPATH_MODE) {
          Map<String, Cookbook> deps = kc.getBerksFile().getDeps();
          for (Cookbook cb : deps.values()) {
            String depId = cb.getUrls().id;
            dag.addDependency(cbid, depId);
            if (!loaded.contains(depId) && !problematics.contains(depId)) {
              depsToLoad.add(depId);
            }
          }
        }
      }
      loaded.addAll(toLoad);
      toLoad.clear();
      toLoad.addAll(depsToLoad);

    }
    logger.debug(String.format("################## COOKBOOK TRANSIENT DEPENDENCIES FOR %s " +
            "############", clusterName));
    logger.debug(dag.print());
    logger.debug("############################################################################");
    return all;
  }
}
