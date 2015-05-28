/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.machines;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class MachinesMonitor implements TaskSubmitter, Runnable {

  private static final Logger logger = Logger.getLogger(MachinesMonitor.class);
  private final String clusterName;
  private final Map<String, SshMachine> machines = new HashMap<>();
  private boolean paused = false;
  ExecutorService executor;
  private final SshKeyPair keyPair;
  private boolean stopping = false;

  public MachinesMonitor(String clusterName, int numMachines, SshKeyPair keyPair) {
    this.keyPair = keyPair;
    this.clusterName = clusterName;
    executor = Executors.newFixedThreadPool(numMachines);
  }

  public void setStopping(boolean stopping) {
    for (Map.Entry<String, SshMachine> entry : machines.entrySet()) {
      SshMachine sshMachine = entry.getValue();
      sshMachine.setStopping(true);
    }
    this.stopping = stopping;
  }

  public SshMachine getMachine(String publicIp) {
    for (Map.Entry<String, SshMachine> entry : machines.entrySet()) {
      SshMachine sshMachine = entry.getValue();
      if (sshMachine.getMachineEntity().getPublicIp().equals(publicIp)) {
        return sshMachine;
      }
    }
    return null;
  }

  public void addMachines(List<MachineRuntime> machineEntities) {
    for (MachineRuntime machineEntity : machineEntities) {
      SshMachine sshMachine = new SshMachine(machineEntity, keyPair.getPublicKey(), keyPair.getPrivateKey(),
          keyPair.getPassphrase());
      machines.put(machineEntity.getId(), sshMachine);
      executor.execute(sshMachine);
    }
  }

  public void resume() {
    if (paused) {
      logger.info("Sending resume signal to all machines");
      for (Map.Entry<String, SshMachine> entry : machines.entrySet()) {
        SshMachine sshMachine = entry.getValue();
        sshMachine.resume();
      }
      paused = false;
    }
  }

  public void pause() {
    if (!paused) {
      logger.info("Sending pause signal to all machines");
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
    while (true && !stopping) {
      try {
        Set<Map.Entry<String, SshMachine>> entrySet = new HashSet<>();
        entrySet.addAll(machines.entrySet());
        for (Map.Entry<String, SshMachine> entry : entrySet) {
          SshMachine machine = entry.getValue();
          machine.ping();
        }

        try {
          Thread.currentThread().sleep(Settings.SSH_PING_INTERVAL);
        } catch (InterruptedException ex) {
          if (stopping) {
            logger.error("Terminating machines threadpool");
            executor.shutdownNow();
            try {
              executor.awaitTermination(1, TimeUnit.MINUTES);
              logger.info("Machines threadpool terminated");
              logger.info(String.format("Machines-Monitor stoped for '%s' d'-'", clusterName));
              return;
            } catch (InterruptedException ex1) {
            }
          } else {
            logger.error("Got interupted without having recived the stopping signal..", ex);
          }
        }
      } catch (Exception ex) {
        logger.error("", ex);
      }
    }
  }

  @Override
  public void submitTask(Task task) throws KaramelException {
    logger.debug(String.format("Recieved '%s' from DAG", task.toString()));
    String machineName = task.getMachineId();
    if (!machines.containsKey(machineName)) {
      throw new KaramelException(String.format("Machine '%s' does not exist in manager", machineName));
    }
    SshMachine machine = machines.get(machineName);
    machine.enqueue(task);
    // TODO - check if there is a return value....
  }

  public void disconnect() throws KaramelException {
    Set<Map.Entry<String, SshMachine>> entrySet = machines.entrySet();
    for (Map.Entry<String, SshMachine> entry : entrySet) {
      SshMachine machine = entry.getValue();
      machine.disconnect();
    }
  }

  @Override
  public void prepareToStart(Task task) throws KaramelException {
    MachineRuntime machine = task.getMachine();
    machine.addTask(task);
  }
}
