/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.launcher.baremetal;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.launcher.Launcher;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class BaremetalLauncher extends Launcher {

  private static final Logger logger = Logger.getLogger(BaremetalLauncher.class);

  public final SshKeyPair sshKeyPair;

  public BaremetalLauncher(SshKeyPair sshKeyPair) {
    this.sshKeyPair = sshKeyPair;
    logger.info(String.format("Public-key='%s'", sshKeyPair.getPublicKeyPath()));
    logger.info(String.format("Private-key='%s'", sshKeyPair.getPrivateKeyPath()));
  }

  @Override
  public void cleanup(JsonCluster definition, ClusterRuntime runtime) throws KaramelException {
    logger.info("It is baremetal, cleanup is skipped.");
  }

  @Override
  public String forkGroup(JsonCluster definition, ClusterRuntime runtime, String groupName) throws KaramelException {
    logger.info(String.format("Provider of %s is baremetal, fork-group is skipped.", groupName));
    return groupName;
  }

  @Override
  public List<MachineRuntime> forkMachines(JsonCluster definition, ClusterRuntime runtime, String groupName) 
      throws KaramelException {
    logger.info(String.format("Provider of %s is baremetal, available machines expected.", groupName));
    GroupRuntime gr = ClusterDefinitionService.findGroup(runtime, groupName);
    Baremetal baremetal = (Baremetal) ClusterDefinitionService.getGroupProvider(definition, groupName);
    String username = baremetal.getUsername();
    List<MachineRuntime> machines = new ArrayList<>();
    for (String ip : baremetal.retriveAllIps()) {
      MachineRuntime machine = new MachineRuntime(gr);
      machine.setMachineType("baremetal");
      machine.setName(ip);
      machine.setPrivateIp(ip);
      machine.setPublicIp(ip);
      machine.setSshPort(Settings.BAREMETAL_DEFAULT_SSH_PORT);
      machine.setSshUser(username);
      machines.add(machine);
    }
    return machines;
  }

}
