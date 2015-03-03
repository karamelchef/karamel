/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.HashSet;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ChefJsonGenerator;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.dag.MultiThreadedDagExecutor;
import se.kth.karamel.backend.launcher.amazon.Ec2Launcher;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.running.model.ClusterEntity;
import se.kth.karamel.backend.running.model.GroupEntity;
import se.kth.karamel.backend.running.model.MachineEntity;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.client.model.Ec2;
import se.kth.karamel.client.model.Provider;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class ClusterManager implements Runnable {

  public static enum Command {

    LAUNCH, PAUSE, RESUME, PURGE
  }

  private static final Logger logger = Logger.getLogger(ClusterManager.class);
  private final JsonCluster definition;
  private final ClusterEntity runtime;
  private final MachinesMonitor machinesMonitor;
  private final ClusterStatusMonitor clusterStatusMonitor;
  private Dag installationDag;
  MultiThreadedDagExecutor executor;
  private final BlockingQueue<Command> cmdQueue = new ArrayBlockingQueue<>(1);
  ExecutorService tpool;
  private final ClusterContext clusterContext;
  private Ec2Launcher ec2Launcher;
  private Future<?> clusterManagerFuture = null;
  private Future<?> machinesMonitorFuture = null;
  private Future<?> clusterStatusFuture = null;
  private boolean stoping = false;

  public ClusterManager(JsonCluster definition, ClusterContext clusterContext) {
    this.clusterContext = clusterContext;
    this.definition = definition;
    this.runtime = new ClusterEntity(definition);
    int totalMachines = UserClusterDataExtractor.totalMachines(definition);
    machinesMonitor = new MachinesMonitor(definition.getName(), totalMachines, clusterContext.getSshKeyPair());
    clusterStatusMonitor = new ClusterStatusMonitor(machinesMonitor, definition, runtime);
  }

  public JsonCluster getDefinition() {
    return definition;
  }

  public ClusterEntity getRuntime() {
    return runtime;
  }

  public synchronized void enqueue(Command command) throws KaramelException {
    if (!cmdQueue.offer(command)) {
      String msg = String.format("Sorry!! have to reject '%s' for '%s', try later (._.)", command, definition.getName());
      logger.error(msg);
      throw new KaramelException(msg);
    }
    if (command == Command.PURGE) {
      stoping = true;
    }
    if ((command == Command.PAUSE || command == Command.PURGE) && cmdQueue.remainingCapacity() == 0) {
      if (clusterManagerFuture != null && !clusterManagerFuture.isCancelled()) {
        logger.info(String.format("Interrupting cluster manager of '%s'", definition.getName()));
        clusterManagerFuture.cancel(true);
      }
    }

  }

  public void start() {
    tpool = Executors.newFixedThreadPool(3);
    clusterManagerFuture = tpool.submit(this);
    machinesMonitorFuture = tpool.submit(machinesMonitor);
    clusterStatusFuture = tpool.submit(clusterStatusMonitor);
  }

  public void stop() throws InterruptedException {
    if (executor != null && !executor.isShutdown()) {
      logger.info(String.format("Terminating the installation DAG of '%s'", definition.getName()));
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.MINUTES);
    }
    machinesMonitor.setStoping(true);
    if (machinesMonitorFuture != null && !machinesMonitorFuture.isCancelled()) {
      logger.info(String.format("Terminating machines monitor of '%s'", definition.getName()));
      machinesMonitorFuture.cancel(true);
    }
    clusterStatusMonitor.setStoping(true);
    if (clusterStatusFuture != null && !clusterStatusFuture.isCancelled()) {
      logger.info(String.format("Terminating cluster status monitor of '%s'", definition.getName()));
      clusterStatusFuture.cancel(true);
    }
    stoping = true;
    if (clusterManagerFuture != null && !clusterManagerFuture.isCancelled()) {
      logger.info(String.format("Terminating cluster manager of '%s'", definition.getName()));
      clusterManagerFuture.cancel(true);
    }
  }

  private synchronized void clean(boolean purging) {
    if (purging) {
      logger.info(String.format("Purging '%s' ...", definition.getName()));
      runtime.setPhase(ClusterEntity.ClusterPhases.PURGING);
    } else {
      logger.info(String.format("Prelaunch Cleaning '%s' ...", definition.getName()));
      runtime.setPhase(ClusterEntity.ClusterPhases.PRECLEANING);
    }

    runtime.setFailed(false);
    List<GroupEntity> groups = runtime.getGroups();
    for (GroupEntity group : groups) {
      if (purging) {
        group.setPhase(GroupEntity.GroupPhase.PURGING);
      } else {
        group.setPhase(GroupEntity.GroupPhase.PRECLEANING);
      }
      group.setFailed(false);
      Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      if (provider instanceof Ec2) {
        try {
          Ec2 ec2 = (Ec2) provider;
          if (ec2Launcher == null) {
            ec2Launcher = new Ec2Launcher(clusterContext.getEc2Context(), clusterContext.getSshKeyPair());
          }
          ec2Launcher.cleanup(group.getName(), ec2.getRegion());
          if (purging) {
            group.setMachines(Collections.EMPTY_LIST);
            group.setPhase(GroupEntity.GroupPhase.NONE);
          } else {
            group.setPhase(GroupEntity.GroupPhase.PRECLEANED);
          }
        } catch (Exception ex) {
          group.setFailed(true);
          runtime.setFailed(true);
        }
      }
    }

    if (purging) {
      runtime.setPhase(ClusterEntity.ClusterPhases.NONE);
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' PURGED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
    } else if (!runtime.isFailed()) {
      runtime.setPhase(ClusterEntity.ClusterPhases.PRECLEANED);
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' PRECLEANED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
    }
  }

  private synchronized void forkGroups() throws InterruptedException {
    logger.info(String.format("Froking groups '%s' ...", definition.getName()));
    runtime.setPhase(ClusterEntity.ClusterPhases.FORKING_GROUPS);
    runtime.setFailed(false);
    List<GroupEntity> groups = runtime.getGroups();
    for (GroupEntity group : groups) {
      if (group.getPhase() == GroupEntity.GroupPhase.PRECLEANED || (group.getPhase() == GroupEntity.GroupPhase.FORKING_GROUPS && group.isFailed())) {
        group.setPhase(GroupEntity.GroupPhase.FORKING_GROUPS);
        group.setFailed(false);
        Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
        if (provider instanceof Ec2) {
          try {
            Ec2 ec2 = (Ec2) provider;
            Set<String> ports = new HashSet<>();
            ports.addAll(Settings.EC2_DEFAULT_PORTS);
            if (ec2Launcher == null) {
              ec2Launcher = new Ec2Launcher(clusterContext.getEc2Context(), clusterContext.getSshKeyPair());
            }
            ec2Launcher.createSecurityGroup(definition.getName(), group.getName(), ec2.getRegion(), ports);
            group.setPhase(GroupEntity.GroupPhase.GROUPS_FORKED);
          } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
              InterruptedException ex1 = (InterruptedException) ex;
              throw ex1;
            } else {
              logger.error("", ex);
            }
            group.setFailed(true);
            runtime.setFailed(true);
          }
        }
      }
    }

    if (!runtime.isFailed()) {
      runtime.setPhase(ClusterEntity.ClusterPhases.GROUPS_FORKED);
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' GROUPS_FORKED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
    }
  }

  private synchronized void install() throws Exception {
    logger.info(String.format("Installing '%s' ...", definition.getName()));
    runtime.setPhase(ClusterEntity.ClusterPhases.INSTALLING);
    runtime.setFailed(false);
    List<GroupEntity> groups = runtime.getGroups();
    for (GroupEntity group : groups) {
      group.setPhase(GroupEntity.GroupPhase.INSTALLING);
    }

    try {
      Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsons(definition, runtime);
      installationDag = UserClusterDataExtractor.getInstallationDag(definition, runtime, machinesMonitor, chefJsons, false);

      executor = new MultiThreadedDagExecutor(Settings.INSTALLATION_DAG_THREADPOOL_SIZE);
      executor.submit(installationDag);

    } catch (Exception ex) {
      runtime.setFailed(true);
      throw ex;
    } finally {
      if (executor != null && !executor.isShutdown()) {
        executor.shutdown();
        try {
          executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException ex) {
          logger.warn("Couldn't shutdown the thread-pool of installation DAG", ex);
        }
      }
    }

    if (!runtime.isFailed()) {
      runtime.setPhase(ClusterEntity.ClusterPhases.INSTALLED);
      for (GroupEntity group : groups) {
        group.setPhase(GroupEntity.GroupPhase.INSTALLED);
      }
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' INSTALLED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
    }
  }

  private synchronized void pause() {
    machinesMonitor.pause();
  }

  private synchronized void resume() {
    machinesMonitor.resume();
  }

  private synchronized void purge() throws InterruptedException {
    clean(true);
    stop();
  }

  private void forkMachines() throws Exception {
    logger.info(String.format("Launching '%s' ...", definition.getName()));
    runtime.setPhase(ClusterEntity.ClusterPhases.FORKING_MACHINES);
    runtime.setFailed(false);
    List<GroupEntity> groups = runtime.getGroups();
    for (GroupEntity group : groups) {
      if (group.getPhase() == GroupEntity.GroupPhase.GROUPS_FORKED || (group.getPhase() == GroupEntity.GroupPhase.FORKING_MACHINES && group.isFailed())) {
        group.setPhase(GroupEntity.GroupPhase.FORKING_MACHINES);
        group.setFailed(false);
        Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
        JsonGroup definedGroup = UserClusterDataExtractor.findGroup(definition, group.getName());
        if (provider instanceof Ec2) {
          try {
            Ec2 ec2 = (Ec2) provider;
            HashSet<String> gns = new HashSet<>();
            gns.add(group.getName());
            if (ec2Launcher == null) {
              ec2Launcher = new Ec2Launcher(clusterContext.getEc2Context(), clusterContext.getSshKeyPair());
            }
            String keypairname = Settings.EC2_KEYPAIR_NAME(runtime.getName());
            List<MachineEntity> mcs = ec2Launcher.forkMachines(keypairname, group, gns, Integer.valueOf(definedGroup.getSize()), ec2);
            group.setMachines(mcs);
            machinesMonitor.addMachines(mcs);
            group.setPhase(GroupEntity.GroupPhase.MACHINES_FORKED);
          } catch (Exception ex) {
            group.setFailed(true);
            runtime.setFailed(true);
            throw ex;
          }
        }
      }
    }

    if (!runtime.isFailed()) {
      runtime.setPhase(ClusterEntity.ClusterPhases.MACHINES_FORKED);
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' MACHINES_FORKED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
    }
  }

  @Override
  public void run() {
    logger.info(String.format("Cluster-Manager started for '%s' d'-'", definition.getName()));
    while (true) {
      try {
        Command cmd = cmdQueue.take();
        logger.info(String.format("Going to serve '%s'", cmd.toString()));
        switch (cmd) {
          case LAUNCH:
            if (runtime.getPhase() == ClusterEntity.ClusterPhases.NONE
                    || (runtime.getPhase() == ClusterEntity.ClusterPhases.PRECLEANING && runtime.isFailed())) {
              clean(false);
            }
            if (runtime.getPhase() == ClusterEntity.ClusterPhases.PRECLEANED
                    || (runtime.getPhase() == ClusterEntity.ClusterPhases.FORKING_GROUPS && runtime.isFailed())) {
              forkGroups();
            }
            if (runtime.getPhase() == ClusterEntity.ClusterPhases.GROUPS_FORKED
                    || (runtime.getPhase() == ClusterEntity.ClusterPhases.FORKING_MACHINES && runtime.isFailed())) {
              forkMachines();
            }
            if (runtime.getPhase() == ClusterEntity.ClusterPhases.MACHINES_FORKED
                    || (runtime.getPhase() == ClusterEntity.ClusterPhases.INSTALLING && runtime.isFailed())) {
              install();
            }
            break;
          case PAUSE:
            pause();
            break;
          case RESUME:
            resume();
            break;
          case PURGE:
            purge();
            break;
        }
      } catch (java.lang.InterruptedException ex) {
        if (stoping) {
          tpool.shutdown();
          try {
            tpool.awaitTermination(1, TimeUnit.MINUTES);
          } catch (InterruptedException ex1) {
          }
          logger.info(String.format("Cluster-Manager stoped for '%s' d'-'", definition.getName()));
          return;
        } else {
          logger.error("", ex);
        }
      } catch (Exception ex) {
        logger.error("", ex);
      }
    }
  }

}
