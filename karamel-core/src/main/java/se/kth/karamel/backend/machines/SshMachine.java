/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.machines;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.SequenceInputStream;
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
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import java.util.logging.Level;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import se.kth.karamel.backend.LogService;
import se.kth.karamel.backend.dag.DagParams;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.Failure;
import se.kth.karamel.backend.running.model.tasks.RunRecipeTask;

/**
 *
 * @author kamal
 */
public class SshMachine implements Runnable {

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
  private SshShell shell;
  private Task activeTask;

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
    if (machineEntity.getTasksStatus().ordinal() < MachineRuntime.TasksStatus.PAUSING.ordinal()) {
      machineEntity.setTasksStatus(MachineRuntime.TasksStatus.PAUSING, null, null);
    }
  }

  public void resume() {
    if (machineEntity.getTasksStatus() != MachineRuntime.TasksStatus.FAILED) {
      machineEntity.setTasksStatus(MachineRuntime.TasksStatus.ONGOING, null, null);
    }
  }

  @Override
  public void run() {
    logger.info(String.format("Started SSH_Machine to '%s' d'-'", machineEntity.getId()));
    try {
      while (true && !stopping) {
        try {
          if (machineEntity.getLifeStatus() == MachineRuntime.LifeStatus.CONNECTED
              && machineEntity.getTasksStatus() == MachineRuntime.TasksStatus.ONGOING) {
            try {
              logger.debug("Taking a new task from the queue.");
              if (this.activeTask != null) {
                this.activeTask = taskQueue.take();
              } else {
                logger.debug("Retrying a task that didn't complete on last execution attempt.");
              }
              logger.debug(String.format("Task for execution.. '%s'", activeTask.getName()));
              runTask(this.activeTask);
              this.activeTask = null;
            } catch (InterruptedException ex) {
              if (stopping) {
                logger.info(String.format("Stopping SSH_Machine to '%s'", machineEntity.getId()));
                return;
              } else {
                logger.error("Got interrupted without having recieved stopping signal");
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
                logger.error("Got interrupted without having recieved stopping signal");
              }
            }
          }
        } catch (Exception e) {
          logger.error("", e);
        }
      }
    } finally {
      disconnect();
    }
  }

  public void enqueue(Task task) throws KaramelException {
    logger.debug(String.format("Queuing '%s'", task.toString()));
    try {
      taskQueue.put(task);
      task.queued();
    } catch (InterruptedException ex) {
      String message = String.format("Couldn't queue task '%s' on machine '%s'", task.getName(), machineEntity.getId());
      task.failed(message);
      throw new KaramelException(message, ex);
    }
  }

  private void runTask(Task task) {
    try {
      task.started();
      List<ShellCommand> commands = task.getCommands();

      for (ShellCommand cmd : commands) {
        if (cmd.getStatus() != ShellCommand.Status.DONE) {

          runSshCmd(cmd, task);

          if (cmd.getStatus() != ShellCommand.Status.DONE) {
            task.failed(String.format("Incompleted command '%s", cmd.getCmdStr()));
            break;
          }
        }
      }
      if (task.getStatus() == Status.ONGOING) {
        task.succeed();
      }
    } catch (Exception ex) {
      task.failed(ex.getMessage());
    }
  }

  private void runSshCmd(ShellCommand shellCommand, Task task) {

    int numRetries = 4;
    int timeBetweenRetries = 1000;
    float scaleRetryTimeout = 1.5f;
    boolean finished = false;
    Session session = null;

    while (!finished && numRetries > 0) {
      shellCommand.setStatus(ShellCommand.Status.ONGOING);
      try {
        logger.info(machineEntity.getId() + " => " + shellCommand.getCmdStr());

        session = client.startSession();
        Session.Command cmd = session.exec(shellCommand.getCmdStr());
        cmd.join(60 * 24, TimeUnit.MINUTES);
        updateHeartbeat();
        if (cmd.getExitStatus() != 0) {
          shellCommand.setStatus(ShellCommand.Status.FAILED);
          // Retry just in case there was a network problem somewhere on the server side
        } else {
          shellCommand.setStatus(ShellCommand.Status.DONE);
          finished = true;
          if (task instanceof RunRecipeTask) {
            RunRecipeTask rrt = (RunRecipeTask) task;
            try {
              processReturnValues(rrt.getCookbookName(), rrt.getRecipeName());
            } catch (JsonParseException p) {
              logger.error("Bug in Chef Cookbook - Return JSON was not a valid json document from: "
                  + rrt.getCookbookName() + "::" + rrt.getRecipeCanonicalName());
            } catch (IOException e) {
              logger.error("No return values for: "
                  + rrt.getCookbookName() + "::" + rrt.getRecipeCanonicalName());
            }
          }
        }
        SequenceInputStream sequenceInputStream = new SequenceInputStream(cmd.getInputStream(), cmd.getErrorStream());
        LogService.serializeTaskLog(task, machineEntity.getPublicIp(), sequenceInputStream);
      } catch (ConnectionException | TransportException ex) {
        if (getMachineEntity().getGroup().getCluster().getPhase() != ClusterRuntime.ClusterPhases.PURGING) {
          logger.error(String.format("Couldn't excecute command on client '%s' ", machineEntity.getId()), ex);
        }
      } finally {
        // Retry if we have a network problem
        numRetries--;
        if (!finished) {
          try {
            Thread.sleep(timeBetweenRetries);
          } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SshMachine.class.getName()).log(Level.SEVERE, null, ex);
          }
          timeBetweenRetries *= scaleRetryTimeout;
        }
      }
    }
    if (session != null) {
      try {
        session.close();
      } catch (TransportException | ConnectionException ex) {
        logger.error(String.format("Couldn't close ssh session to '%s' ", machineEntity.getId()), ex);
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

  private boolean connect() throws KaramelException {
    try {
      KeyProvider keys;
      client = new SSHClient();
      client.addHostKeyVerifier(new PromiscuousVerifier());
      client.setConnectTimeout(Settings.SSH_CONNECTION_TIMEOUT);
      client.setTimeout(Settings.SSH_SESSION_TIMEOUT);
      if (passphrase == null) {
        keys = client.loadKeys(serverPrivateKey, serverPubKey, null);
      } else {
        keys = client.loadKeys(serverPrivateKey, serverPubKey, getPasswordFinder());
      }
      logger.info(String.format("connecting to '%s'...", machineEntity.getId()));

      int numRetries = 3;
      int timeBetweenRetries = 2000;
      float scaleRetryTimeout = 1.0f;
      boolean succeeded = false;
      while (!succeeded && numRetries > 0) {

        try {
          client.connect(machineEntity.getPublicIp(), machineEntity.getSshPort());
        } catch (IOException ex) {
          logger.warn(String.format("Opps!! coudln' t connect to '%s' :@", machineEntity.getId()));
          if (passphrase != null) {
            logger.warn(String.format("Is the passphrase for your private key correct?"));
          }
          logger.debug(ex);
        }
        if (client.isConnected()) {
          succeeded = true;
          logger.info(String.format("Yey!! connected to '%s' ^-^", machineEntity.getId()));
          machineEntity.getGroup().getCluster().resolveFailure(Failure.hash(Failure.Type.SSH_KEY_NOT_AUTH, machineEntity.getPublicIp()));
          client.authPublickey(machineEntity.getSshUser(), keys);
          machineEntity.setLifeStatus(MachineRuntime.LifeStatus.CONNECTED);
          return true;
        } else {
          logger.error(String.format("Mehh!! no connection to '%s', is the port '%d' open?", machineEntity.getId(), machineEntity.getSshPort()));
          if (passphrase != null) {
            logger.warn(String.format("Is the passphrase for your private key correct?"));
          }
          machineEntity.setLifeStatus(MachineRuntime.LifeStatus.UNREACHABLE);
//        return false;
        }

        numRetries--;
        if (!succeeded) {
          try {
            Thread.sleep(timeBetweenRetries);
          } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SshMachine.class.getName()).log(Level.SEVERE, null, ex);
          }
          timeBetweenRetries *= scaleRetryTimeout;
        }
      }

    } catch (UserAuthException ex) {
      String message = "Authentication problem using ssh keys.";
      if (passphrase != null) {
        message = message + " Is the passphrase for your private key correct?";
      }
      KaramelException exp = new KaramelException(message, ex);
      machineEntity.getGroup().getCluster().issueFailure(new Failure(Failure.Type.SSH_KEY_NOT_AUTH, machineEntity.getPublicIp(), message));
      throw exp;
    } catch (IOException e) {
      throw new KaramelException(e);
    }
    return false;
  }

  public void disconnect() {
    logger.info(String.format("Closing ssh session to '%s'", machineEntity.getId()));
    try {
      if (client != null && client.isConnected()) {
        client.close();
      }
    } catch (IOException ex) {
    }
  }

  public boolean ping() throws KaramelException {
    if (lastHeartbeat < System.currentTimeMillis() - Settings.SSH_PING_INTERVAL) {
      if (client != null && client.isConnected()) {
        updateHeartbeat();
        return true;
      } else {
        return connect();
      }
    } else {
      return true;
    }
  }

  private void updateHeartbeat() {
    lastHeartbeat = System.currentTimeMillis();
  }

  /**
   * http://unix.stackexchange.com/questions/136165/java-code-to-copy-files-from-one-linux-machine-to-another-linux-machine
   *
   * This method downloads the return values from a chef recipe as a JSON
   * object. It then parses the JSON object and updates a central location for
   * Chef Attributes.
   *
   * @param cookbook Chef solo cookbook name
   * @param recipe Chef solo recipe name
   */
  private synchronized void processReturnValues(String cookbook, String recipe) throws IOException {
    String postfix = "__out.json";
    String recipeSeparator = "__";
    String remoteFile = Settings.SYSTEM_TMP_FOLDER_NAME + File.separator + cookbook + recipeSeparator + recipe + postfix;
    SCPFileTransfer scp = client.newSCPFileTransfer();
    String localResultsFile = Settings.KARAMEL_TMP_PATH + File.separator + cookbook + recipeSeparator + recipe + postfix;
    File f = new File(localResultsFile);
    f.mkdirs();
    // Don't collect logs of values, just overwrite
    if (f.exists()) {
      f.delete();
    }
    // If the file doesn't exist, it should quickly throw an IOException
    scp.download(remoteFile, localResultsFile);
    JsonReader reader = new JsonReader(new FileReader(localResultsFile));
    JsonParser jsonParser = new JsonParser();
    try {
      JsonElement el = jsonParser.parse(reader);
      DagParams.setGlobalParams(el);
    } catch (JsonIOException | JsonSyntaxException ex) {
      logger.error(String.format("Invalid return value as Json object: %s \n %s'", ex.toString(), reader.toString()));
    }
  }
}
