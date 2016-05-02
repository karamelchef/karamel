/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package se.kth.karamel.backend.running.model.tasks;


import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.stats.ClusterStats;

/**
 *
 * @author henke
 */
public abstract class TablespoonTask extends Task {
  
  private static final Logger logger = Logger.getLogger(TablespoonTask.class);
  
  public TablespoonTask(String name, String id, boolean idempotent, MachineRuntime machine, ClusterStats clusterStats,
      TaskSubmitter submitter) {
    super(name, id, idempotent, machine, clusterStats, submitter);
  }
  
  @Override
  public Set<String> dagDependencies() {
    Set<String> deps = new HashSet<>();
    String tablespoonAgent = InstallTablespoonAgent.makeUniqueId(getMachineId());
    deps.add(tablespoonAgent);
    return deps;
  }
}

