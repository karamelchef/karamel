package se.kth.karamel.common.cookbookmeta;

import java.util.List;
import java.util.Set;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.exception.KaramelException;

public interface CookbookCache {

  KaramelizedCookbook readNew(String cookbookUrl) throws KaramelException;

  KaramelizedCookbook get(String cookbookUrl) throws KaramelException;

  void prepareParallel(Set<String> cookbookUrls) throws KaramelException;

  void prepareNewParallel(Set<String> cookbookUrls) throws KaramelException;

  List<KaramelizedCookbook> loadRootKaramelizedCookbooks(JsonCluster cluster) throws KaramelException;
  
  List<KaramelizedCookbook> loadAllKaramelizedCookbooks(YamlCluster cluster) throws KaramelException;

}
