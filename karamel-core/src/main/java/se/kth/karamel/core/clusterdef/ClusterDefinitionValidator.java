/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.core.clusterdef;

import com.google.common.collect.Lists;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonCookbook;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.clusterdef.json.JsonRecipe;
import se.kth.karamel.common.exception.ValidationException;

import java.util.ArrayList;
import se.kth.karamel.common.exception.InconsistentDeploymentException;

/**
 *
 * @author kamal
 */
public class ClusterDefinitionValidator {

  public static void validate(JsonCluster cluster) throws ValidationException {
    cluster.validate();

    boolean autoascalingenabled = false;
    String tablespoonSeverGroup = null;

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
      autoascalingenabled |= group.getAutoScalingEnabled();
      for (JsonCookbook jc : group.getCookbooks()) {
        ArrayList<JsonRecipe> recs = Lists.newArrayList(jc.getRecipes());
        for (int i = 0; i < recs.size(); i++) {
          String recName = recs.get(i).getCanonicalName();
          for (int j = i + 1; j < recs.size(); j++) {
            if (recName.equals(recs.get(j).getCanonicalName())) {
              throw new ValidationException(
                  String.format("More than one %s in the group %s", recs.get(i).getCanonicalName(), group.getName()));
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

    if (autoascalingenabled && tablespoonSeverGroup == null) {
      throw new InconsistentDeploymentException(
          "To enable autoscaling you must locate tablespoon-riemann::server in a group");
    }

  }
}
