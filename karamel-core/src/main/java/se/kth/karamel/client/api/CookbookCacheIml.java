/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * It caches cookbooks' metadata that being read from Github
 */
package se.kth.karamel.client.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.cookbookmeta.CookbookUrls;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.util.IoUtils;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.exception.NoKaramelizedCookbookException;

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
    System.out.println(contents.size());
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
  public List<KaramelizedCookbook> loadAllKaramelizedCookbooks(YamlCluster cluster) throws KaramelException {
    Set<String> toLoad = new HashSet<>();
    Dag dag = new Dag();
    
    for (Cookbook cb : cluster.getCookbooks().values()) {
      toLoad.add(cb.getUrls().id);
    }
    Set<String> loaded = new HashSet<>();
    List<KaramelizedCookbook> all = new ArrayList<>();
    
    int level = 0;
    
    while (!toLoad.isEmpty()) {
      logger.info(String.format("%d-level cookbooks for %s is %d", level++, cluster.getName(), toLoad.size()));
      prepareParallel(toLoad);
      for (String tl : toLoad) {
        dag.addNode(tl);
        if (problematics.contains(tl)) {
          dag.updateLabel(tl, "NON_KARAMELIZED");
        } else {
          dag.updateLabel(tl, "OK");
        }
      }
      loaded.addAll(toLoad);
      loaded.removeAll(problematics);
      toLoad.clear();
      
      for (String cbid : loaded) {
        KaramelizedCookbook kc = get(cbid);
        all.add(kc);
        Map<String, Cookbook> deps = kc.getBerksFile().getDeps();
        for (Cookbook cb : deps.values()) {
          String depId = cb.getUrls().id;
          dag.addDependency(cbid, depId);
          if (!loaded.contains(depId) && !problematics.contains(depId)) {
            toLoad.add(depId);
          }
        }
      }
    }
    System.out.println(dag.print());
    return all;
  }
}
