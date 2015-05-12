/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend;

import org.apache.log4j.Logger;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.common.Settings;

/**
 *
 * @author kamal
 */
public class ClusterStatusMonitor implements Runnable {
  
  private static final Logger logger = Logger.getLogger(ClusterStatusMonitor.class);
  private final JsonCluster definition;
  private final MachinesMonitor machinesMonitor;
  private final ClusterRuntime clusterEntity;
  private boolean stopping = false;
  
  public ClusterStatusMonitor(MachinesMonitor machinesMonitor, JsonCluster definition, ClusterRuntime runtime) {
    this.definition = definition;
    this.machinesMonitor = machinesMonitor;
    this.clusterEntity = runtime;
  }
  
  public void setStoping(boolean stopping) {
    this.stopping = stopping;
  }
  
  @Override
  public void run() {
    logger.info(String.format("Cluster-StatusMonitor started for '%s' d'-'", definition.getName()));
    while (true && !stopping) {
      try {
        if (clusterEntity.isFailed()) {
          machinesMonitor.pause();
        }
        try {
          Thread.sleep(Settings.CLUSTER_FAILURE_DETECTION_INTERVAL);
        } catch (InterruptedException ex) {
          if (stopping) {
            logger.info(String.format("Cluster-StatusMonitor stoped for '%s' d'-'", definition.getName()));
            return;
          } else {
            String message = String.format("ClusterMonitor for '%s' interrupted while it hasn't received stopping signale yet", definition.getName());
            logger.error(message, ex);
          }
          
        }
      } catch (Exception ex) {
        logger.error("", ex);
      }
    }
  }
  
}
