/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package se.kth.karamel.backend.tablespoon;

import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.tasks.DagBuilder;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;

/**
 *
 * @author henke
 */
public class MonitoringService {
  
  private final JsonCluster cluster;
  private final ClusterRuntime clusterEntity;
  private final ClusterStats clusterStats;
  private final MachinesMonitor mm;
  
  public MonitoringService(JsonCluster cluster, ClusterRuntime clusterEntity, ClusterStats clusterStats,
      MachinesMonitor mm) {
    this.cluster = cluster;
    this.clusterEntity = clusterEntity;
    this.clusterStats = clusterStats;
    this.mm = mm;
  }
  
  public void install() throws KaramelException {
    DagBuilder.getInstallMonitoringDag(clusterEntity, clusterStats, mm);
  }
  
  public void start() throws KaramelException {
    Dag dag = DagBuilder.getStartMonitoringDag(clusterEntity, clusterStats, mm);
  }
  
  
  
  
}
