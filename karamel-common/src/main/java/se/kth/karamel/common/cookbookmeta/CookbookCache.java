package se.kth.karamel.common.cookbookmeta;

import java.util.List;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.exception.KaramelException;

public interface CookbookCache {

  KaramelizedCookbook get(String cookbookUrl) throws KaramelException;

  List<KaramelizedCookbook> loadAllKaramelizedCookbooks(JsonCluster cluster) throws KaramelException;
  
  List<KaramelizedCookbook> loadAllKaramelizedCookbooks(YamlCluster cluster) throws KaramelException;

}
