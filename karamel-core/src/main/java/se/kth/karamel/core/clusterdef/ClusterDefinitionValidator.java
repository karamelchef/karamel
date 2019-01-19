package se.kth.karamel.core.clusterdef;

import com.google.common.collect.Lists;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.json.JsonRecipe;
import se.kth.karamel.common.exception.ValidationException;

import java.util.ArrayList;

/**
 *
 * @author kamal
 */
public class ClusterDefinitionValidator {

  public static void validate(JsonCluster cluster) throws ValidationException {
    cluster.validate();

    for (JsonGroup group : cluster.getGroups()) {
      Provider provider = UserClusterDataExtractor.getGroupProvider(cluster, group.getName());
      if (provider instanceof Baremetal) {
        Baremetal baremetal = (Baremetal) provider;
        int s1 = baremetal.retriveAllIps().size();
        if (s1 != group.getSize()) {
          throw new ValidationException(
              String.format("Number of ip addresses is not equal to the group size %d != %d", s1, group.getSize()));
        }
      }

      ArrayList<JsonRecipe> recs = Lists.newArrayList(group.getRecipes());
      for (int i = 0; i < recs.size(); i++) {
        for (int j = i + 1; j < recs.size(); j++) {
          if (recs.get(i).getCanonicalName().equals(recs.get(j).getCanonicalName()))
            throw new ValidationException(
                String.format("More than one %s in the group %s", recs.get(i).getCanonicalName(), group.getName()));
        }
      }
    }
  }
}
