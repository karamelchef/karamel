/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * It caches cookbooks' metadata that being read from Github
 */
package se.kth.karamel.common.cookbookmeta;

import java.util.List;
import java.util.Set;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public interface CookbookCache {

  public KaramelizedCookbook readNew(String cookbookUrl) throws KaramelException;

  public KaramelizedCookbook get(String cookbookUrl) throws KaramelException;

  public void prepareParallel(Set<String> cookbookUrls) throws KaramelException;

  public void prepareNewParallel(Set<String> cookbookUrls) throws KaramelException;

  public List<KaramelizedCookbook> loadRootKaramelizedCookbooks(JsonCluster cluster) throws KaramelException;
  
  public List<KaramelizedCookbook> loadAllKaramelizedCookbooks(YamlCluster cluster) throws KaramelException;

  public List<KaramelizedCookbook> loadAllKaramelizedCookbooks(JsonCluster cluster) throws KaramelException;
  
}
