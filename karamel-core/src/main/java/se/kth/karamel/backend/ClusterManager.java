/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ChefJsonGenerator;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.kandy.KandyRestClient;
import se.kth.karamel.backend.launcher.Launcher;
import se.kth.karamel.backend.launcher.amazon.Ec2Launcher;
import se.kth.karamel.backend.launcher.baremetal.BaremetalLauncher;
import se.kth.karamel.backend.launcher.google.GceLauncher;
import se.kth.karamel.backend.launcher.nova.NovaLauncher;
import se.kth.karamel.backend.launcher.novav3.NovaV3Launcher;
import se.kth.karamel.backend.launcher.occi.OcciLauncher;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.Failure;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.tasks.DagBuilder;
import se.kth.karamel.backend.stats.ClusterStatistics;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.Nova;
import se.kth.karamel.common.clusterdef.Occi;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.stats.PhaseStat;
import se.kth.karamel.common.util.Settings;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author kamal
 */
public class ClusterManager implements Runnable {

  public static enum Command {

    LAUNCH_CLUSTER, INTERRUPT_CLUSTER, TERMINATE_CLUSTER,
    SUBMIT_INSTALL_DAG, SUBMIT_PURGE_DAG, INTERRUPT_DAG, PAUSE_DAG, RESUME_DAG;

  }

  public static boolean EXIT_ON_COMPLETION = false;
  private static final Logger logger = Logger.getLogger(ClusterManager.class);
  private final JsonCluster definition;
  private final ClusterRuntime runtime;
  private final MachinesMonitor machinesMonitor;
  private final ClusterStatusMonitor clusterStatusMonitor;
  private Dag currentDag;
  private final BlockingQueue<Command> cmdQueue = new ArrayBlockingQueue<>(2);
  ExecutorService tpool;
  private final ClusterContext clusterContext;
  private Map<Class, Launcher> launchers = new HashMap<>();
  private Future<?> clusterManagerFuture = null;
  private Future<?> machinesMonitorFuture = null;
  private Future<?> clusterStatusFuture = null;
  private boolean stopping = false;
  private final ClusterStats stats = new ClusterStats();

  public ClusterManager(JsonCluster definition, ClusterContext clusterContext) throws KaramelException {
    this.clusterContext = clusterContext;
    this.definition = definition;
    this.runtime = new ClusterRuntime(definition);
    int totalMachines = UserClusterDataExtractor.totalMachines(definition);
    machinesMonitor = new MachinesMonitor(definition.getName(), totalMachines, clusterContext.getSshKeyPair());
    String yaml = ClusterDefinitionService.jsonToYaml(definition);
    this.stats.setDefinition(yaml);
    this.stats.setUserId(Settings.USER_NAME);
    this.stats.setStartTime(System.currentTimeMillis());
    clusterStatusMonitor = new ClusterStatusMonitor(machinesMonitor, definition, runtime, stats);
    initLaunchers();
  }

  public ClusterStats getStats() {
    return stats;
  }

  public Dag getCurrentDag() {
    return currentDag;
  }

  public MachinesMonitor getMachinesMonitor() {
    return machinesMonitor;
  }

  public JsonCluster getDefinition() {
    return definition;
  }

  public ClusterRuntime getRuntime() {
    return runtime;
  }

  /**
   * Non-blocking way of controlling the cluster, the quick commands are served immediately while the time-consuming
   * commands are queued to be served one by one. Commands have different level of priorities and the higher priority
   * commands invalidated the lower-priority ones.
   *
   * Cluster-scope immediate: 
   *   - INTERRUPT_CLUSTER 
   * Cluster-scope long-running: 
   *   - LAUNCH_CLUSTER 
   *   - INTERRUPT_CLUSTER
   * DAG-scope immediate: 
   *   - INTERRUPT_DAG 
   *   - PAUSE_DAG 
   *   - RESUME_DAG 
   * DAG-scope long-running: 
   *   - SUBMIT_INSTALL_DAG 
   *   - SUBMIT_PURGE_DAG
   *
   * @param command
   * @throws KaramelException
   */
  public void enqueue(Command command) throws KaramelException {
    ArrayList<Command> clusterScopeQueuingCommands = Lists.newArrayList(
        Command.LAUNCH_CLUSTER,
        Command.TERMINATE_CLUSTER);

    ArrayList<Command> dagScopeQueuingCommands = Lists.newArrayList(
        Command.SUBMIT_INSTALL_DAG,
        Command.SUBMIT_PURGE_DAG);

    if (clusterScopeQueuingCommands.contains(command)) {
      cmdQueue.removeAll(dagScopeQueuingCommands);
    }

    switch (command) {
      case LAUNCH_CLUSTER:
        runtime.resolveFailures();
        cmdQueue.offer(command);
        break;
      case INTERRUPT_CLUSTER:
        interupt();
        break;
      case TERMINATE_CLUSTER:
        runtime.resolveFailures();
        cmdQueue.offer(command);
        break;
      case INTERRUPT_DAG:
        cmdQueue.remove(Command.SUBMIT_INSTALL_DAG);
        cmdQueue.remove(Command.SUBMIT_PURGE_DAG);
        cmdQueue.remove(Command.PAUSE_DAG);
        cmdQueue.remove(Command.RESUME_DAG);
        if (runtime.getPhase() == ClusterRuntime.ClusterPhases.RUNNING_DAG) {
          interupt();
        }
        break;
      case SUBMIT_INSTALL_DAG:
        cmdQueue.offer(command);
        break;
      case SUBMIT_PURGE_DAG:
        cmdQueue.offer(command);
        break;
      case PAUSE_DAG:
        pause();
        break;
      case RESUME_DAG:
        resume();
        break;
    }
  }

  public void interupt() {
    if (clusterManagerFuture != null && !clusterManagerFuture.isCancelled()) {
      logger.info(String.format("Forcing to interrupt ClusterManager of '%s'", definition.getName()));
      clusterManagerFuture.cancel(true);
    }
  }

  public void start() {
    tpool = Executors.newFixedThreadPool(3);
    clusterManagerFuture = tpool.submit(this);
    machinesMonitorFuture = tpool.submit(machinesMonitor);
    clusterStatusFuture = tpool.submit(clusterStatusMonitor);
  }

  public void stop() throws InterruptedException {
    machinesMonitor.setStopping(true);
    if (machinesMonitorFuture != null && !machinesMonitorFuture.isCancelled()) {
      logger.info(String.format("Terminating machines monitor of '%s'", definition.getName()));
      machinesMonitorFuture.cancel(true);
    }
    clusterStatusMonitor.setStopping(true);
    if (clusterStatusFuture != null && !clusterStatusFuture.isCancelled()) {
      logger.info(String.format("Terminating cluster status monitor of '%s'", definition.getName()));
      clusterStatusFuture.cancel(true);
    }
    if (clusterManagerFuture != null && !clusterManagerFuture.isCancelled()) {
      logger.info(String.format("Terminating cluster manager of '%s'", definition.getName()));
      clusterManagerFuture.cancel(true);
    }
  }

  private void initLaunchers() throws KaramelException {
    for (JsonGroup group : definition.getGroups()) {
      Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      Launcher launcher = launchers.get(provider.getClass());
      if (launcher == null) {
        if (provider instanceof Ec2) {
          launcher = new Ec2Launcher(clusterContext.getEc2Context(), clusterContext.getSshKeyPair());
        } else if (provider instanceof Baremetal) {
          launcher = new BaremetalLauncher(clusterContext.getSshKeyPair());
        } else if (provider instanceof Gce) {
          launcher = new GceLauncher(clusterContext.getGceContext(), clusterContext.getSshKeyPair());
        } else if (provider instanceof Nova) {
          if (clusterContext.getNovaContext() != null) {
            launcher = new NovaLauncher(clusterContext.getNovaContext(), clusterContext.getSshKeyPair());
          } else if (clusterContext.getNovaV3Context() != null) {
            launcher = new NovaV3Launcher(clusterContext.getNovaV3Context(), clusterContext.getSshKeyPair());
          }
        } else if (provider instanceof Occi) {
          launcher = new OcciLauncher(clusterContext.getOcciContext(), clusterContext.getSshKeyPair());
        }
        launchers.put(provider.getClass(), launcher);
      }
    }
  }

  private void clean(boolean terminating) {
    if (!terminating) {
      LogService.cleanup(definition.getName());
      logger.info(String.format("Prelaunch Cleaning '%s' ...", definition.getName()));
      runtime.setPhase(ClusterRuntime.ClusterPhases.PRECLEANING);
    }
    runtime.resolveFailures();
    List<GroupRuntime> groups = runtime.getGroups();
    List<GroupRuntime> ec2GroupEntities = new ArrayList<>();
    for (GroupRuntime group : groups) {
      if (terminating) {
        group.setPhase(GroupRuntime.GroupPhase.TERMINATING);
      } else {
        group.setPhase(GroupRuntime.GroupPhase.PRECLEANING);
      }
      group.getCluster().resolveFailures();
      Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
      ec2GroupEntities.add(group);
    }
    try {
      for (Map.Entry<Class, Launcher> entry : launchers.entrySet()) {
        Launcher launcher = entry.getValue();
        launcher.cleanup(definition, runtime);
      }
      for (GroupRuntime group : ec2GroupEntities) {
        if (terminating) {
          group.setMachines(Collections.EMPTY_LIST);
          group.setPhase(GroupRuntime.GroupPhase.NONE);
        } else {
          group.setPhase(GroupRuntime.GroupPhase.PRECLEANED);
        }
      }
    } catch (Exception ex) {
      logger.info("Cleanup error", ex);
      if (!(ex.getCause() instanceof InterruptedException && stopping)) {
        logger.error("", ex);
        runtime.issueFailure(new Failure(Failure.Type.CLEANUP_FAILE, ex.getMessage()));
      }
    }

    if (!terminating && !runtime.isFailed()) {
      runtime.setPhase(ClusterRuntime.ClusterPhases.PRECLEANED);
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' PRECLEANED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
    }
  }

  private void forkGroups() throws InterruptedException {
    logger.info(String.format("Forking groups '%s' ...", definition.getName()));
    runtime.setPhase(ClusterRuntime.ClusterPhases.FORKING_GROUPS);
    runtime.resolveFailures();
    List<GroupRuntime> groups = runtime.getGroups();
    for (GroupRuntime group : groups) {
      if (group.getPhase() == GroupRuntime.GroupPhase.PRECLEANED
          || (group.getPhase() == GroupRuntime.GroupPhase.FORKING_GROUPS)) {
        runtime.resolveFailure(Failure.hash(Failure.Type.CREATING_SEC_GROUPS_FAILE, group.getName()));
        group.setPhase(GroupRuntime.GroupPhase.FORKING_GROUPS);
        Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
        Launcher launcher = launchers.get(provider.getClass());
        try {
          String groupId = launcher.forkGroup(definition, runtime, group.getName());
          group.setId(groupId);
          group.setPhase(GroupRuntime.GroupPhase.GROUPS_FORKED);
        } catch (Exception ex) {
          logger.info("Fork groups ", ex);
          if (ex instanceof InterruptedException) {
            InterruptedException ex1 = (InterruptedException) ex;
            throw ex1;
          } else {
            logger.error("", ex);
          }
          runtime.issueFailure(new Failure(Failure.Type.CREATING_SEC_GROUPS_FAILE, group.getName(), ex.getMessage()));
        }
      }
    }

    if (!runtime.isFailed()) {
      runtime.setPhase(ClusterRuntime.ClusterPhases.GROUPS_FORKED);
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' GROUPS_FORKED \\o/\\o/\\o/\\o/\\o/",
          definition.getName()));
    }
  }

  private void runDag(boolean installDag) throws Exception {
    logger.info(String.format("Running the DAG for '%s' ...", definition.getName()));
    if (currentDag != null) {
      logger.info(String.format("Terminating the previous DAG before running the new one for '%s' ...",
          definition.getName()));
      currentDag.termiante();
    }
    runtime.setPhase(ClusterRuntime.ClusterPhases.RUNNING_DAG);
    runtime.resolveFailure(Failure.hash(Failure.Type.DAG_FAILURE, null));
    List<GroupRuntime> groups = runtime.getGroups();
    for (GroupRuntime group : groups) {
      group.setPhase(GroupRuntime.GroupPhase.RUNNING_DAG);
    }

    try {
      if (installDag) {
        Map<String, JsonObject> chefJsons = ChefJsonGenerator.
            generateClusterChefJsonsForInstallation(definition, runtime);
        currentDag = DagBuilder.getInstallationDag(definition, runtime, stats, machinesMonitor, chefJsons);
      } else {
        Map<String, JsonObject> chefJsons = ChefJsonGenerator.
            generateClusterChefJsonsForPurge(definition, runtime);
        currentDag = DagBuilder.getPurgingDag(definition, runtime, stats, machinesMonitor, chefJsons);
      }
      currentDag.start(definition);
    } catch (Exception ex) {
      runtime.issueFailure(new Failure(Failure.Type.DAG_FAILURE, ex.getMessage()));
      throw ex;
    }

    while (runtime.getPhase() == ClusterRuntime.ClusterPhases.RUNNING_DAG && !currentDag.isDone()) {
      Thread.sleep(Settings.CLUSTER_STATUS_CHECKING_INTERVAL);
    }

    if (!runtime.isFailed()) {
      runtime.setPhase(ClusterRuntime.ClusterPhases.DAG_DONE);
      for (GroupRuntime group : groups) {
        group.setPhase(GroupRuntime.GroupPhase.DAG_DONE);
      }
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' DAG IS DONE \\o/\\o/\\o/\\o/\\o/", definition.getName()));
      
      if (ClusterManager.EXIT_ON_COMPLETION) {
        Duration cooldown = Duration.ofMinutes(2);
        logger.info(String.format("Cooling down for %d minutes before exiting", cooldown.toMinutes()));
        TimeUnit.MINUTES.sleep(cooldown.toMinutes());
        System.exit(0);
      }
    }
  }

  private void pause() {
    logger.info(String.format("Pausing '%s'", definition.getName()));
    machinesMonitor.pause();
    runtime.setPaused(true);
    logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' PAUSED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
  }

  private void resume() {
    logger.info(String.format("Resuming '%s'", definition.getName()));
    machinesMonitor.resume();
    runtime.setPaused(false);
    logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' RESUMED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
  }

  private void terminate() throws InterruptedException, KaramelException {
    logger.info(String.format("Terminating '%s' ...", definition.getName()));
    runtime.setPhase(ClusterRuntime.ClusterPhases.TERMINATING);
    stopping = true;
    clean(true);
    stop();
    runtime.setPhase(ClusterRuntime.ClusterPhases.NOT_STARTED);
    KandyRestClient.pushClusterStats(definition.getName(), stats);
    logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' TERMINATED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
  }

  private void forkMachines() throws Exception {
    logger.info(String.format("Launching '%s' ...", definition.getName()));
    runtime.setPhase(ClusterRuntime.ClusterPhases.FORKING_MACHINES);
    runtime.resolveFailure(Failure.hash(Failure.Type.FORK_MACHINE_FAILURE, null));
    List<GroupRuntime> groups = runtime.getGroups();
    

    logger.info(String.format("groups '%s'", groups));

    for (GroupRuntime group : groups) {
      if (group.getPhase() == GroupRuntime.GroupPhase.GROUPS_FORKED
          || (group.getPhase() == GroupRuntime.GroupPhase.FORKING_MACHINES)) {
        group.setPhase(GroupRuntime.GroupPhase.FORKING_MACHINES);
        runtime.resolveFailure(Failure.hash(Failure.Type.FORK_MACHINE_FAILURE, group.getName()));
        logger.info(String.format("Gogo"));
        Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
        logger.info(String.format("Using provider '%s'", provider));
        Launcher launcher = launchers.get(provider.getClass());
        logger.info(String.format("Using launcher '%s'", launcher));
        try {
          logger.info(String.format("Using launcher '%s'", launcher));
          List<MachineRuntime> mcs = launcher.forkMachines(definition, runtime, group.getName());
          group.setMachines(mcs);
          machinesMonitor.addMachines(mcs);
          group.setPhase(GroupRuntime.GroupPhase.MACHINES_FORKED);
        } catch (Exception ex) {
          logger.error("Fork error", ex);
          runtime.issueFailure(new Failure(Failure.Type.FORK_MACHINE_FAILURE, group.getName(), ex.getMessage()));
          throw ex;
        }
      }
    }

    if (!runtime.isFailed()) {
      runtime.setPhase(ClusterRuntime.ClusterPhases.MACHINES_FORKED);
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
          case LAUNCH_CLUSTER:
            if (runtime.getPhase() == ClusterRuntime.ClusterPhases.NOT_STARTED
                || (runtime.getPhase() == ClusterRuntime.ClusterPhases.PRECLEANING && runtime.isFailed())) {
              ClusterStatistics.startTimer();
              clean(false);
              long duration = ClusterStatistics.stopTimer();
              String status = runtime.isFailed() ? "FAILED" : "SUCCEED";
              PhaseStat phaseStat = new PhaseStat(ClusterRuntime.ClusterPhases.PRECLEANING.name(), status, duration);
              stats.addPhase(phaseStat);
            }
            if (runtime.getPhase() == ClusterRuntime.ClusterPhases.PRECLEANED
                || (runtime.getPhase() == ClusterRuntime.ClusterPhases.FORKING_GROUPS && runtime.isFailed())) {
              ClusterStatistics.startTimer();
              forkGroups();
              long duration = ClusterStatistics.stopTimer();
              String status = runtime.isFailed() ? "FAILED" : "SUCCEED";
              PhaseStat phaseStat
                  = new PhaseStat(ClusterRuntime.ClusterPhases.FORKING_GROUPS.name(), status, duration);
              stats.addPhase(phaseStat);
            }
            if (runtime.getPhase() == ClusterRuntime.ClusterPhases.GROUPS_FORKED
                || (runtime.getPhase() == ClusterRuntime.ClusterPhases.FORKING_MACHINES && runtime.isFailed())) {
              ClusterStatistics.startTimer();
              forkMachines();
              long duration = ClusterStatistics.stopTimer();
              String status = runtime.isFailed() ? "FAILED" : "SUCCEED";
              PhaseStat phaseStat
                  = new PhaseStat(ClusterRuntime.ClusterPhases.FORKING_MACHINES.name(), status, duration);
              stats.addPhase(phaseStat);
            }
            break;
          case SUBMIT_INSTALL_DAG:
            if (runtime.getPhase().ordinal() >= ClusterRuntime.ClusterPhases.MACHINES_FORKED.ordinal()
                && (runtime.getPhase().ordinal() <= ClusterRuntime.ClusterPhases.DAG_DONE.ordinal())) {
              ClusterStatistics.startTimer();
              runDag(true);
              long duration = ClusterStatistics.stopTimer();
              String status = runtime.isFailed() ? "FAILED" : "SUCCEED";
              PhaseStat phaseStat = new PhaseStat(ClusterRuntime.ClusterPhases.RUNNING_DAG.name(), status, duration);
              stats.addPhase(phaseStat);
            }
            break;
          case SUBMIT_PURGE_DAG:
            if (runtime.getPhase().ordinal() >= ClusterRuntime.ClusterPhases.MACHINES_FORKED.ordinal()
                && (runtime.getPhase().ordinal() <= ClusterRuntime.ClusterPhases.DAG_DONE.ordinal())) {
              ClusterStatistics.startTimer();
              runDag(false);
              long duration = ClusterStatistics.stopTimer();
              String status = runtime.isFailed() ? "FAILED" : "SUCCEED";
              PhaseStat phaseStat = new PhaseStat(ClusterRuntime.ClusterPhases.RUNNING_DAG.name(), status, duration);
              stats.addPhase(phaseStat);
            }
            break;
          case TERMINATE_CLUSTER:
            ClusterStatistics.startTimer();
            terminate();
            long duration = ClusterStatistics.stopTimer();
            String status = runtime.isFailed() ? "FAILED" : "SUCCEED";
            PhaseStat phaseStat = new PhaseStat(ClusterRuntime.ClusterPhases.TERMINATING.name(), status, duration);
            stats.addPhase(phaseStat);
            break;
        }
      } catch (java.lang.InterruptedException ex) {
        if (stopping) {
          tpool.shutdownNow();
          try {
            tpool.awaitTermination(1, TimeUnit.MINUTES);
          } catch (InterruptedException ex1) {
          }
          logger.info(String.format("Cluster-Manager stoped for '%s' d'-'", definition.getName()));
          return;
        } else {
          logger.warn("Got interrupted, perhaps a higher priority command is comming on..");
        }
      } catch (Exception ex) {
        logger.error("", ex);
      }
    }
  }

}
