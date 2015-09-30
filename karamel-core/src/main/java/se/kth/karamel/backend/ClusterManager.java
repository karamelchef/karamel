/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import se.kth.karamel.backend.stats.ClusterStatistics;
import se.kth.karamel.backend.launcher.Launcher;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.converter.ChefJsonGenerator;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.kandy.KandyRestClient;
import se.kth.karamel.backend.launcher.amazon.Ec2Launcher;
import se.kth.karamel.backend.launcher.baremetal.BaremetalLauncher;
import se.kth.karamel.backend.launcher.google.GceLauncher;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.Failure;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.tasks.DagBuilder;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.stats.PhaseStat;
import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Gce;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.util.Settings;

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
  private final ClusterRuntime runtime;
  private final MachinesMonitor machinesMonitor;
  private final ClusterStatusMonitor clusterStatusMonitor;
  private Dag installationDag;
  private final BlockingQueue<Command> cmdQueue = new ArrayBlockingQueue<>(1);
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

  public Dag getInstallationDag() {
    return installationDag;
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

  public void enqueue(Command command) throws KaramelException {
    if (command != Command.PAUSE && command != Command.RESUME) {
      if (!cmdQueue.offer(command)) {
        String msg = String.format("Sorry!! have to reject '%s' for '%s', try later (._.)", command,
            definition.getName());
        logger.error(msg);
        throw new KaramelException(msg);
      }
    }

    if (command == Command.PURGE) {
      if (clusterManagerFuture != null && !clusterManagerFuture.isCancelled()) {
        logger.info(String.format("Forcing to stop ClusterManager of '%s'", definition.getName()));
        clusterManagerFuture.cancel(true);
      }
    } else if (command == Command.PAUSE) {
      pause();
    } else if (command == Command.RESUME) {
      resume();
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

  private void initLaunchers() {
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
        }
        launchers.put(provider.getClass(), launcher);
      }
    }
  }

  private void clean(boolean purging) {
    if (!purging) {
      LogService.cleanup(definition.getName());
      logger.info(String.format("Prelaunch Cleaning '%s' ...", definition.getName()));
      runtime.setPhase(ClusterRuntime.ClusterPhases.PRECLEANING);
    }
    runtime.resolveFailures();
    List<GroupRuntime> groups = runtime.getGroups();
    List<GroupRuntime> ec2GroupEntities = new ArrayList<>();
    for (GroupRuntime group : groups) {
      if (purging) {
        group.setPhase(GroupRuntime.GroupPhase.PURGING);
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
        if (purging) {
          group.setMachines(Collections.EMPTY_LIST);
          group.setPhase(GroupRuntime.GroupPhase.NONE);
        } else {
          group.setPhase(GroupRuntime.GroupPhase.PRECLEANED);
        }
      }
    } catch (Exception ex) {
      if (!(ex.getCause() instanceof InterruptedException && stopping)) {
        logger.error("", ex);
        runtime.issueFailure(new Failure(Failure.Type.CLEANUP_FAILE, ex.getMessage()));
      }
    }

    if (!purging && !runtime.isFailed()) {
      runtime.setPhase(ClusterRuntime.ClusterPhases.PRECLEANED);
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' PRECLEANED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
    }
  }

  private void forkGroups() throws InterruptedException {
    logger.info(String.format("Froking groups '%s' ...", definition.getName()));
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

  private void install() throws Exception {
    logger.info(String.format("Installing '%s' ...", definition.getName()));
    runtime.setPhase(ClusterRuntime.ClusterPhases.INSTALLING);
    runtime.resolveFailure(Failure.hash(Failure.Type.INSTALLATION_FAILURE, null));
    List<GroupRuntime> groups = runtime.getGroups();
    for (GroupRuntime group : groups) {
      group.setPhase(GroupRuntime.GroupPhase.INSTALLING);
    }

    try {
      Map<String, JsonObject> chefJsons = ChefJsonGenerator.generateClusterChefJsons(definition, runtime);
      installationDag = DagBuilder.getInstallationDag(definition, runtime, stats, machinesMonitor, chefJsons);
      installationDag.start();
    } catch (Exception ex) {
      runtime.issueFailure(new Failure(Failure.Type.INSTALLATION_FAILURE, ex.getMessage()));
      throw ex;
    }

    while (runtime.getPhase() == ClusterRuntime.ClusterPhases.INSTALLING && !installationDag.isDone()) {
      Thread.sleep(Settings.CLUSTER_STATUS_CHECKING_INTERVAL);
    }

    if (!runtime.isFailed()) {
      runtime.setPhase(ClusterRuntime.ClusterPhases.INSTALLED);
      for (GroupRuntime group : groups) {
        group.setPhase(GroupRuntime.GroupPhase.INSTALLED);
      }
      logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' INSTALLED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
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

  private void purge() throws InterruptedException, KaramelException {
    logger.info(String.format("Purging '%s' ...", definition.getName()));
    runtime.setPhase(ClusterRuntime.ClusterPhases.PURGING);
    stopping = true;
    clean(true);
    stop();
    runtime.setPhase(ClusterRuntime.ClusterPhases.NOT_STARTED);
    KandyRestClient.pushClusterStats(stats);
    logger.info(String.format("\\o/\\o/\\o/\\o/\\o/'%s' PURGED \\o/\\o/\\o/\\o/\\o/", definition.getName()));
  }

  private void forkMachines() throws Exception {
    logger.info(String.format("Launching '%s' ...", definition.getName()));
    runtime.setPhase(ClusterRuntime.ClusterPhases.FORKING_MACHINES);
    runtime.resolveFailure(Failure.hash(Failure.Type.FORK_MACHINE_FAILURE, null));
    List<GroupRuntime> groups = runtime.getGroups();
    for (GroupRuntime group : groups) {
      if (group.getPhase() == GroupRuntime.GroupPhase.GROUPS_FORKED
          || (group.getPhase() == GroupRuntime.GroupPhase.FORKING_MACHINES)) {
        group.setPhase(GroupRuntime.GroupPhase.FORKING_MACHINES);
        runtime.resolveFailure(Failure.hash(Failure.Type.FORK_MACHINE_FAILURE, group.getName()));
        Provider provider = UserClusterDataExtractor.getGroupProvider(definition, group.getName());
        Launcher launcher = launchers.get(provider.getClass());
        try {
          List<MachineRuntime> mcs = launcher.forkMachines(definition, runtime, group.getName());
          group.setMachines(mcs);
          machinesMonitor.addMachines(mcs);
          group.setPhase(GroupRuntime.GroupPhase.MACHINES_FORKED);
        } catch (Exception ex) {
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
          case LAUNCH:
            if (runtime.getPhase() == ClusterRuntime.ClusterPhases.NOT_STARTED
                || (runtime.getPhase() == ClusterRuntime.ClusterPhases.PRECLEANING && runtime.isFailed())) {
              ClusterStatistics.startTimer();
              clean(false);
              long duration = ClusterStatistics.stopTimer();
              PhaseStat phaseStat = new PhaseStat(ClusterRuntime.ClusterPhases.PRECLEANING.name(), "succeed", duration);
              stats.addPhase(phaseStat);
            }
            if (runtime.getPhase() == ClusterRuntime.ClusterPhases.PRECLEANED
                || (runtime.getPhase() == ClusterRuntime.ClusterPhases.FORKING_GROUPS && runtime.isFailed())) {
              ClusterStatistics.startTimer();
              forkGroups();
              long duration = ClusterStatistics.stopTimer();
              PhaseStat phaseStat
                  = new PhaseStat(ClusterRuntime.ClusterPhases.FORKING_GROUPS.name(), "succeed", duration);
              stats.addPhase(phaseStat);
            }
            if (runtime.getPhase() == ClusterRuntime.ClusterPhases.GROUPS_FORKED
                || (runtime.getPhase() == ClusterRuntime.ClusterPhases.FORKING_MACHINES && runtime.isFailed())) {
              ClusterStatistics.startTimer();
              forkMachines();
              long duration = ClusterStatistics.stopTimer();
              PhaseStat phaseStat
                  = new PhaseStat(ClusterRuntime.ClusterPhases.FORKING_MACHINES.name(), "succeed", duration);
              stats.addPhase(phaseStat);
            }
            if (runtime.getPhase() == ClusterRuntime.ClusterPhases.MACHINES_FORKED
                || (runtime.getPhase() == ClusterRuntime.ClusterPhases.INSTALLING && runtime.isFailed())) {
              ClusterStatistics.startTimer();
              install();
              long duration = ClusterStatistics.stopTimer();
              PhaseStat phaseStat = new PhaseStat(ClusterRuntime.ClusterPhases.INSTALLING.name(), "succeed", duration);
              stats.addPhase(phaseStat);
            }
            break;
          case PURGE:
            ClusterStatistics.startTimer();
            purge();
            long duration = ClusterStatistics.stopTimer();
            PhaseStat phaseStat = new PhaseStat(ClusterRuntime.ClusterPhases.PURGING.name(), "succeed", duration);
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
