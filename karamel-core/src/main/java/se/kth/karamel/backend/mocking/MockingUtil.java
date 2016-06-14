/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.mocking;

import java.util.ArrayList;
import java.util.List;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.Provider;
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
      Provider provider = ClusterDefinitionService.getGroupProvider(definition, group.getName());
      String machineType = null;
      if (provider instanceof Ec2) {
        Ec2 ec2 = (Ec2) provider;
        machineType = "ec2/" + ec2.getRegion() + "/" + ec2.getType() + "/" + ec2.getAmi() + "/"
            + ec2.getVpc() + "/" + ec2.getPrice();
      } else if (provider instanceof Baremetal) {
        Baremetal baremetal = (Baremetal) provider;
        machineType = "baremetal";
      } else if (provider instanceof Gce) {
        Gce gce = (Gce) provider;
        machineType = "gce/" + gce.getZone() + "/" + gce.getType() + "/" + gce.getImage();
      }

      JsonGroup definedGroup = ClusterDefinitionService.findGroup(definition, group.getName());
      List<MachineRuntime> mcs = new ArrayList<>();
//      String ippref = "192.168.0.";
      String ippref = group.getName();
      for (int i = 1; i <= definedGroup.getSize(); i++) {
        String name = group.getName() + "-" + i;
        MachineRuntime ma = new MachineRuntime(group);
        ma.setMachineType(machineType);
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
