package se.kth.karamel.backend.autoscalar;

import org.apache.log4j.Logger;
import se.kth.autoscalar.scaling.ScalingSuggestion;
import se.kth.autoscalar.scaling.models.MachineType;
import se.kth.karamel.backend.running.model.GroupRuntime;

import java.util.ArrayList;
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

  public AutoScalingHandler(int noOfGroupsInCluster) {
    executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(noOfGroupsInCluster);
  }

  public synchronized void startHandlingGroup(GroupRuntime groupRuntime) {
    executor.execute(new AutoScalingSuggestionExecutor(groupRuntime));
  }

  class AutoScalingSuggestionExecutor implements Runnable {

    private GroupRuntime groupRuntime;

    public AutoScalingSuggestionExecutor(GroupRuntime groupRuntime) {
      this.groupRuntime = groupRuntime;
    }

    @Override
    public void run() {
      while (true) {
        //wait on queue, get suggestion and execute suggestion
        try {
          ScalingSuggestion suggestion = groupRuntime.getAutoScalingSuggestionsQueue().take();
          switch (suggestion.getScalingDirection()) {
            case SCALE_IN:
              ArrayList<String> machinesToRemove = suggestion.getScaleInSuggestions();
              handleScaleInSuggestion(machinesToRemove.toArray(new String[machinesToRemove.size()]));
              break;
            case SCALE_OUT:
              //
              ArrayList<MachineType> scaleOutMachines = suggestion.getScaleOutSuggestions();
              handleScaleOutSuggestion(scaleOutMachines.toArray(new MachineType[scaleOutMachines.size()]));
              break;
            default:
              logger.warn("Handle scaling has not been emplemented for the scaling direction: " +
                      suggestion.getScalingDirection().name());
          }
        } catch (InterruptedException e) {
          logger.error("Error while taking the auto-scaling suggestion in group: " + groupRuntime.getId());
        }
      }
    }

    private void handleScaleInSuggestion(String[] vmIds) {

    }

    private void handleScaleOutSuggestion(MachineType[] machineTypes) {

    }

  }
}
