/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.machines;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.running.model.MachineEntity;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.common.Confs;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class MachinesMonitor implements Runnable {

  private static final Logger logger = Logger.getLogger(MachinesMonitor.class);
  private final String clusterName;
  private final Map<String, SshMachine> machines = new HashMap<>();
  private boolean paused = false;
  ExecutorService executor;
  private final SshKeyPair keyPair;

  public MachinesMonitor(String clusterName, int numMachines, SshKeyPair keyPair) {
    this.keyPair = keyPair;
    this.clusterName = clusterName;
    executor = Executors.newFixedThreadPool(numMachines);
  }

  public synchronized void addMachines(List<MachineEntity> machineEntities) {
    for (MachineEntity machineEntity : machineEntities) {
      SshMachine sshMachine = new SshMachine(machineEntity, keyPair.getPublicKey(), keyPair.getPrivateKey());
      machines.put(machineEntity.getId(), sshMachine);
      executor.execute(sshMachine);
    }
  }

  public synchronized void resume() {
    if (paused) {
      for (Map.Entry<String, SshMachine> entry : machines.entrySet()) {
        SshMachine sshMachine = entry.getValue();
        sshMachine.resume();
      }
      paused = false;
    }
  }

  public synchronized void pause() {
    if (!paused) {
      for (Map.Entry<String, SshMachine> entry : machines.entrySet()) {
        SshMachine sshMachine = entry.getValue();
        sshMachine.pause();
      }
      paused = true;
    }
  }

  @Override
  public void run() {
    logger.info(String.format("Machines-Monitor started for '%s' d'-'", clusterName));
    while (true) {
      if (!paused) {
        Set<Map.Entry<String, SshMachine>> entrySet = machines.entrySet();
        for (Map.Entry<String, SshMachine> entry : entrySet) {
          SshMachine machine = entry.getValue();
          try {
            machine.ping();
          } catch (KaramelException ex) {
            logger.error("", ex);
          }
        }
      } else {
        logger.info(String.format("Cluster %s is on pause, a failure might have happened.", clusterName));
      }

      try {
        Thread.currentThread().sleep(Settings.SSH_PING_INTERVAL);
      } catch (InterruptedException ex) {
        logger.error("Someone knocked on my door (-_-)zzz", ex);
      }
    }
  }

  public synchronized void runTask(Task task) throws KaramelException {
    logger.debug(String.format("Recieved '%s' from DAG", task.toString()));
    String machineName = task.getMachineId();
    if (!machines.containsKey(machineName)) {
      throw new KaramelException(String.format("Machine '%s' does not exist in manager", machineName));
    }
    SshMachine machine = machines.get(machineName);
    machine.enqueue(task);
  }

  public synchronized void disconnect() throws KaramelException {
    Set<Map.Entry<String, SshMachine>> entrySet = machines.entrySet();
    for (Map.Entry<String, SshMachine> entry : entrySet) {
      SshMachine machine = entry.getValue();
      machine.disconnect();
    }
  }
}
