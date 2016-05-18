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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import se.kth.autoscalar.scaling.exceptions.AutoScalarException;
import se.kth.autoscalar.scaling.monitoring.MachineMonitoringEvent;
import se.kth.autoscalar.scaling.monitoring.MonitoringListener;
import se.kth.karamel.backend.ClusterManager;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class MachinesMonitor implements TaskSubmitter, Runnable {

  private static final Logger logger = Logger.getLogger(MachinesMonitor.class);
  private final String clusterName;
  private final Map<String, SshMachine> activeMachines = new HashMap<>();
  private final Map<String, SshMachine> decomissionedMachines = new HashMap<>();
  private boolean paused = false;
  ExecutorService executor;
  private final SshKeyPair keyPair;
  private boolean stopping = false;
  private final ClusterManager clusterManager;

  public MachinesMonitor(String clusterName, int numMachines, SshKeyPair keyPair, ClusterManager clusterManager) {
    this.keyPair = keyPair;
    this.clusterName = clusterName;
    this.clusterManager = clusterManager;
    executor = Executors.newFixedThreadPool(numMachines);
  }

  public void setStopping(boolean stopping) {
    for (Map.Entry<String, SshMachine> entry : activeMachines.entrySet()) {
      SshMachine sshMachine = entry.getValue();
      sshMachine.setStopping(true);
    }
    this.stopping = stopping;
  }

  public SshMachine getMachine(String publicIp) {
    for (Map.Entry<String, SshMachine> entry : activeMachines.entrySet()) {
      SshMachine sshMachine = entry.getValue();
      if (sshMachine.getMachineRuntime().getPublicIp().equals(publicIp)) {
        return sshMachine;
      }
    }
    return null;
  }

  public void addMachines(List<MachineRuntime> machineEntities) {
    for (MachineRuntime machineEntity : machineEntities) {
      SshMachine sshMachine = new SshMachine(machineEntity, keyPair.getPublicKey(), keyPair.getPrivateKey(),
          keyPair.getPassphrase());
      activeMachines.put(machineEntity.getId(), sshMachine);
      Future<?> machineFuture = executor.submit(sshMachine);
      sshMachine.setFuture(machineFuture);
    }
  }

  public void resume() {
    if (paused) {
      logger.info("Sending resume signal to all machines");
      for (Map.Entry<String, SshMachine> entry : activeMachines.entrySet()) {
        SshMachine sshMachine = entry.getValue();
        sshMachine.resume();
      }
      paused = false;
    }
  }

  public void pause() {
    if (!paused) {
      logger.info("Sending pause signal to all machines");
      for (Map.Entry<String, SshMachine> entry : activeMachines.entrySet()) {
        SshMachine sshMachine = entry.getValue();
        sshMachine.pause();
      }
      paused = true;
    }
  }

  private void decomissionAndReportToAutoscalar(SshMachine machine) throws AutoScalarException {
    MachineRuntime runtime = machine.getMachineRuntime();
    Map<String, MonitoringListener> autoscalerListenersMap = clusterManager.getAutoscalerListenersMap();
    String gid = runtime.getGroup().getId();
    MonitoringListener listener = autoscalerListenersMap.get(gid);
    if (runtime.getLifeStatus() == MachineRuntime.LifeStatus.DECOMMISSIONINING) {
      Future future = machine.getFuture();
      future.cancel(true);
      runtime.setLifeStatus(MachineRuntime.LifeStatus.DECOMMISSIONED);
      activeMachines.remove(runtime.getId());
      decomissionedMachines.put(runtime.getId(), machine);
      listener.onStateChange(gid, new MachineMonitoringEvent(gid,
          runtime.getId(), MachineMonitoringEvent.Status.KILLED));
      //TODO: We must reheal all running DAGs here
    } else if (runtime.getForkingTime() != null) {
      long rentTime = System.currentTimeMillis() - runtime.getForkingTime();
      long remainedBillingTime = rentTime % Settings.HOURE_IN_MS;
      if (remainedBillingTime > Settings.MACHINE_BILLING_PERIOD_REPORT_MARGIN) {
        MachineMonitoringEvent event = new MachineMonitoringEvent(gid, runtime.getId(), 
            MachineMonitoringEvent.Status.AT_END_OF_BILLING_PERIOD);
        event.setTimeRemaining(remainedBillingTime);
        listener.onStateChange(gid, event);
      }
    }
  }

  @Override
  public void run() {
    logger.info(String.format("Machines-Monitor started for '%s' d'-'", clusterName));
    while (true && !stopping) {
      try {
        Set<Map.Entry<String, SshMachine>> entrySet = new HashSet<>();
        entrySet.addAll(activeMachines.entrySet());
        for (Map.Entry<String, SshMachine> entry : entrySet) {
          SshMachine machine = entry.getValue();
          machine.ping();
          if (machine.getMachineRuntime().getGroup().isAutoScalingEnabled()) {
            decomissionAndReportToAutoscalar(machine);
          }
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
    if (!activeMachines.containsKey(machineName)) {
      throw new KaramelException(String.format("Machine '%s' does not exist in manager", machineName));
    }
    SshMachine machine = activeMachines.get(machineName);
    machine.enqueue(task);
    // TODO - check if there is a return value....
  }

  public void disconnect() throws KaramelException {
    Set<Map.Entry<String, SshMachine>> entrySet = activeMachines.entrySet();
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

  @Override
  public void terminate(Task task) throws KaramelException {
    logger.debug(String.format("Recieved '%s' from DAG to remove", task.toString()));
    String machineName = task.getMachineId();
    if (!activeMachines.containsKey(machineName)) {
      throw new KaramelException(String.format("Machine '%s' does not exist in manager", machineName));
    }
    SshMachine sshMachine = activeMachines.get(machineName);
    sshMachine.remove(task);
    MachineRuntime machine = task.getMachine();
    machine.removeTask(task);
  }

  @Override
  public void killMe(Task task) throws KaramelException {
    String machineName = task.getMachineId();
    if (!activeMachines.containsKey(machineName)) {
      throw new KaramelException(String.format("Machine '%s' does not exist in manager", machineName));
    }
    SshMachine machine = activeMachines.get(machineName);
    machine.killTaskSession(task);
  }

  @Override
  public void retryMe(Task task) throws KaramelException {
    String machineName = task.getMachineId();
    if (!activeMachines.containsKey(machineName)) {
      throw new KaramelException(String.format("Machine '%s' does not exist in manager", machineName));
    }
    SshMachine machine = activeMachines.get(machineName);
    machine.retryFailedTask(task);
  }

  @Override
  public void skipMe(Task task) throws KaramelException {
    String machineName = task.getMachineId();
    if (!activeMachines.containsKey(machineName)) {
      throw new KaramelException(String.format("Machine '%s' does not exist in manager", machineName));
    }
    SshMachine machine = activeMachines.get(machineName);
    machine.skipFailedTask(task);
  }
}
