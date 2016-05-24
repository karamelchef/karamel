package se.kth.karamel.backend.tablespoon;

import java.util.ArrayList;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.tasks.DagBuilder;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.tablespoon.client.broadcasting.AgentBroadcaster;
import se.kth.tablespoon.client.broadcasting.BroadcastException;
import se.kth.tablespoon.client.general.Groups;
import se.kth.tablespoon.client.general.Start;

/**
 *
 * @author henke
 */
public class MonitoringService implements AgentBroadcaster {
  
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
    //TODO: ClusterManager is responsible for handling the state of groups.
    Groups groups = new Groups();
    Start.setUp(groups, this, "localhost", 5555);
  }
  
  public void install() throws KaramelException {
    Dag dag = DagBuilder.getInstallMonitoringDag(clusterEntity, clusterStats, mm);
    
  }
  
  public void start() throws KaramelException {
    Dag dag = DagBuilder.getStartMonitoringDag(clusterEntity, clusterStats, mm);
  }
  
  public void topic(ArrayList<String> machines, String json) throws BroadcastException {
    Dag dag;
    try {
      dag =  DagBuilder.getTopicMonitoringDag(clusterEntity, clusterStats, mm, json);
    } catch (KaramelException ex) {
      throw new BroadcastException(ex.getMessage());
    }
  }
  
  @Override
  public void sendToMachines(ArrayList<String> machines, String json)
      throws BroadcastException {
    topic(machines, json);
  }
  
  
  
  
}
