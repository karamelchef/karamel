package se.kth.karamel.backend.autoscalar;

import org.apache.log4j.Logger;
import se.kth.autoscalar.scaling.ScalingSuggestion;
import se.kth.autoscalar.scaling.core.AutoScalarAPI;
import se.kth.autoscalar.scaling.models.MachineType;
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
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class AutoScalingHandler {

  private ThreadPoolExecutor executor;
  private static final Logger logger = Logger.getLogger(AutoScalingHandler.class);
  private static final ClusterService clusterService = ClusterService.getInstance();
  private static AutoScalarAPI autoScalarAPI;
  private Map<String, ArrayBlockingQueue<ScalingSuggestion>> groupSuggestionsQueue =
          new HashMap<String, ArrayBlockingQueue<ScalingSuggestion>>();

  public AutoScalingHandler(int noOfGroupsInCluster, AutoScalarAPI autoScalarAPI) {
    this.autoScalarAPI = autoScalarAPI;
    executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(noOfGroupsInCluster);
  }

  public synchronized void startHandlingGroup(GroupRuntime groupRuntime) {
    executor.execute(new AutoScalingSuggestionExecutor(groupRuntime));
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
        //TODO-AS temporary removing first Id
        vmIds = new String[]{groupRuntime.getMachines().get(0).getId()};

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
