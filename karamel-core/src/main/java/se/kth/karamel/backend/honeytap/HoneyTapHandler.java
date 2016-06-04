package se.kth.karamel.backend.honeytap;

import org.apache.log4j.Logger;
import se.kth.honeytap.scaling.ScalingSuggestion;
import se.kth.honeytap.scaling.core.HoneyTapAPI;
import se.kth.honeytap.scaling.exceptions.HoneyTapException;
import se.kth.honeytap.scaling.models.MachineType;
//import se.kth.honeytap.scaling.rules.Rule;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.launcher.amazon.InstanceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
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
  private static HoneyTapAPI honeyTapAPI;
  private Map<String, AutoScalingSuggestionExecutor> groupExecutorMap =
          new HashMap<String, AutoScalingSuggestionExecutor>();
  private boolean isAutoScalingActive = false;

  //for simulation
  boolean isSimulation = true;
  long scaleOutDelay = 1000 * 60; // 1 min

  public HoneyTapHandler(int noOfGroupsInCluster, HoneyTapAPI honeyTapAPI) {
    this.honeyTapAPI = honeyTapAPI;
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
        ArrayBlockingQueue<ScalingSuggestion> suggestionQueue = honeyTapAPI.getSuggestionQueue(groupRuntime.getId());
        if (suggestionQueue != null) {
          this.suggestionsQueueOfGroup = suggestionQueue;
          logger.info(" ############### AS suggestion queue recieved, group: " + groupRuntime.getId() +
                  "#################");
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
            logger.info("##################### SIMULATION scaling suggestions #############");
            switch (suggestion.getScalingDirection()) {
              case SCALE_IN:
                resetVmInfoAtMonitor(groupRuntime.getId());  //setting actual running vms
                ArrayList<String> machinesToRemove = suggestion.getScaleInSuggestions();
                Thread.sleep(new Random().nextInt(20 * 1000));  // delay upto 20 seconds
                for (String machineId : machinesToRemove) {
                  removeVmIdfromMonitorSimulation(groupRuntime.getId(), machineId);
                }
                break;
              case SCALE_OUT:
                resetVmInfoAtMonitor(groupRuntime.getId());
                ArrayList<MachineType> scaleOutMachines = suggestion.getScaleOutSuggestions();
                Thread.sleep(scaleOutDelay + new Random().nextInt(20 * 1000));  //1 min + making a random addition
                                                                                          // upto 20seconds
                for (MachineType machine : scaleOutMachines) {
                  addVmIdToMonitorSimulation(groupRuntime.getId(), String.valueOf(UUID.randomUUID()),
                          machine.getProperty(MachineType.Properties.TYPE.name()));
                }
                break;
              case TMP_SCALEIN:
                resetVmInfoAtMonitor(groupRuntime.getId());
                int noOfMachinesToRemove = Math.abs(suggestion.getScaleInNumber());
                ArrayList<String> allVms = new ArrayList<>(Arrays.asList(honeyTapAPI.getAllVmIds(
                        groupRuntime.getId())));
                Thread.sleep(new Random().nextInt(20 * 1000));  // delay upto 20 seconds
                for (int i = 0; i < noOfMachinesToRemove; ++i) {
                  int removeIndex = new Random().nextInt(allVms.size());
                  String vmIdToRemove = allVms.get(removeIndex);
                  allVms.remove(vmIdToRemove);
                  removeVmIdfromMonitorSimulation(groupRuntime.getId(), vmIdToRemove);
                }
                break;
              default:
                logger.warn("SIMULATION: Handle scaling has not been implemented for the scaling direction: " +
                        suggestion.getScalingDirection().name());
                break;
            }
          } else {
            logger.info("########################## NON simulation got suggestion: " + groupRuntime.getName() + " " +
                    suggestion.getScalingDirection().name() + " ######################################");
            switch (suggestion.getScalingDirection()) {
              case SCALE_IN:
                ArrayList<String> machinesToRemove = suggestion.getScaleInSuggestions();
                handleScaleInSuggestion(machinesToRemove.toArray(new String[machinesToRemove.size()]));
                resetVmInfoAtMonitor(groupRuntime.getId());
                break;
              case SCALE_OUT:
                ArrayList<MachineType> scaleOutMachines = suggestion.getScaleOutSuggestions();
                handleScaleOutSuggestion(scaleOutMachines.toArray(new MachineType[scaleOutMachines.size()]));
                resetVmInfoAtMonitor(groupRuntime.getId());
                break;
              case TMP_SCALEIN:
                ArrayList<String> toRemove = suggestion.getScaleInSuggestions();
                handleScaleInSuggestion(toRemove.toArray(new String[toRemove.size()]));
                resetVmInfoAtMonitor(groupRuntime.getId());
                break;
              default:
                logger.warn("Handle scaling has not been implemented for the scaling direction: " +
                        suggestion.getScalingDirection().name());
                break;
            }
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
        /*//TODO-AS temporary removing first Id -remove passing ids
        vmIds = new String[]{groupRuntime.getMachines().get(0).getVmId()};*/

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

    private void resetVmInfoAtMonitor(String groupId) {
      List<MachineRuntime> machineRuntimes = groupRuntime.getMachines();
      for (int i = 0; i < machineRuntimes.size(); ++i) {
        MachineRuntime machineRuntime = machineRuntimes.get(i);
        boolean isStart = false;
        if (i == 0) {
          isStart = true;
        }
        try {
          InstanceType instanceType = InstanceType.valueByModel(machineRuntime.getMachineType().split("/")[2]);
          HoneyTapAPI.getInstance().addVmInfo(groupId, machineRuntime.getVmId(), instanceType.numVCpu,
                  instanceType.memInGig, instanceType.numDisks, instanceType.diskSize, isStart);
        } catch (HoneyTapException e) {
          throw new IllegalStateException(e);
        }
      }
    }

    private void addVmIdToMonitorSimulation(String groupId, String vmId, String machineType) {
      try {
        InstanceType instanceType = InstanceType.valueByModel(machineType);
        HoneyTapAPI.getInstance().addSimulatedVmInfo(groupId, vmId, instanceType.numVCpu, instanceType.memInGig,
                instanceType.numDisks, instanceType.diskSize);
      } catch (HoneyTapException e) {
        throw new IllegalStateException(e);
      }
    }

    private void removeVmIdfromMonitorSimulation(String groupId, String vmId) {
      try {
        HoneyTapAPI.getInstance().removeSimulatedVmInfo(groupId, vmId);
      } catch (HoneyTapException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
