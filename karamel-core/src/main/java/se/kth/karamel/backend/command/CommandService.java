/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.command;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.ClusterManager;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.LogService;
import se.kth.karamel.backend.converter.ChefJsonGenerator;
import se.kth.karamel.backend.converter.UserClusterDataExtractor;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.kandy.KandyRestClient;
import se.kth.karamel.backend.launcher.OsType;
import se.kth.karamel.backend.launcher.amazon.Ec2Context;
import se.kth.karamel.backend.machines.MachinesMonitor;
import se.kth.karamel.backend.machines.SshMachine;
import se.kth.karamel.backend.machines.SshShell;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.mocking.MockingUtil;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.Failure;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.tasks.DagBuilder;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.common.TextTable;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.stats.ClusterStats;
import se.kth.karamel.common.stats.PhaseStat;
import se.kth.karamel.common.stats.TaskStat;
import se.kth.karamel.common.util.IoUtils;
import se.kth.karamel.common.util.SshKeyPair;

/**
 * Terminal backend, a replacement for API with more flexibilities. It processes user commands and generates mere
 * hyper-link aware textual pages. Each hyper-link is another command and results of each command is another page.
 *
 * @author kamal
 */
public class CommandService {

  private static final Logger logger = Logger.getLogger(CommandService.class);
  private static String chosenCluster = null;
  private static String autoselectedCluster = null;
  private static String chosenMachine = "";
  private static final ClusterService clusterService = ClusterService.getInstance();
  private static SshShell shell = null;
  private static String MENU_BAR = "";
  private static String HELP_PAGE_TEMPLATE = "";
  private static String HOME_PAGE_TEMPLATE = "";
  private static String RUNNING_PAGE_TEMPLATE = "";
  private static String YAMLS_TABLE_PLH = "%YAMLS_TABLE%";
  private static String CLUSTERS_TABLE_PLH = "%CLUSTERS_TABLE%";
  private static String HYPERLINKS_PLH = "%HYPERLINKS%";

  static {
    try {
      HELP_PAGE_TEMPLATE = IoUtils.readContentFromClasspath("se/kth/karamel/backend/command/helppage");
      HOME_PAGE_TEMPLATE = IoUtils.readContentFromClasspath("se/kth/karamel/backend/command/homepage");
      RUNNING_PAGE_TEMPLATE = IoUtils.readContentFromClasspath("se/kth/karamel/backend/command/running");
    } catch (IOException e) {

    }
  }

  public static CommandResponse processCommand(String command, String... args) throws KaramelException {
    String cmd = command;
    String nextCmd = null;
    CommandResponse.Renderer renderer = CommandResponse.Renderer.INFO;
    CommandResponse response = new CommandResponse();
    response.addMenuItem("Home", "home");
    response.addMenuItem("New Cluster", "new");
    response.addMenuItem("List Clusters", "list");
    selectCluster();
    String result = null;
    String successMessage = null;
    String context = chosenCluster();
    if (cmd.equals("help")) {
      result = HELP_PAGE_TEMPLATE;
    } else if (cmd.equals("running")) {
      result = RUNNING_PAGE_TEMPLATE.replace(CLUSTERS_TABLE_PLH, clustersTable());
      ClusterManager cluster = cluster(context);
      String hyperLinks;
      if (cluster != null) {
        hyperLinks = UserClusterDataExtractor.clusterLinks(cluster.getDefinition(), cluster.getRuntime());
      } else {
        String yml = ClusterDefinitionService.loadYaml(context);
        JsonCluster json = ClusterDefinitionService.yamlToJsonObject(yml);
        hyperLinks = UserClusterDataExtractor.clusterLinks(json, null);
      }
      result = result.replace(HYPERLINKS_PLH, hyperLinks);
      nextCmd = "running";
    } else if (cmd.equals("home")) {
      result = HOME_PAGE_TEMPLATE.replace(YAMLS_TABLE_PLH, yamlsTable());
      result = result.replace(CLUSTERS_TABLE_PLH, clustersTable());
      nextCmd = "home";
    } else if (cmd.equals("clusters")) {
      result = clustersTable();
      nextCmd = "clusters";
    } else if (cmd.equals("list")) {
      StringBuilder builder = new StringBuilder();
      builder.append("Yaml Definitions:").append("\n").append("---------------------").append("\n");
      builder.append(yamlsTable());
      builder.append("\n").append("Running Clusters:").append("\n").append("----------------------").append("\n");
      builder.append(clustersTable());
      result = builder.toString();
      nextCmd = "list";
    } else if (cmd.equals("save")) {
      if (args.length == 0) {
        throw new KaramelException("Provide the yaml definition");
      }
      ClusterDefinitionService.saveYaml(args[0]);
      successMessage = "Yaml updated";
    } else if (cmd.equals("new")) {
      result = "";
      response.addMenuItem("Save", "save");
      renderer = CommandResponse.Renderer.YAML;
    } else {
      boolean found = false;
      Pattern p = Pattern.compile("use\\s+(\\w+)");
      Matcher matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        if (cluster(clusterName) != null) {
          chosenCluster = clusterName;
          successMessage = String.format("switched to %s now", clusterName);
        } else {
          throw new KaramelException(String.format("cluster %s is not registered yet!!", clusterName));
        }
      }

      p = Pattern.compile("remove\\s+(\\w+)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        if (cluster(clusterName) != null) {
          throw new KaramelException(String.format("%s is running now, terminate it first!!", clusterName));
        } else {
          ClusterDefinitionService.removeDefinition(clusterName);
          successMessage = String.format("cluster definition %s removed successfully..", clusterName);
        }
      }

      p = Pattern.compile("yaml\\s+(\\w+)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        if (chosenCluster().toLowerCase().equals(clusterName.toLowerCase())) {
          addActiveClusterMenus(response);
        }
        renderer = CommandResponse.Renderer.YAML;
        result = ClusterDefinitionService.loadYaml(clusterName);
      }
      String clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "install");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        clusterService.submitInstallationDag(clusterNameInUserInput);
        successMessage = clusterNameInUserInput + " was scheduled for installing, "
            + "it might take some time please be patient!";
        nextCmd = "status " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "purge");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        clusterService.submitPurgeDag(clusterNameInUserInput);
        successMessage = clusterNameInUserInput + " was scheduled for purging, "
            + "it might take some time please be patient!";
        nextCmd = "status " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "pause");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        clusterService.pauseDag(clusterNameInUserInput);
        successMessage = clusterNameInUserInput + " was scheduled for pausing, "
            + "it might take some time please be patient!";
        nextCmd = "status " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "resume");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        clusterService.resumeDag(clusterNameInUserInput);
        successMessage = clusterNameInUserInput + " was scheduled for resuming, "
            + "it might take some time please be patient!";
        nextCmd = "status " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "terminate");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        clusterService.terminateCluster(clusterNameInUserInput);
        successMessage = clusterNameInUserInput + " was scheduled for terminating, "
            + "it might take some time please be patient!";
        nextCmd = "status " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "status");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        addActiveClusterMenus(response);
        StringBuilder builder = new StringBuilder();
        ClusterManager cluster = cluster(clusterNameInUserInput);
        ClusterRuntime clusterEntity = cluster.getRuntime();
        builder.append(clusterEntity.getName()).append(" is ").append(clusterEntity.getPhase());
        if (clusterEntity.isPaused()) {
          builder.append(" but it is on pause.").append("\n");
        }

        if (clusterEntity.isFailed() && !clusterEntity.getFailures().isEmpty()) {
          builder.append(" List of failures: ").append("\n");
          builder.append(failureTable(clusterEntity.getFailures().values(), true));
        }
        builder.append("\n\n");
        builder.append("Passed Phases:");
        builder.append("\n");
        builder.append(phaseStatsTable(cluster.getStats()));
        builder.append("\n\n");
        builder.append("Tasks' Status:");
        builder.append("\n");
        builder.append(machinesTasksTable(clusterEntity));
        result = builder.toString();
        nextCmd = "status " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "detail");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        addActiveClusterMenus(response);
        ClusterManager cluster = cluster(clusterNameInUserInput);
        JsonCluster json = cluster.getDefinition();
        result = ClusterDefinitionService.serializeJson(json);
        nextCmd = "detail " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "groups");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        addActiveClusterMenus(response);
        ClusterManager cluster = cluster(clusterNameInUserInput);
        ClusterRuntime clusterEntity = cluster.getRuntime();
        String[] columnNames = {"Name", "Phase"};
        String[][] data = new String[clusterEntity.getGroups().size()][2];
        for (int i = 0; i < clusterEntity.getGroups().size(); i++) {
          GroupRuntime group = clusterEntity.getGroups().get(i);
          data[i][0] = group.getName();
          data[i][1] = String.valueOf(group.getPhase());
        }
        result = TextTable.makeTable(columnNames, 0, data, true);
        nextCmd = "groups " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "machines");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        addActiveClusterMenus(response);
        ClusterManager cluster = cluster(clusterNameInUserInput);
        ClusterRuntime clusterEntity = cluster.getRuntime();
        ArrayList<MachineRuntime> machines = new ArrayList<>();
        for (GroupRuntime group : clusterEntity.getGroups()) {
          for (MachineRuntime machine : group.getMachines()) {
            machines.add(machine);
          }
        }
        result = machinesTable(machines, true);
        nextCmd = "machines " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "tasks");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        addActiveClusterMenus(response);
        ClusterManager cluster = cluster(clusterNameInUserInput);
        ClusterRuntime clusterEntity = cluster.getRuntime();
        result = machinesTasksTable(clusterEntity);
        nextCmd = "tasks " + clusterNameInUserInput;
      }

      clusterNameInUserInput = getClusterNameIfRunningAndMatchesForCommand(cmd, "stats");
      if (!found && clusterNameInUserInput != null) {
        found = true;
        addActiveClusterMenus(response);
        ClusterManager cluster = cluster(clusterNameInUserInput);
        ClusterStats stats = cluster.getStats();
        result = statsTable(cluster.getDefinition().getName(), stats);
        nextCmd = "stats " + clusterNameInUserInput;
      }

      p = Pattern.compile("shellconnect\\s+((\\d|.)+)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String ip = matcher.group(1);
        if (chosenCluster() != null) {
          String clusterName = chosenCluster();
          ClusterManager clusterMgr = cluster(clusterName);
          MachinesMonitor mm = clusterMgr.getMachinesMonitor();
          SshMachine machine = mm.getMachine(ip);
          if (machine != null) {
            shell = machine.getShell();
            if (!shell.isConnected()) {
              shell.connect();
              if (!shell.isConnected()) {
                throw new KaramelException("attempt to connect shell was not successful!!");
              } else {
                try {
                  Thread.sleep(200);
                } catch (InterruptedException ex) {
                  logger.error("", ex);
                }
              }
            }
            context = chosenCluster() + "/" + shell.getIpAddress();
            result = shell.readStreams();
            renderer = CommandResponse.Renderer.SSH;
            addActiveMachineMenus(response);
          } else {
            throw new KaramelException("Opps, machine was not found, make sure cluster is chosen first");
          }
        } else {
          throw new KaramelException("no cluster has been chosen yet!!");
        }
      }
      p = Pattern.compile("shelldisconnect");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        if (chosenCluster() != null) {
          if (shell != null) {
            shell.disconnect();
            successMessage = "Shell disconnected";
            renderer = CommandResponse.Renderer.INFO;
          } else {
            throw new KaramelException("Opps, there is not connected shell..");
          }
        } else {
          throw new KaramelException("no cluster has been chosen yet!!");
        }
      }

      p = Pattern.compile("shellexec((.|\n|\t|\r|\033|\003|\004)+)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String cmdStr = matcher.group(1);
        cmdStr = cmdStr.replace("arrup", "\033[A");
        cmdStr = cmdStr.replace("arrdown", "\033[B");
        if (chosenCluster() != null) {
          if (shell != null) {
            if (!shell.isConnected()) {
              throw new KaramelException("shell is not connected.");
            } else {
              shell.exec(cmdStr);
              try {
                Thread.sleep(100);
              } catch (InterruptedException ex) {
                logger.error("", ex);
              }
              context = chosenCluster() + "/" + shell.getIpAddress();
              result = shell.readStreams();
              renderer = CommandResponse.Renderer.SSH;
              addActiveMachineMenus(response);
            }
          } else {
            throw new KaramelException("Opps, there is not connected shell..");
          }
        } else {
          throw new KaramelException("no cluster has been chosen yet!!");
        }
      }

      p = Pattern.compile("shellread");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        if (chosenCluster() != null) {
          if (shell != null) {
            if (!shell.isConnected()) {
              throw new KaramelException("shell is not connected.");
            } else {
              context = chosenCluster() + "/" + shell.getIpAddress();
              result = shell.readStreams();
              renderer = CommandResponse.Renderer.SSH;
              addActiveMachineMenus(response);
            }
          } else {
            throw new KaramelException("Opps, there is not connected shell..");
          }
        } else {
          throw new KaramelException("no cluster has been chosen yet!!");
        }

      }

      p = Pattern.compile("tdag\\s+(\\w+)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        ClusterManager cluster = cluster(clusterName);
        if (cluster == null || cluster.getCurrentDag() == null) {
          TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

            @Override
            public void submitTask(Task task) throws KaramelException {
              logger.info(" Received request to process a command with info: " + task.uniqueId());
              task.succeed();
            }

            @Override
            public void prepareToStart(Task task) throws KaramelException {
            }

            @Override
            public void killMe(Task task) throws KaramelException {
            }

            @Override
            public void retryMe(Task task) throws KaramelException {
            }

            @Override
            public void skipMe(Task task) throws KaramelException {
            }

            @Override
            public void terminate(Task task) throws KaramelException {
            }
          };
          String yml = ClusterDefinitionService.loadYaml(clusterName);
          JsonCluster json = ClusterDefinitionService.yamlToJsonObject(yml);
          ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(json);
          Map<String, JsonObject> chefJsons = ChefJsonGenerator.
              generateClusterChefJsonsForInstallation(json, dummyRuntime);
          ClusterStats clusterStats = new ClusterStats();
          Dag installationDag = DagBuilder.getInstallationDag(json, dummyRuntime, clusterStats, dummyTaskSubmitter,
              chefJsons);
          installationDag.validate();
          result = installationDag.print();
        } else {
          result = cluster.getCurrentDag().print();
          addActiveClusterMenus(response);
          nextCmd = cmd;
        }

        renderer = CommandResponse.Renderer.INFO;
      }

      p = Pattern.compile("vdag\\s+(\\w+)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        ClusterManager cluster = cluster(clusterName);
        if (cluster == null || cluster.getCurrentDag() == null) {
          TaskSubmitter dummyTaskSubmitter = new TaskSubmitter() {

            @Override
            public void submitTask(Task task) throws KaramelException {
              task.succeed();
            }

            @Override
            public void prepareToStart(Task task) throws KaramelException {
            }

            @Override
            public void killMe(Task task) throws KaramelException {
            }

            @Override
            public void retryMe(Task task) throws KaramelException {
            }

            @Override
            public void skipMe(Task task) throws KaramelException {
            }

            @Override
            public void terminate(Task task) throws KaramelException {
            }
          };
          String yml = ClusterDefinitionService.loadYaml(clusterName);
          JsonCluster json = ClusterDefinitionService.yamlToJsonObject(yml);
          ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(json);
          Map<String, JsonObject> chefJsons = ChefJsonGenerator.
              generateClusterChefJsonsForInstallation(json, dummyRuntime);
          ClusterStats clusterStats = new ClusterStats();
          Dag installationDag = DagBuilder.getInstallationDag(json, dummyRuntime, clusterStats, dummyTaskSubmitter,
              chefJsons);
          installationDag.validate();
          result = installationDag.asJson();
          if (cluster != null) {
            addActiveClusterMenus(response);
          }
        } else {
          result = cluster.getCurrentDag().asJson();
          addActiveClusterMenus(response);
          nextCmd = cmd;
        }

        renderer = CommandResponse.Renderer.DAG;
      }

      p = Pattern.compile("links\\s+(\\w+)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        ClusterManager cluster = cluster(clusterName);
        if (cluster != null) {
          result = UserClusterDataExtractor.clusterLinks(cluster.getDefinition(), cluster.getRuntime());
          addActiveClusterMenus(response);
        } else {
          String yml = ClusterDefinitionService.loadYaml(clusterName);
          JsonCluster json = ClusterDefinitionService.yamlToJsonObject(yml);
          result = UserClusterDataExtractor.clusterLinks(json, null);
        }

        renderer = CommandResponse.Renderer.INFO;
      }

      p = Pattern.compile("launch\\s+(\\w+)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        if (cluster(clusterName) != null) {
          throw new KaramelException(String.format("%s is already running, terminate it first!!", clusterName));
        } else {
          String yaml = ClusterDefinitionService.loadYaml(clusterName);
          String json = ClusterDefinitionService.yamlToJson(yaml);
          clusterService.startCluster(json);
          successMessage = String.format("cluster %s launched successfully..", clusterName);
          nextCmd = "status";
          addActiveClusterMenus(response);
        }
      }

      p = Pattern.compile("cost\\s+(\\w+)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String clusterName = matcher.group(1);
        String yaml = ClusterDefinitionService.loadYaml(clusterName);
        if (yaml != null) {
          result = KandyRestClient.estimateCost(yaml);
          if (result != null) {
            renderer = CommandResponse.Renderer.INFO;
          } else {
            throw new KaramelException("Kandy could not estimate the cost.");
          }
        } else {
          throw new KaramelException("The cluster definition does not exist.");
        }
      }

      p = Pattern.compile("log\\s+(.*)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String taskuuid = matcher.group(1);
        if (chosenCluster() != null) {
          boolean taskFound = false;
          ClusterManager cluster = cluster(chosenCluster());
          ClusterRuntime clusterEntity = cluster.getRuntime();
          for (GroupRuntime group : clusterEntity.getGroups()) {
            for (MachineRuntime machine : group.getMachines()) {
              for (Task task : machine.getTasks()) {
                if (task.getUuid().equals(taskuuid)) {
                  taskFound = true;
                  result = LogService.loadLog(clusterEntity.getName(), machine.getPublicIp(), task.getName());
                }
              }
            }
          }
          if (!taskFound) {
            throw new KaramelException("Opps, task was not found, make sure cluster is chosen first");
          }
          addActiveClusterMenus(response);
        } else {
          throw new KaramelException("no cluster has been chosen yet!!");
        }
      }

      p = Pattern.compile("skipfailed\\s+(.*)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String taskuuid = matcher.group(1);
        if (chosenCluster() != null) {
          boolean taskFound = false;
          ClusterManager cluster = cluster(chosenCluster());
          ClusterRuntime clusterEntity = cluster.getRuntime();
          for (GroupRuntime group : clusterEntity.getGroups()) {
            for (MachineRuntime machine : group.getMachines()) {
              for (Task task : machine.getTasks()) {
                if (task.getUuid().equals(taskuuid)) {
                  taskFound = true;
                  if (task.getStatus() == Task.Status.FAILED) {
                    task.skip();
                    successMessage = String.format("The failed task '%s' on '%s' has been skipped successfully",
                        task.getName(), task.getMachineId());
                  }
                }
              }
            }
          }
          if (!taskFound) {
            throw new KaramelException("Opps, task was not found, make sure cluster is chosen first");
          }
          addActiveClusterMenus(response);
        } else {
          throw new KaramelException("no cluster has been chosen yet!!");
        }
        nextCmd = "status";
      }

      p = Pattern.compile("retryfailed\\s+(.*)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String taskuuid = matcher.group(1);
        if (chosenCluster() != null) {
          boolean taskFound = false;
          ClusterManager cluster = cluster(chosenCluster());
          ClusterRuntime clusterEntity = cluster.getRuntime();
          for (GroupRuntime group : clusterEntity.getGroups()) {
            for (MachineRuntime machine : group.getMachines()) {
              for (Task task : machine.getTasks()) {
                if (task.getUuid().equals(taskuuid)) {
                  taskFound = true;
                  if (task.getStatus() == Task.Status.FAILED) {
                    task.retry();
                    successMessage = String.format("The failed task '%s' on '%s' has been retried successfully",
                        task.getName(), task.getMachineId());
                  }
                }
              }
            }
          }
          if (!taskFound) {
            throw new KaramelException("Opps, task was not found, make sure cluster is chosen first");
          }
          addActiveClusterMenus(response);
        } else {
          throw new KaramelException("no cluster has been chosen yet!!");
        }
        nextCmd = "status";
      }

      p = Pattern.compile("kill\\s+(.*)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String taskuuid = matcher.group(1);
        if (chosenCluster() != null) {
          boolean taskFound = false;
          ClusterManager cluster = cluster(chosenCluster());
          ClusterRuntime clusterEntity = cluster.getRuntime();
          for (GroupRuntime group : clusterEntity.getGroups()) {
            for (MachineRuntime machine : group.getMachines()) {
              for (Task task : machine.getTasks()) {
                if (task.getUuid().equals(taskuuid)) {
                  taskFound = true;
                  if (task.getStatus() == Task.Status.ONGOING) {
                    task.kill();
                    successMessage = String.format("The failed task '%s' on '%s' has been killed successfully",
                        task.getName(), task.getMachineId());
                  }
                }
              }
            }
          }
          if (!taskFound) {
            throw new KaramelException("Opps, task was not found, make sure cluster is chosen first");
          }
          addActiveClusterMenus(response);
        } else {
          throw new KaramelException("no cluster has been chosen yet!!");
        }
        nextCmd = "status";
      }

      p = Pattern.compile("which\\s+(cluster|aws|ssh)");
      matcher = p.matcher(cmd);
      if (!found && matcher.matches()) {
        found = true;
        String subcmd = matcher.group(1);
        if (subcmd.equals("cluster")) {
          if (chosenCluster() != null) {
            result = String.format("%s has been chosen.", chosenCluster());
          } else {
            throw new KaramelException("no cluster has been chosen yet!!");
          }
        } else if (subcmd.equals("aws")) {
          Ec2Context ec2Context = clusterService.getCommonContext().getEc2Context();
          if (ec2Context != null) {
            result = String.format("aws account id is %s", ec2Context.getCredentials().getAccessKey());
          } else {
            throw new KaramelException("no aws account has been chosen yet!!");
          }
        } else if (subcmd.equals("ssh")) {
          SshKeyPair sshKeyPair = clusterService.getCommonContext().getSshKeyPair();
          if (sshKeyPair != null) {
            result = String.format("public key path: %s \nprivate key path: %s", sshKeyPair.getPublicKeyPath(),
                sshKeyPair.getPrivateKeyPath());
          } else {
            throw new KaramelException("no ssh keys has been chosen yet!!");
          }
        }
      }
    }
    if (result == null && successMessage == null) {
      throw new KaramelException(String.format("Command '%s' not found", cmd));
    }
    response.setContext(context);
    response.setSuccessMessage(successMessage);
    response.addMenuItem("Help", "help");
    response.setNextCmd(nextCmd);
    response.setResult(result);
    response.setRenderer(renderer);
    return response;
  }

  private static String chosenCluster() {
    if (chosenCluster != null) {
      return chosenCluster;
    } else if (autoselectedCluster != null) {
      return autoselectedCluster;
    } else {
      return null;
    }
  }

  private static void selectCluster() {
    Map<String, ClusterManager> repository = clusterService.getRepository();
    if (autoselectedCluster != null && cluster(autoselectedCluster) == null) {
      autoselectedCluster = null;
    }
    if (chosenCluster != null && cluster(chosenCluster) == null) {
      chosenCluster = null;
    }

    if (chosenCluster == null && repository.size() == 1) {
      autoselectedCluster = (String) repository.keySet().toArray()[0];
    }
  }

  private static ClusterManager cluster(String name) {
    Map<String, ClusterManager> repository = clusterService.getRepository();
    Set<Map.Entry<String, ClusterManager>> clusters = repository.entrySet();
    for (Map.Entry<String, ClusterManager> cluster : clusters) {
      if (cluster.getKey().toLowerCase().equals(name.toLowerCase())) {
        return cluster.getValue();
      }
    }
    return null;
  }

  private static String clustersTable() {
    Map<String, ClusterManager> repository = clusterService.getRepository();
    String[] columnNames = {"Cluster Name", "Phase", "Failed/Paused", "Actions"};
    Object[][] data = new Object[repository.size()][columnNames.length];
    int i = 0;
    for (ClusterManager cluster : repository.values()) {
      String name = cluster.getDefinition().getName();
      data[i][0] = name;
      data[i][1] = cluster.getRuntime().getPhase();
      data[i][2] = cluster.getRuntime().isFailed() + "/" + cluster.getRuntime().isPaused();
      data[i][3] = "<a kref='status " + name + "'>status</a> <a kref='tdag " + name + "'>tdag</a> <a kref='vdag "
          + name + "'>vdag</a> <a kref='groups " + name + "'>groups</a> <a kref='machines "
          + name + "'>machines</a> <a kref='tasks " + name + "'>tasks</a> <a kref='terminate "
          + name + "'>terminate</a> <a kref='links " + name + "'>services</a> <a kref='yaml "
          + name + "'>yaml</a> <a kref='cost " + name + "'>cost</a>";
      i++;
    }

    return TextTable.makeTable(columnNames, 1, data, true);
  }

  private static String yamlsTable() throws KaramelException {
    List<String> defs = ClusterDefinitionService.listClusters();
    String[] columnNames = {"Yaml Name", "Actions"};
    Object[][] data = new Object[defs.size()][columnNames.length];
    int i = 0;
    for (String yaml : defs) {
      data[i][0] = yaml;
      data[i][1] = "<a kref='yaml " + yaml + "'>edit</a> <a kref='tdag " + yaml + "'>tdag</a> <a kref='vdag "
          + yaml + "'>vdag</a> <a kref='launch " + yaml + "'>launch</a> <a kref='remove "
          + yaml + "'>remove</a> <a kref='links " + yaml + "'>services</a>  <a kref='cost " + yaml + "'>cost</a>";
      i++;
    }

    return TextTable.makeTable(columnNames, 1, data, true);
  }

  private static String failureTable(Collection<Failure> failures, boolean rowNumbering) {
    String[] columnNames = {"Failure", "Id", "Message"};
    Object[][] data = new Object[failures.size()][columnNames.length];
    int i = 0;
    for (Failure failure : failures) {
      data[i][0] = failure.getType().name();
      data[i][1] = (failure.getId() == null) ? "" : failure.getId();
      data[i][2] = failure.getMessage();
      i++;
    }

    return TextTable.makeTable(columnNames, 1, data, rowNumbering);
  }

  private static String tasksTable(List<Task> tasks, boolean rowNumbering) {
    String[] columnNames = {"Task", "Status", "Actions", "Duration(ms)"};
    Object[][] data = new Object[tasks.size()][columnNames.length];
    for (int i = 0; i < tasks.size(); i++) {
      Task task = tasks.get(i);
      data[i][0] = task.getName();
      data[i][1] = task.getStatus();
      String uuid = task.getUuid();
      String actions = "";
      if (task.getStatus().ordinal() == Task.Status.ONGOING.ordinal()) {
        actions += "<a kref='kill " + uuid + "'>kill</a>";
      }

      if (task.getStatus().ordinal() == Task.Status.FAILED.ordinal()) {
        actions += "<a kref='retryfailed " + uuid + "'>retry</a>"
            + "  <a kref='skipfailed " + uuid + "'>skip</a>";
      }

      if (task.getStatus().ordinal() > Task.Status.ONGOING.ordinal()) {
        actions += "  <a kref='log " + uuid + "'>log</a>";
      }

      data[i][2] = actions;

      if (task.getDuration() > 0) {
        data[i][3] = task.getDuration();
      } else {
        data[i][3] = "";
      }
    }
    return TextTable.makeTable(columnNames, 1, data, rowNumbering);
  }

  private static String machinesTable(ArrayList<MachineRuntime> machines, boolean rowNumbering) {
    String[] columnNames = {"OS Family", "Public IP", "Private IP", 
      "SSH Port", "SSH User", "Life Status", "Task Status"};
    Object[][] data = new Object[machines.size()][columnNames.length];
    for (int i = 0; i < machines.size(); i++) {
      MachineRuntime machine = machines.get(i);
      OsType osType = machine.getOsType();
      data[i][0] = (osType == null) ? "?" : osType.family.toString();
      data[i][1] = "<a kref='shellconnect " + machine.getPublicIp() + "'>" + machine.getPublicIp() + "</a>";
      data[i][2] = machine.getPrivateIp();
      data[i][3] = machine.getSshPort();
      data[i][4] = machine.getSshUser();
      data[i][5] = machine.getLifeStatus();
      data[i][6] = machine.getTasksStatus();
    }
    return TextTable.makeTable(columnNames, 6, data, rowNumbering);
  }

  private static String statsTable(String clusterName, ClusterStats stats) {
    StringBuilder builder = new StringBuilder();
    builder.append("Total Time: ").append(stats.getEndTime() - stats.getStartTime());
    builder.append("\n");
    builder.append(phaseStatsTable(stats));
    builder.append("\n");
    builder.append(taskStatsTable(stats));
    builder.append("\n");
    return builder.toString();
  }

  private static String phaseStatsTable(ClusterStats stats) {
    String[] columnNames = {"Phase", "Status", "Duration"};
    ArrayList<PhaseStat> phases = Lists.newArrayList(stats.getPhases());
    Object[][] data = new Object[phases.size()][columnNames.length];
    for (int i = 0; i < phases.size(); i++) {
      PhaseStat phase = phases.get(i);
      data[i][0] = phase.getName();
      data[i][1] = phase.getStatus();
      data[i][2] = phase.getDuration();
    }
    return TextTable.makeTable(columnNames, 0, data, true);
  }

  private static String taskStatsTable(ClusterStats stats) {
    String[] columnNames = {"Task", "Status", "Duration"};
    ArrayList<TaskStat> tasks = Lists.newArrayList(stats.getTasks());
    Object[][] data = new Object[tasks.size()][columnNames.length];
    for (int i = 0; i < tasks.size(); i++) {
      TaskStat task = tasks.get(i);
      data[i][0] = task.getTaskId();
      data[i][1] = task.getStatus();
      data[i][2] = task.getDuration();
    }
    return TextTable.makeTable(columnNames, 0, data, true);
  }

  private static String machinesTasksTable(ClusterRuntime clusterEntity) {
    StringBuilder builder = new StringBuilder();
    for (GroupRuntime group : clusterEntity.getGroups()) {
      for (MachineRuntime machine : group.getMachines()) {
        ArrayList<MachineRuntime> machines = new ArrayList<>();
        machines.add(machine);
        builder.append(machinesTable(machines, false));
        builder.append("\n");
        builder.append(tasksTable(machine.getTasks(), true));
        builder.append("\n");
      }
    }
    return builder.toString();
  }

  private static void addActiveMachineMenus(CommandResponse response) {
    response.addMenuItem("Status", "status");
    response.addMenuItem("Machines", "machines");
    response.addMenuItem("Close Shell", "shelldisconnect");
  }

  private static void addActiveClusterMenus(CommandResponse response) {
    ClusterManager cluster = cluster(chosenCluster());
    String clusterName = cluster.getDefinition().getName().toLowerCase();
    ClusterRuntime clusterEntity = cluster.getRuntime();
    response.addMenuItem("View Definition", "yaml");
    response.addMenuItem("Status", "status");
    response.addMenuItem("Orchestration DAG", "vdag " + clusterName);
    response.addMenuItem("Quick Links", "links " + clusterName);
    response.addMenuItem("Statistics", "stats " + clusterName);
    if (clusterEntity.getPhase().ordinal() >= ClusterRuntime.ClusterPhases.MACHINES_FORKED.ordinal()
        || clusterEntity.getPhase().ordinal() <= ClusterRuntime.ClusterPhases.DAG_DONE.ordinal()) {
      response.addMenuItem("Install", "install");
      response.addMenuItem("Purge", "purge");
    }
    if (clusterEntity.getPhase() == ClusterRuntime.ClusterPhases.RUNNING_DAG) {
      if (clusterEntity.isPaused()) {
        response.addMenuItem("Resume", "resume");
      } else {
        response.addMenuItem("Pause", "pause");
      }
    }
    response.addMenuItem("Terminate", "terminate");
  }

  private static String getClusterNameIfRunningAndMatchesForCommand(String userinput, String cmd)
      throws KaramelException {
    Pattern p = Pattern.compile(cmd + "(\\s+(\\w+))?");
    Matcher matcher = p.matcher(userinput);
    if (matcher.matches()) {
      String clusterName;
      if (matcher.group(2) == null) {
        if (chosenCluster() != null) {
          ClusterManager cluster = cluster(chosenCluster());
          clusterName = cluster.getDefinition().getName();
        } else {
          throw new KaramelException("No cluster has been chosen yet! When you terminate a cluster it is removed "
              + "from the context.");
        }
      } else {
        clusterName = matcher.group(2);
      }
      return clusterName;
    } else {
      return null;
    }
  }

}
