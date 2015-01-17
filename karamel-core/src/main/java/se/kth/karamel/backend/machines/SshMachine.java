/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.machines;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.running.model.MachineEntity;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.backend.running.model.tasks.Task;
import se.kth.karamel.backend.running.model.tasks.Task.Status;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class SshMachine implements Runnable {

  private static final Logger logger = Logger.getLogger(SshMachine.class);
  private final MachineEntity machineEntity;
  private final String serverPubKey;
  private final String serverPrivateKey;
  private SSHClient client;
  private long lastHeartbeat = 0;
  private final BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(Settings.MACHINES_TASKQUEUE_SIZE);

  public SshMachine(MachineEntity machineEntity, String serverPubKey, String serverPrivateKey) {
    this.machineEntity = machineEntity;
    this.serverPubKey = serverPubKey;
    this.serverPrivateKey = serverPrivateKey;
  }

  @Override
  public void run() {
    logger.debug("Started to run tasks.. ");
    while (true) {

      if (machineEntity.getLifeStatus() == MachineEntity.LifeStatus.CONNECTED
              && machineEntity.getTasksStatus() == MachineEntity.TasksStatus.ONGOING) {
        try {
          logger.debug("Going to take a task from the queue");
          Task task = taskQueue.take();
          logger.debug(String.format("Task was taken from the queue.. '%s'", task.getName()));
          runTask(task);
        } catch (InterruptedException ex) {
          logger.error("", ex);
        } catch (KaramelException ex) {
          machineEntity.setTasksStatus(MachineEntity.TasksStatus.FAILED);
          logger.error("", ex);
        }
      } else {
        try {
//          logger.debug(String.format("'%s' sleeps shorty till state becomes ready for running taks Zzz..", machineEntity.getId()));
          Thread.sleep(Settings.MACHINE_TASKRUNNER_BUSYWAITING_INTERVALS);
        } catch (InterruptedException ex) {
          logger.error("", ex);
        }
      }
    }
  }

  public void enqueue(Task task) throws KaramelException {
    logger.debug(String.format("Queuing '%s'", task.toString()));
    try {
      task.setStatus(Status.READY);
      taskQueue.put(task);
    } catch (InterruptedException ex) {
      task.setStatus(Status.FAILED);
      throw new KaramelException(String.format("Couldn't queue task '%s' on machine '%s'", task.getName(), machineEntity.getId()), ex);
    }
  }

  private synchronized void runTask(Task task) throws KaramelException {
    try {
      task.setStatus(Status.ONGOING);
      List<ShellCommand> commands = task.getCommands();

      for (ShellCommand cmd : commands) {
        if (cmd.getStatus() != ShellCommand.Status.DONE) {
          runSshCmd(cmd);
          if (cmd.getStatus() != ShellCommand.Status.DONE) {
            task.setStatus(Status.FAILED);
            break;
          }
        }
      }
      if (task.getStatus() == Status.ONGOING) {
        task.setStatus(Status.DONE);
      }
    } catch (Exception ex) {
      task.setStatus(Status.FAILED);
      machineEntity.setTasksStatus(MachineEntity.TasksStatus.FAILED);
      throw new KaramelException(ex);
    }
  }

  private synchronized void runSshCmd(ShellCommand shellCommand) {
    shellCommand.setStatus(ShellCommand.Status.ONGOING);
    Session session = null;
    try {
      logger.info(machineEntity.getId() + " => " + shellCommand.getCmdStr());
        
      session = client.startSession();
      Session.Command cmd = session.exec(shellCommand.getCmdStr());
      cmd.join(30, TimeUnit.MINUTES);
      updateHeartbeat();
      if (cmd.getExitStatus() != 0) {
        shellCommand.setStatus(ShellCommand.Status.FAILED);
      } else {
        shellCommand.setStatus(ShellCommand.Status.DONE);
      }
      try {
        shellCommand.appendLog(IOUtils.readFully(cmd.getErrorStream()).toString());
      } catch (IOException ex) {
        logger.error("", ex);
      }

    } catch (ConnectionException | TransportException ex) {
      logger.error(String.format("Couldn't excecute command on client '%s' ", machineEntity.getId()), ex);
    } finally {
      if (session != null) {
        try {
          session.close();
        } catch (TransportException | ConnectionException ex) {
          logger.error(String.format("Couldn't close ssh session to '%s' ", machineEntity.getId()), ex);
        }
      }
    }
  }

  private synchronized boolean connect() throws KaramelException {
    try {
      KeyProvider keys = null;
      client = new SSHClient();
      client.addHostKeyVerifier(new PromiscuousVerifier());
      keys = client.loadKeys(serverPrivateKey, serverPubKey, null);
      logger.info(String.format("connecting to '%s'...", machineEntity.getId()));
      try {
        client.connect(machineEntity.getPublicIp(), machineEntity.getSshPort());
      } catch (IOException ex) {
        logger.warn(String.format("Opps!! coudln't connect to '%s' :@", machineEntity.getId()));
        logger.debug(ex);
      }
      if (client.isConnected()) {
        logger.info(String.format("Yey!! connected to '%s' ^-^", machineEntity.getId()));
        client.authPublickey(machineEntity.getSshUser(), keys);
        machineEntity.setLifeStatus(MachineEntity.LifeStatus.CONNECTED);
        return true;
      } else {
        logger.error(String.format("Mehh!! no connection to '%s', is the port '%d' open?", machineEntity.getId(), machineEntity.getSshPort()));
        machineEntity.setLifeStatus(MachineEntity.LifeStatus.UNREACHABLE);
        return false;
      }
    } catch (IOException e) {
      throw new KaramelException(e);
    }
  }

  public synchronized void disconnect() throws KaramelException {
    try {
      client.close();
    } catch (IOException ex) {
      throw new KaramelException(String.format("IOException during closing ssh session to '%s'", machineEntity.getId()), ex);
    }
  }

  public synchronized boolean ping() throws KaramelException {
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
}
