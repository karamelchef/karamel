package se.kth.karamel.backend.honeytap;

import org.apache.log4j.Logger;
import se.kth.honeytap.scaling.ScalingSuggestion;
import se.kth.honeytap.scaling.core.HoneyTapAPI;
import se.kth.honeytap.scaling.models.MachineType;
//import se.kth.honeytap.scaling.rules.Rule;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.common.exception.KaramelException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created with IntelliJ IDEA.
 * Each cluster will be handled by one HoneyTapHandler
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class HoneyTapHandler {

  private ThreadPoolExecutor executor;
  private static final Logger logger = Logger.getLogger(HoneyTapHandler.class);
  private static final ClusterService clusterService = ClusterService.getInstance();
  private static HoneyTapAPI autoScalarAPI;
  private Map<String, AutoScalingSuggestionExecutor> groupExecutorMap =
          new HashMap<String, AutoScalingSuggestionExecutor>();
  private boolean isAutoScalingActive = false;

  public HoneyTapHandler(int noOfGroupsInCluster, HoneyTapAPI autoScalarAPI) {
    this.autoScalarAPI = autoScalarAPI;
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(noOfGroupsInCluster);
    this.isAutoScalingActive = true;
  }

  public synchronized void startHandlingGroup(GroupRuntime groupRuntime) {
    if (isAutoScalingActive) {
      AutoScalingSuggestionExecutor suggestionExecutor = new AutoScalingSuggestionExecutor(groupRuntime);
      groupExecutorMap.put(groupRuntime.getId(), suggestionExecutor);
      executor.execute(suggestionExecutor);
    } else {
      logger.error("Cannot start handling auto-scaling in group. Auto-scaling is set to " + isAutoScalingActive);
    }
  }

  public synchronized void stopHandlingGroup(String groupId) {
    AutoScalingSuggestionExecutor suggestionExecutor = groupExecutorMap.get(groupId);
    groupExecutorMap.remove(groupId);
    suggestionExecutor.stopAutoScalingSuggestionExecution();
  }

  public void stopHandlingCluster() {
    isAutoScalingActive = false;
    for (Map.Entry<String, AutoScalingSuggestionExecutor> executorEntry : groupExecutorMap.entrySet()) {
      groupExecutorMap.remove(executorEntry.getKey());
      executorEntry.getValue().stopAutoScalingSuggestionExecution();
    }
    executor.shutdown();  //already submitted tasked will be completed before shutting down
  }

  class AutoScalingSuggestionExecutor implements Runnable {

    private GroupRuntime groupRuntime;
    private boolean shouldAutoScale = false;
    private ArrayBlockingQueue<ScalingSuggestion> suggestionsQueueOfGroup = null;

    public AutoScalingSuggestionExecutor(GroupRuntime groupRuntime) {
      this.groupRuntime = groupRuntime;
      this.shouldAutoScale = true;

      while (this.suggestionsQueueOfGroup == null) {
        ArrayBlockingQueue<ScalingSuggestion> suggestionQueue = autoScalarAPI.getSuggestionQueue(groupRuntime.getId());
        if (suggestionQueue != null) {
          this.suggestionsQueueOfGroup = suggestionQueue;
          logger.info(" ############### AS started, group: " + groupRuntime.getId() + "#################");
          break;
        }
      }
    }

    @Override
    public void run() {
      while (shouldAutoScale) {
        //wait on queue, get suggestion and execute suggestion
        try {
          ScalingSuggestion suggestion = suggestionsQueueOfGroup.take();
          logger.info("########################## got suggestion: " + groupRuntime.getName() + " " +
                  suggestion.getScalingDirection().name() + " ######################################");
          switch (suggestion.getScalingDirection()) {
            case SCALE_IN:
              ArrayList<String> machinesToRemove = suggestion.getScaleInSuggestions();
              handleScaleInSuggestion(machinesToRemove.toArray(new String[machinesToRemove.size()]));
              break;
            case SCALE_OUT:
              ArrayList<MachineType> scaleOutMachines = suggestion.getScaleOutSuggestions();
              handleScaleOutSuggestion(scaleOutMachines.toArray(new MachineType[scaleOutMachines.size()]));
              break;
            case TMP_SCALEIN:
              ArrayList<String> toRemove = suggestion.getScaleInSuggestions();
              handleScaleInSuggestion(toRemove.toArray(new String[toRemove.size()]));
              break;
            default:
              logger.warn("Handle scaling has not been implemented for the scaling direction: " +
                      suggestion.getScalingDirection().name());
              break;
          }
        } catch (InterruptedException e) {
          logger.error("Error while taking the auto-scaling suggestion in group: " + groupRuntime.getId());
        }
      }
    }

    public void stopAutoScalingSuggestionExecution() {
      this.shouldAutoScale = false;
    }

    private void handleScaleInSuggestion(String[] vmIds) {
      try {
        //TODO-AS temporary removing first Id -remove passing ids
        vmIds = new String[]{groupRuntime.getMachines().get(0).getVmId()};

        clusterService.scaleInClusterGroup(groupRuntime.getCluster().getName(), groupRuntime.getName(), vmIds);
      } catch (KaramelException e) {
        logger.error("Failed to scale in the group: " + groupRuntime.getName() + " of cluster: " +
                groupRuntime.getCluster().getName());
      }
    }

    private void handleScaleOutSuggestion(MachineType[] machineTypes) {
      try {
        clusterService.scaleOutClusterGroup(groupRuntime.getCluster().getName(), groupRuntime.getName(), machineTypes);
      } catch (KaramelException e) {
        logger.error("Failed to scale in the group: " + groupRuntime.getName() + " of cluster: " +
                groupRuntime.getCluster().getName());
      }
    }

  }
}
