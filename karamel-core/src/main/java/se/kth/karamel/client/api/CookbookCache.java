/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * It caches cookbooks' metadata that being read from Github
 */
package se.kth.karamel.client.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.common.cookbookmeta.CookbookUrls;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.util.IoUtils;

/**
 *
 * @author kamal
 */
public class CookbookCache {

  public static Map<String, KaramelizedCookbook> cookbooks = new HashMap<>();

  public static KaramelizedCookbook readNew(String cookbookUrl) throws KaramelException {
    KaramelizedCookbook cookbook = new KaramelizedCookbook(cookbookUrl, false);
    cookbooks.put(cookbookUrl, cookbook);
    return cookbook;
  }

  public static KaramelizedCookbook get(String cookbookUrl) throws KaramelException {
    KaramelizedCookbook cb = cookbooks.get(cookbookUrl);
    if (cb == null) {
      cb = readNew(cookbookUrl);
    }
    return cb;
  }

  public static void prepareParallel(Set<String> cookbookUrls) throws KaramelException {
    Set<String> allUrls = new HashSet<>();
    for (String cookbookUrl : cookbookUrls) {
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
    for (String cookbookUrl : cookbookUrls) {
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
        }
      }
    }
  }

  public static void prepareNewParallel(Set<String> cookbookUrls) throws KaramelException {
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
}
