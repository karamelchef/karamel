/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.mocking;

import java.util.ArrayList;
import java.util.List;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonGroup;

/**
 *
 * @author kamal
 */
public class MockingUtil {

  public static ClusterRuntime dummyRuntime(JsonCluster definition) {
    ClusterRuntime clusterRuntime = new ClusterRuntime(definition);
    for (GroupRuntime group : clusterRuntime.getGroups()) {
      JsonGroup definedGroup = UserClusterDataExtractor.findGroup(definition, group.getName());
      List<MachineRuntime> mcs = new ArrayList<>();
//      String ippref = "192.168.0.";
      String ippref = group.getName();
      for (int i = 1; i <= definedGroup.getSize(); i++) {
        String name = group.getName() + "-" + i;
        MachineRuntime ma = new MachineRuntime(group);
        ma.setName(name);
        ma.setPublicIp(ippref + i);
        ma.setPrivateIp(ippref + i);
        ma.setSshUser("ubuntu");
        mcs.add(ma);
      }
      group.setMachines(mcs);
    }

    return clusterRuntime;
  }
}
