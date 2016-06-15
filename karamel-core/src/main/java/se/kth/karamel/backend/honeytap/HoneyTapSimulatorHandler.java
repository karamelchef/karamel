package se.kth.karamel.backend.honeytap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kth.honeytap.scaling.ScalingSuggestion;
import se.kth.honeytap.scaling.core.HoneyTapAPI;
import se.kth.honeytap.scaling.models.MachineType;
import se.kth.honeytap.stat.StatManager;
import se.kth.karamel.backend.ClusterService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import se.kth.karamel.common.launcher.aws.InstanceType;

//import se.kth.honeytap.scaling.rules.Rule;
/**
 * Created with IntelliJ IDEA. Each cluster will be handled by one HoneyTapHandler
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class HoneyTapSimulatorHandler {

  private ThreadPoolExecutor executor;
  Log log = LogFactory.getLog(HoneyTapSimulatorHandler.class);
  private static final ClusterService clusterService = ClusterService.getInstance();
  private HoneyTapAPI honeyTapAPI;
  private Map<String, AutoScalingSuggestionExecutor> groupExecutorMap
      = new HashMap<String, AutoScalingSuggestionExecutor>();
  private boolean isAutoScalingActive = false;

  //for simulation
  boolean isSimulation = true;

  public HoneyTapSimulatorHandler(int noOfGroupsInCluster, HoneyTapAPI honeyTapAPI) {
    this.honeyTapAPI = honeyTapAPI;
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(noOfGroupsInCluster);
    this.isAutoScalingActive = true;
  }

  public synchronized void startHandlingGroup(String groupId) {
    if (isAutoScalingActive) {
      AutoScalingSuggestionExecutor suggestionExecutor = new AutoScalingSuggestionExecutor(groupId);
      groupExecutorMap.put(groupId, suggestionExecutor);
      executor.execute(suggestionExecutor);
    } else {
      log.error("Cannot start handling auto-scaling in group. Auto-scaling is set to " + isAutoScalingActive);
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

    String groupId;
    private boolean shouldAutoScale = false;
    private ArrayBlockingQueue<ScalingSuggestion> suggestionsQueueOfGroup = null;

    public AutoScalingSuggestionExecutor(String groupId) {
      this.shouldAutoScale = true;
      this.groupId = groupId;
      while (this.suggestionsQueueOfGroup == null) {
        ArrayBlockingQueue<ScalingSuggestion> suggestionQueue = honeyTapAPI.getSuggestionQueue(groupId);
        if (suggestionQueue != null) {
          this.suggestionsQueueOfGroup = suggestionQueue;
          log.info(" ############### AS suggestion queue recieved, group: " + groupId
              + "#################");
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
          //TODO-AS this is temporary code for simulation:isSimulation. After that only the logic
          // in else part should be there
          if (isSimulation) {
            log.info("##################### SIMULATION scaling suggestions #############");
            switch (suggestion.getScalingDirection()) {
              case SCALE_IN:
                ////resetVmInfoAtMonitor(groupRuntime.getId());  //setting actual running vms
                //remove above line in all cases only if we can start without spawining machines????
                ArrayList<String> machinesToRemove = suggestion.getScaleInSuggestions();
                Thread.sleep(new Random().nextInt(10 * 1000) + 5 * 1000);  // delay upto 5 - 15 seconds
                for (String machineId : machinesToRemove) {
                  removeVmIdfromMonitorSimulation(groupId, machineId);
                  StatManager.setMachineAllocation(System.currentTimeMillis(), 1, machineId);
                }
                /////////log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ scale-in suggestion executed " +
                        /////////"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ " + System.currentTimeMillis());
                break;
              case SCALE_OUT:
                ////resetVmInfoAtMonitor(groupRuntime.getId());
                ArrayList<MachineType> scaleOutMachines = suggestion.getScaleOutSuggestions();
                Thread.
                    sleep((1000 * 60 + new Random().nextInt(30 * 1000)));  //1 min - 1 1/2 min
                // upto 20seconds
                for (MachineType machine : scaleOutMachines) {
                  addVmIdToMonitorSimulation(groupId, String.valueOf(UUID.randomUUID()),
                      machine.getProperty(MachineType.Properties.TYPE.name()));
                  StatManager.setMachineAllocation(System.currentTimeMillis(), 1, machine.getProperty(
                          MachineType.Properties.TYPE.name()));
                }
                /////////log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ scale-out suggestion executed " +
                        //////"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ " + System.currentTimeMillis());
                break;
              case TMP_SCALEIN:
                /////resetVmInfoAtMonitor(groupRuntime.getId());
                int noOfMachinesToRemove = Math.abs(suggestion.getScaleInNumber());
                ArrayList<String> allVms = new ArrayList<>(Arrays.asList(honeyTapAPI.getAllSimulatedVmIds(groupId)));
                Thread.sleep(new Random().nextInt(10 * 1000) + 5 * 1000);  // delay upto 5 - 15 seconds
                for (int i = 0; i < noOfMachinesToRemove; ++i) {
                  int removeIndex = new Random().nextInt(allVms.size());
                  String vmIdToRemove = allVms.get(removeIndex);
                  allVms.remove(vmIdToRemove);
                  removeVmIdfromMonitorSimulation(groupId, vmIdToRemove);
                  StatManager.setMachineAllocation(System.currentTimeMillis(), 1, vmIdToRemove);
                }
                ///////////log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@ scalein-tmp suggestion executed " +
                        ///////////"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ " + System.currentTimeMillis());
                break;
              default:
                log.warn("SIMULATION: Handle scaling has not been implemented for the scaling direction: "
                    + suggestion.getScalingDirection().name());
                break;
            }
          }
        } catch (InterruptedException e) {
          log.error("Error while taking the auto-scaling suggestion in group: " + groupId);
        }
      }
    }

    public void stopAutoScalingSuggestionExecution() {
      this.shouldAutoScale = false;
    }

    private void addVmIdToMonitorSimulation(String groupId, String vmId, String machineType) {
      InstanceType instanceType = InstanceType.valueByModel(machineType);
      honeyTapAPI.addSimulatedVmInfo(groupId, vmId, instanceType.numVCpu, instanceType.memInGig,
          instanceType.numDisks, instanceType.diskSize);
      log.info("************** adding machine: type, id ***************************: " + machineType + ", " + vmId);
    }

    private void removeVmIdfromMonitorSimulation(String groupId, String vmId) {
      honeyTapAPI.removeSimulatedVmInfo(groupId, vmId);
      log.info("************** removing machine: id ***************************: " + vmId);
    }
  }
}
