/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.client.model;

import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class ClusterDefinitionValidator {
  
  public static void validate(JsonCluster cluster) throws ValidationException{
    cluster.validate();
    
    for (JsonGroup group : cluster.getGroups()) {
      Provider provider = UserClusterDataExtractor.getGroupProvider(cluster, group.getName());
      if(provider instanceof Baremetal) {
        Baremetal baremetal = (Baremetal) provider;
        int s1 = baremetal.retriveAllIps().size();
        if (s1 != group.getSize()) {
          throw new ValidationException(
              String.format("Number of ip addresses is not equal to the group size %d != %d", s1, group.getSize()));
        }
      }
    }
  }
}
