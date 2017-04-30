/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import org.apache.log4j.Logger;
import se.kth.karamel.backend.kandy.KandyRestClient;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.util.Settings;

/**
 * While cluster is running, it observes its status, should failure happen it pauses MachinesMonitor.
 *
 * @author kamal
 */
public class ClusterStatusMonitor implements Runnable {

  private static final Logger logger = Logger.getLogger(ClusterStatusMonitor.class);
  private final JsonCluster definition;
  private final MachinesMonitor machinesMonitor;
  private final ClusterRuntime clusterEntity;
  private final ClusterStats stats;
  private boolean stopping = false;
  private long lastStatsReport = 0;

  public ClusterStatusMonitor(MachinesMonitor machinesMonitor, JsonCluster definition, ClusterRuntime runtime,
      ClusterStats stats) {
    this.definition = definition;
    this.machinesMonitor = machinesMonitor;
    this.clusterEntity = runtime;
    this.stats = stats;
  }

  public void setStopping(boolean stopping) {
    this.stopping = stopping;
  }

  @Override
  public void run() {
    logger.debug(String.format("Cluster-StatusMonitor started for '%s' d'-'", definition.getName()));
    while (true && !stopping) {
      try {
        if (clusterEntity.isFailed()) {
          machinesMonitor.pause();
          clusterEntity.setPaused(true);
        } else {
          machinesMonitor.resume();
          clusterEntity.setPaused(false);
        }
        try {
          long lastReportInterval = System.currentTimeMillis() - lastStatsReport;
          if (lastReportInterval > Settings.CLUSTER_STAT_REPORT_INTERVAL && stats.isUpdated()) {
            KandyRestClient.pushClusterStats(definition.getName(), stats);
            lastStatsReport = System.currentTimeMillis();
          }
          Thread.sleep(Settings.CLUSTER_FAILURE_DETECTION_INTERVAL);
        } catch (InterruptedException ex) {
          if (stopping) {
            logger.info(String.format("Cluster-StatusMonitor stopped for '%s' d'-'", definition.getName()));
            return;
          } else {
            String message = String.format("ClusterMonitor for '%s' interrupted while it hasn't received stopping "
                + "signal yet", definition.getName());
            logger.error(message, ex);
          }

        }
      } catch (Exception ex) {
        logger.error("", ex);
      }
    }
  }

}
