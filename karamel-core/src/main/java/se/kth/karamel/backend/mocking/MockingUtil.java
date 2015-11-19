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
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;

/**
 *
 * @author kamal
 */
public class MockingUtil {

  public static ClusterRuntime dummyRuntime(JsonCluster definition) {
    ClusterRuntime clusterRuntime = new ClusterRuntime(definition);
    for (GroupRuntime group : clusterRuntime.getGroups()) {
      //TODO: should add suport for other providers as well, GCE and baremetal machine type
      Ec2 ec2 = (Ec2) UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      JsonGroup definedGroup = UserClusterDataExtractor.findGroup(definition, group.getName());
      List<MachineRuntime> mcs = new ArrayList<>();
//      String ippref = "192.168.0.";
      String ippref = group.getName();
      for (int i = 1; i <= definedGroup.getSize(); i++) {
        String name = group.getName() + "-" + i;
        MachineRuntime ma = new MachineRuntime(group);
        //other machine types from GCE and BareMetal should be added
        ma.setMachineType("ec2/" + ec2.getRegion() + "/" + ec2.getType() + "/" + ec2.getAmi() + "/"
            + ec2.getVpc() + "/" + ec2.getPrice());
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
