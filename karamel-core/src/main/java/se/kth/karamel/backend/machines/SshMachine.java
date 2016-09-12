/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.machines;

import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.backend.running.model.tasks.Task.Status;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import se.kth.karamel.backend.ClusterService;
import se.kth.karamel.backend.LogService;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.Failure;
import se.kth.karamel.backend.running.model.tasks.KillSessionTask;
import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;
import se.kth.karamel.common.util.Confs;
import se.kth.karamel.common.util.IoUtils;

/**
 *
 * @author kamal
 */
public class SshMachine implements MachineInterface, Runnable {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private static final Logger logger = Logger.getLogger(SshMachine.class);
  private final MachineRuntime machineEntity;
  private final String serverPubKey;
  private final String serverPrivateKey;
  private final String passphrase;
  private SSHClient client;
  private long lastHeartbeat = 0;
  private final BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(Settings.MACHINES_TASKQUEUE_SIZE);
  private boolean stopping = false;
  private boolean killing = false;
  private final SshShell shell;
  private Task activeTask;
  private boolean isSucceedTaskHistoryUpdated = false;
  private final List<String> succeedTasksHistory = new ArrayList<>();
  private static Confs confs = Confs.loadKaramelConfs();

  /**
   * This constructor is used for users with SSH keys protected by a password
   *
   * @param machineEntity
   * @param serverPubKey
   * @param serverPrivateKey
   * @param passphrase
   */
  public SshMachine(MachineRuntime machineEntity, String serverPubKey, String serverPrivateKey, String passphrase) {
    this.machineEntity = machineEntity;
    this.serverPubKey = serverPubKey;
    this.serverPrivateKey = serverPrivateKey;
    this.passphrase = passphrase;
    this.shell = new SshShell(serverPrivateKey, serverPubKey, machineEntity.getPublicIp(),
        machineEntity.getSshUser(), passphrase, machineEntity.getSshPort());
  }

  public MachineRuntime getMachineEntity() {
    return machineEntity;
  }

  public SshShell getShell() {
    return shell;
  }

  public void setStopping(boolean stopping) {
    this.stopping = stopping;
  }

  public void pause() {
    if (anyFailure() && machineEntity.getTasksStatus().ordinal() < MachineRuntime.TasksStatus.PAUSING.ordinal()) {
      machineEntity.setTasksStatus(MachineRuntime.TasksStatus.PAUSING, null, null);
    }
  }

  public void resume() {
    if (!anyFailure()) {
      if (taskQueue.isEmpty()) {
        machineEntity.setTasksStatus(MachineRuntime.TasksStatus.EMPTY, null, null);
      } else {
        machineEntity.setTasksStatus(MachineRuntime.TasksStatus.ONGOING, null, null);
      }
    }
  }

  private boolean anyFailure() {
    boolean anyfailure = false;
    if (machineEntity.getTasksStatus() == MachineRuntime.TasksStatus.FAILED) {
      for (Task task : machineEntity.getTasks()) {
        if (task.getStatus() == Task.Status.FAILED) {
          anyfailure = true;
        }
      }
    }
    return anyfailure;
  }

  @Override
  public void run() {
    logger.info(String.format("%s: Started SSH_Machine d'-'", machineEntity.getId()));
    try {
      while (!stopping) {
        try {
          if (machineEntity.getLifeStatus() == MachineRuntime.LifeStatus.CONNECTED
              && (machineEntity.getTasksStatus() == MachineRuntime.TasksStatus.ONGOING
              || machineEntity.getTasksStatus() == MachineRuntime.TasksStatus.EMPTY)) {
            try {
              if (activeTask == null) {
                if (taskQueue.isEmpty()) {
                  machineEntity.setTasksStatus(MachineRuntime.TasksStatus.EMPTY, null, null);
                }
                activeTask = taskQueue.take();
                logger.debug(String.format("%s: Taking a new task from the queue.", machineEntity.getId()));
                machineEntity.setTasksStatus(MachineRuntime.TasksStatus.ONGOING, null, null);
              } else {
                logger.debug(
                    String.format("%s: Retrying a task that didn't complete on last execution attempt.",
                        machineEntity.getId()));
              }
              logger.debug(String.format("%s: Task for execution.. '%s'", machineEntity.getId(), activeTask.getName()));
              runTask(activeTask);
            } catch (InterruptedException ex) {
              if (stopping) {
                logger.info(String.format("%s: Stopping SSH_Machine", machineEntity.getId()));
                return;
              } else {
                logger.error(
                    String.format("%s: Got interrupted without having recieved stopping signal",
                        machineEntity.getId()));
              }
            }
          } else {
            if (machineEntity.getTasksStatus() == MachineRuntime.TasksStatus.PAUSING) {
              machineEntity.setTasksStatus(MachineRuntime.TasksStatus.PAUSED, null, null);
            }
            try {
              Thread.sleep(Settings.MACHINE_TASKRUNNER_BUSYWAITING_INTERVALS);
            } catch (InterruptedException ex) {
              if (!stopping) {
                logger.error(
                    String.format("%s: Got interrupted without having recieved stopping signal",
                        machineEntity.getId()));
              }
            }
          }
        } catch (Exception e) {
          logger.error(String.format("%s: ", machineEntity.getId()), e);
        }
      }
    } finally {
      disconnect();
    }
  }

  public void enqueue(Task task) throws KaramelException {
    logger.debug(String.format("%s: Queuing '%s'", machineEntity.getId(), task.toString()));
    try {
      taskQueue.put(task);
      task.queued();
    } catch (InterruptedException ex) {
      String message = String.format("%s: Couldn't queue task '%s'", machineEntity.getId(), task.getName());
      task.failed(message);
      throw new KaramelException(message, ex);
    }
  }

  public void remove(Task task) throws KaramelException {
    logger.debug(String.format("%s: De-queuing '%s'", machineEntity.getId(), task.toString()));
    taskQueue.remove(task);
    if (activeTask == task) {
      activeTask = null;
    }
  }

  public void killTaskSession(Task task) {
    if (activeTask == task) {
      logger.info(String.format("Killing '%s' on '%s'", task.getName(), task.getMachine().getPublicIp()));
      KillSessionTask killTask = new KillSessionTask();
      killing = true;
      runTask(killTask);
    } else {
      logger.warn(String.format("Request to kill '%s' on '%s' but the task is not ongoing now", task.getName(),
          task.getMachine().getPublicIp()));
    }
  }

  public void retryFailedTask(Task task) throws KaramelException {
    if (task.getStatus() == Status.FAILED) {
      logger.info(String.format("Retrying '%s' on '%s'", task.getName(), task.getMachine().getPublicIp()));
      machineEntity.getGroup().getCluster().resolveFailure(Failure.hash(Failure.Type.TASK_FAILED, task.getUuid()));
      task.retried();
      activeTask = task;
      resume();
    } else {
      String msg = String.format("Impossible to retry '%s' on '%s' because the task is not failed", task.getName(),
          task.getMachineId());
      logger.error(msg);
      throw new KaramelException(msg);
    }
  }

  public void skipFailedTask(Task task) throws KaramelException {
    if (task.getStatus() == Status.FAILED) {
      logger.info(String.format("Skipping '%s' on '%s'", task.getName(), task.getMachine().getPublicIp()));
      machineEntity.getGroup().getCluster().resolveFailure(Failure.hash(Failure.Type.TASK_FAILED, task.getUuid()));
      task.skipped();
      if (activeTask == task) {
        activeTask = null;
      }
      resume();
    } else {
      String msg = String.format("Impossible to skip '%s' on '%s' because the task is not failed", task.getName(),
          task.getMachineId());
      logger.error(msg);
      throw new KaramelException(msg);
    }
  }

  private void runTask(Task task) {
    logger.debug("start running " + task.getId());
    if (!isSucceedTaskHistoryUpdated) {
      logger.debug("updating the task history");
      loadSucceedListFromMachineToMemory();
      logger.debug("the taks history was updated");
      isSucceedTaskHistoryUpdated = true;
    }
    String skipConf = confs.getProperty(Settings.SKIP_EXISTINGTASKS_KEY);
    if (skipConf != null && skipConf.equalsIgnoreCase("true")
        && task.isIdempotent() && succeedTasksHistory.contains(task.getId())) {
      task.exists();
      logger.info(String.format("Task skipped due to idempotency '%s'", task.getId()));
      if (!(task instanceof KillSessionTask)) {
        activeTask = null;
      }
    } else {
      logger.debug(String.format("task '%s' was not found in the task history, running it", task.getId()));
      try {
        task.started();
        List<ShellCommand> commands = task.getCommands();
        logger.debug(String.format("task %s has %d commands to run", task.getId(), commands.size()));
        for (ShellCommand cmd : commands) {
          if (cmd.getStatus() != ShellCommand.Status.DONE) {
            logger.debug(String.format("command to run %s", cmd.getCmdStr()));
            runSshCmd(cmd, task, false);

            if (cmd.getStatus() != ShellCommand.Status.DONE) {
              task.failed(String.format("%s: Command did not complete: %s", machineEntity.getId(),
                  cmd.getCmdStr()));
              break;
            } else {
              try {
                task.collectResults(this);
                if (task instanceof RunRecipeTask) {
                  // If this task is an experiment, try and download the experiment results
                  // In contrast with 'collectResults' - the results will not necessarly be json objects,
                  // they could be anything - but will be stored in a single file in /tmp/cookbook_recipe.out .
                  if (cmd.getCmdStr().contains("experiment") && cmd.getCmdStr().contains("json")) {
                    task.downloadExperimentResults(this);
                  }
                }
              } catch (KaramelException ex) {
                logger.error(String.format("%s: Error in collecting/downloading the results", machineEntity.getId()),
                    ex);
                task.failed(ex.getMessage());
              }
            }
          } else {
            logger.debug(String.format("skiping this command, status is %s", cmd.getStatus().toString()));
          }
        }
        if (task.getStatus() == Status.ONGOING) {
          if (!(task instanceof KillSessionTask)) {
            task.succeed();
            succeedTasksHistory.add(task.uniqueId());
            activeTask = null;
          }
        }
      } catch (Exception ex) {
        logger.debug(String.format("failing the task because of the exception %s", ex.getMessage()), ex);
        task.failed(ex.getMessage());
      }
    }
  }

  private void runSshCmd(ShellCommand shellCommand, Task task, boolean killcommand) {
    logger.debug(String.format("recieved a command to run '%s'", shellCommand.getCmdStr()));
    int numCmdRetries = Settings.SSH_CMD_RETRY_NUM;
    int timeBetweenRetries = Settings.SSH_CMD_RETRY_INTERVALS;
    boolean finished = false;
    Session session = null;

    while (!stopping && !killing && !finished && numCmdRetries > 0) {
      shellCommand.setStatus(ShellCommand.Status.ONGOING);
      try {
        logger.info(String.format("%s: running: %s", machineEntity.getId(), shellCommand.getCmdStr()));

        //there is no harm of retrying to start session several times for running a command
        int numSessionRetries = Settings.SSH_SESSION_RETRY_NUM;
        while (numSessionRetries > 0) {
          try {
            session = client.startSession();
            if (task.isSudoTerminalReqd()) {
              session.allocateDefaultPTY();
            }
            numSessionRetries = -1;
          } catch (ConnectionException | TransportException ex) {
            logger.warn(String.format("%s: Couldn't start ssh session, will retry", machineEntity.getId()), ex);
            numSessionRetries--;
            if (numSessionRetries == -1) {
              logger.error(String.format("%s: Exhasuted retrying to start a ssh session", machineEntity.getId()));
              return;
            }
            //make sure to relese the session in case of exception to avoid to many session leak problem
            if (session != null) {
              try {
                session.close();
              } catch (TransportException | ConnectionException ex2) {
                logger.error(String.format("Couldn't close ssh session to '%s' ", machineEntity.getId()), ex);
              }
            }
            try {
              Thread.sleep(timeBetweenRetries);
            } catch (InterruptedException ex3) {
              if (!stopping && !killing) {
                logger.warn(String.format("%s: Interrupted while waiting to start ssh session. Continuing...",
                    machineEntity.getId()));
              }
            }
          }
        }

        Session.Command cmd = null;
        try {
          String cmdStr = shellCommand.getCmdStr();
          String password = ClusterService.getInstance().getCommonContext().getSudoAccountPassword();
          if (password != null && !password.isEmpty()) {
            cmd = session.exec(cmdStr.replaceAll("%password_hidden%", password));
          } else {
            cmd = session.exec(cmdStr);
          }
          cmd.join(Settings.SSH_CMD_MAX_TIOMEOUT, TimeUnit.MINUTES);
          updateHeartbeat();
          if (cmd.getExitStatus() != 0) {
            shellCommand.setStatus(ShellCommand.Status.FAILED);
            // Retry just in case there was a network problem somewhere on the server side
          } else {
            shellCommand.setStatus(ShellCommand.Status.DONE);
            finished = true;
          }
          SequenceInputStream sequenceInputStream = new SequenceInputStream(cmd.getInputStream(), cmd.getErrorStream());
          LogService.serializeTaskLog(task, machineEntity.getPublicIp(), sequenceInputStream);
        } catch (ConnectionException | TransportException ex) {
          if (!killing
              && getMachineEntity().getGroup().getCluster().getPhase() != ClusterRuntime.ClusterPhases.TERMINATING) {
            logger.error(String.format("%s: Couldn't excecute command", machineEntity.getId()), ex);
          }

          if (killing) {
            logger.info(String.format("Killed '%s' on '%s' successfully...", task.getName(), machineEntity.getId()));
          }
        }

      } finally {
        // Retry if we have a network problem
        numCmdRetries--;
        if (!finished) {
          try {
            Thread.sleep(timeBetweenRetries);
          } catch (InterruptedException ex) {
            if (!stopping && !killing) {
              logger.warn(
                  String.format("%s: Interrupted waiting to retry a command. Continuing...", machineEntity.getId()));
            }
          }
          timeBetweenRetries *= Settings.SSH_CMD_RETRY_SCALE;
        }
        //regardless of sucess or fail we must release the session in each iteration of retrying the command
        if (session != null) {
          try {
            session.close();
          } catch (TransportException | ConnectionException ex) {
            logger.error(String.format("Couldn't close ssh session to '%s' ", machineEntity.getId()), ex);
          }
        }
        killing = false;
      }
    }
  }

  private PasswordFinder getPasswordFinder() {
    return new PasswordFinder() {

      @Override
      public char[] reqPassword(Resource<?> resource) {
        return passphrase.toCharArray();
      }

      @Override
      public boolean shouldRetry(Resource<?> resource) {
        return false;
      }
    };
  }

  private void connect() throws KaramelException {
    if (client == null || !client.isConnected()) {
      isSucceedTaskHistoryUpdated = false;
      try {
        KeyProvider keys;
        client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.setConnectTimeout(Settings.SSH_CONNECTION_TIMEOUT);
        client.setTimeout(Settings.SSH_SESSION_TIMEOUT);
        keys = (passphrase == null || passphrase.isEmpty())
            ? client.loadKeys(serverPrivateKey, serverPubKey, null)
            : client.loadKeys(serverPrivateKey, serverPubKey, getPasswordFinder());

        logger.info(String.format("%s: connecting ...", machineEntity.getId()));

        int numRetries = 3;
        int timeBetweenRetries = 2000;
        float scaleRetryTimeout = 1.0f;
        boolean succeeded = false;
        while (!succeeded && numRetries > 0) {

          try {
            client.connect(machineEntity.getPublicIp(), machineEntity.getSshPort());
          } catch (IOException ex) {
            logger.warn(String.format("%s: Opps!! coudln' t connect :@", machineEntity.getId()));
            if (passphrase != null && passphrase.isEmpty() == false) {
              if (numRetries > 1) {
                logger.warn(String.format("%s: Could be a slow network, will retry. ", machineEntity.getId()));
              } else {
                logger.warn(String.format("%s: Could be a network problem. But if your network is fine,"
                    + "then you have probably entered an incorrect the passphrase for your private key.",
                    machineEntity.getId()));
              }
            }
            logger.debug(ex);
          }
          if (client.isConnected()) {
            succeeded = true;
            logger.info(String.format("%s: Yey!! connected ^-^", machineEntity.getId()));
            machineEntity.getGroup().getCluster().resolveFailure(Failure.hash(Failure.Type.SSH_KEY_NOT_AUTH,
                machineEntity.getPublicIp()));
            client.authPublickey(machineEntity.getSshUser(), keys);
            machineEntity.setLifeStatus(MachineRuntime.LifeStatus.CONNECTED);
            return;
          } else {
            machineEntity.setLifeStatus(MachineRuntime.LifeStatus.UNREACHABLE);
          }

          numRetries--;
          if (!succeeded) {
            try {
              Thread.sleep(timeBetweenRetries);
            } catch (InterruptedException ex) {
              logger.error(String.format(""), ex);
            }
            timeBetweenRetries *= scaleRetryTimeout;
          }
        }

        if (!succeeded) {
          String message = String.format("%s: Exhausted retry for ssh connection, is the port '%d' open?",
              machineEntity.getId(), machineEntity.getSshPort());
          if (passphrase != null && !passphrase.isEmpty()) {
            message += " or is the passphrase for your private key correct?";
          }
          logger.error(message);
        }

      } catch (UserAuthException ex) {
        String message = String.format("%s: Authentication problem using ssh keys.", machineEntity.getId());
        if (passphrase != null && !passphrase.isEmpty()) {
          message = message + " Is the passphrase for your private key correct?";
        }
        KaramelException exp = new KaramelException(message, ex);
        machineEntity.getGroup().getCluster().issueFailure(new Failure(Failure.Type.SSH_KEY_NOT_AUTH,
            machineEntity.getPublicIp(), message));
        throw exp;
      } catch (IOException e) {
        throw new KaramelException(e);
      }
      return;
    }
  }

  public void disconnect() {
    logger.info(String.format("%s: Closing ssh session", machineEntity.getId()));
    try {
      if (client != null && client.isConnected()) {
        client.close();
      }
    } catch (IOException ex) {
    }
  }

  public void ping() throws KaramelException {
    if (lastHeartbeat < System.currentTimeMillis() - Settings.SSH_PING_INTERVAL) {
      if (client != null && client.isConnected()) {
        updateHeartbeat();
      } else {
        connect();
      }
    }
  }

  private void updateHeartbeat() {
    lastHeartbeat = System.currentTimeMillis();
  }

  //ssh machine maintains the list of succeed tasks synced with the remote machine, it downloads it just if the ssh 
  //connection is lost
  private void loadSucceedListFromMachineToMemory() {
    logger.info(String.format("Loading succeeded tasklist from %s", machineEntity.getPublicIp()));
    String clusterName = machineEntity.getGroup().getCluster().getName().toLowerCase();
    String remoteSucceedPath = Settings.REMOTE_SUCCEEDTASKS_PATH(machineEntity.getSshUser());
    String localSucceedPath = Settings.MACHINE_SUCCEEDTASKS_PATH(clusterName, machineEntity.getPublicIp());
    File localFile = new File(localSucceedPath);
    try {
      Files.deleteIfExists(localFile.toPath());
    } catch (IOException ex) {
    }
    try {
      downloadRemoteFile(remoteSucceedPath, localSucceedPath, true);
    } catch (IOException ex) {
      logger.info(String.format("Succeeded tasklist not exist on %s", machineEntity.getPublicIp()));
      //remote file does not exists
    } catch (KaramelException ex) {
      //shoudn't throw this because I am deleting the local file already here
    } finally {
      try {
        String list = IoUtils.readContentFromPath(localSucceedPath);
        String[] items = list.split("\n");
        succeedTasksHistory.clear();
        succeedTasksHistory.addAll(Arrays.asList(items));
      } catch (IOException ex) {
        //local file does not exists, list is considered to be empty
        succeedTasksHistory.clear();
      }
    }
  }

  @Override
  public void downloadRemoteFile(String remoteFilePath, String localFilePath, boolean overwrite)
      throws KaramelException, IOException {

    connect();
    SCPFileTransfer scp = client.newSCPFileTransfer();
    File f = new File(localFilePath);
    f.mkdirs();
    // Don't collect logs of values, just overwrite
    if (f.exists()) {
      if (overwrite) {
        f.delete();
      } else {
        throw new KaramelException(String.format("%s: Local file already exist %s",
            machineEntity.getId(), localFilePath));
      }
    }
    // If the file doesn't exist, it should quickly throw an IOException
    scp.download(remoteFilePath, localFilePath);
  }
}
