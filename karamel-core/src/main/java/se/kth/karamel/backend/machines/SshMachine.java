/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.machines;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import java.util.concurrent.ConcurrentHashMap;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import se.kth.karamel.backend.LogService;
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
    private SSHClient client;
    private long lastHeartbeat = 0;
    private final BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(Settings.MACHINES_TASKQUEUE_SIZE);
    private boolean stoping = false;
    private SshShell shell;
    private final ConcurrentHashMap<RunRecipeTask, JsonArray> resultsMap = new ConcurrentHashMap<>();

    public SshMachine(MachineRuntime machineEntity, String serverPubKey, String serverPrivateKey) {
        this.machineEntity = machineEntity;
        this.serverPubKey = serverPubKey;
        this.serverPrivateKey = serverPrivateKey;
        this.shell = new SshShell(serverPrivateKey, serverPubKey, machineEntity.getPublicIp(), machineEntity.getSshUser(), machineEntity.getSshPort());
    }

    public MachineRuntime getMachineEntity() {
        return machineEntity;
    }

    public SshShell getShell() {
        return shell;
    }

    public void setStoping(boolean stoping) {
        this.stoping = stoping;
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
            while (true && !stoping) {

                if (machineEntity.getLifeStatus() == MachineRuntime.LifeStatus.CONNECTED
                        && machineEntity.getTasksStatus() == MachineRuntime.TasksStatus.ONGOING) {
                    Task task = null;
                    try {
                        logger.debug("Going to take a task from the queue");
                        task = taskQueue.take();
                        logger.debug(String.format("Task was taken from the queue.. '%s'", task.getName()));
                              JsonArray res = runTask(task);
                              if (res != null) {
                                  RunRecipeTask rrt = (RunRecipeTask) task;
                                  // TODO - how to pass on return values to update the dag....
                              }
                    } catch (InterruptedException ex) {
                        if (stoping) {
                            logger.info(String.format("Stopping SSH_Machine to '%s'", machineEntity.getId()));
                            return;
                        } else {
                            logger.error("", ex);
                        }
                    } catch (KaramelException ex) {
                        machineEntity.setTasksStatus(MachineRuntime.TasksStatus.FAILED, (task != null) ? task.getUuid() : null, ex.getMessage());
                        logger.error("", ex);
                    }
                } else {
                    if (machineEntity.getTasksStatus() == MachineRuntime.TasksStatus.PAUSING) {
                        machineEntity.setTasksStatus(MachineRuntime.TasksStatus.PAUSED, null, null);
                    }
                    try {
                        Thread.sleep(Settings.MACHINE_TASKRUNNER_BUSYWAITING_INTERVALS);
                    } catch (InterruptedException ex) {
                        logger.error("", ex);
                    }
                }
            }
        } finally {
            disconnect();
        }
    }

    /**
     *
     * @param rrt
     * @return null if the Map doesn't contain a result for this RunRecipeTask, otherwise a JsonArray
     */
    public JsonArray getRecipeResult(RunRecipeTask rrt) {
        JsonArray res = resultsMap.get(rrt);
        if (res != null) {
            // remove result from Map if found
            resultsMap.remove(rrt);
        }
        return res;
    }

    /**
     * http://unix.stackexchange.com/questions/136165/java-code-to-copy-files-from-one-linux-machine-to-another-linux-machine
     *
     * @param session
     * @param cookbook
     * @param recipe
     */
    private synchronized JsonArray downloadResultsScp(String cookbook, String recipe) throws IOException {
        String remoteFile = "~/" + cookbook + "__" + recipe + ".out";
        SCPFileTransfer scp = client.newSCPFileTransfer();
        String localResultsFile = Settings.KARAMEL_TMP_PATH + File.separator + cookbook + "__" + recipe + ".out";
        File f = new File(localResultsFile);
        // TODO - should move this to some initialization method
        f.mkdirs();
        if (f.exists()) {
            f.delete();
        }
        // TODO: error checking here...
        scp.download(remoteFile, localResultsFile);
        JsonReader reader = new JsonReader(new FileReader(localResultsFile));
        JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(reader).getAsJsonArray();
    }

    public void enqueue(Task task) throws KaramelException {
        logger.debug(String.format("Queuing '%s'", task.toString()));
        try {
            task.setStatus(Status.READY, null);
            taskQueue.put(task);
        } catch (InterruptedException ex) {
            String message = String.format("Couldn't queue task '%s' on machine '%s'", task.getName(), machineEntity.getId());
            task.setStatus(Status.FAILED, message);
            throw new KaramelException(message, ex);
        }
    }

    private synchronized JsonArray runTask(Task task) throws KaramelException {
        JsonArray res = null;
        try {
            task.setStatus(Status.ONGOING, null);
            List<ShellCommand> commands = task.getCommands();

            for (ShellCommand cmd : commands) {
                if (cmd.getStatus() != ShellCommand.Status.DONE) {
                    res = runSshCmd(cmd, task);
                    if (cmd.getStatus() != ShellCommand.Status.DONE) {
                        task.setStatus(Status.FAILED, String.format("Incompleted command '%s", cmd.getCmdStr()));
                        break;
                    }
                }
            }
            if (task.getStatus() == Status.ONGOING) {
                task.setStatus(Status.DONE, null);
            }
        } catch (Exception ex) {
            task.setStatus(Status.FAILED, ex.getMessage());
            throw new KaramelException(ex);
        }
        return res;
    }

    private synchronized JsonArray runSshCmd(ShellCommand shellCommand, Task task) {
        shellCommand.setStatus(ShellCommand.Status.ONGOING);
        Session session = null;
        JsonArray res = null;
        try {
            logger.info(machineEntity.getId() + " => " + shellCommand.getCmdStr());

            session = client.startSession();
            Session.Command cmd = session.exec(shellCommand.getCmdStr());
            cmd.join(60 * 24, TimeUnit.MINUTES);
            updateHeartbeat();
            if (cmd.getExitStatus() != 0) {
                shellCommand.setStatus(ShellCommand.Status.FAILED);
            } else {
                shellCommand.setStatus(ShellCommand.Status.DONE);
                if (task instanceof RunRecipeTask) {
                    RunRecipeTask rrt = (RunRecipeTask) task;
                    try {
                        JsonArray results = downloadResultsScp(rrt.getCookbook(), rrt.getRecipe());
                        rrt.setResults(results);
                        res = results;
                    } catch (JsonParseException p) {
                        logger.error("Bug in Chef Cookbook - Results were not a valid json document: " + rrt.getCookbook() + "::" + rrt.getRecipe());
                        rrt.setResults(null);
                    } catch (IOException e) {
                        logger.error("Possible network problem. No results were able to be downloaded for: " + rrt.getCookbook() + "::" + rrt.getRecipe());
                        rrt.setResults(null);
                    }
                }
            }
            LogService.serializeTaskLogs(task, machineEntity.getPublicIp(), cmd.getInputStream(), cmd.getErrorStream());

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
        return res;
    }

    private synchronized boolean connect() throws KaramelException {
        try {
            KeyProvider keys = null;
            client = new SSHClient();
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.setConnectTimeout(Settings.SSH_CONNECTION_TIMEOUT);
            client.setTimeout(Settings.SSH_SESSION_TIMEOUT);
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
                machineEntity.getGroup().getCluster().resolveFailure(Failure.hash(Failure.Type.SSH_KEY_NOT_AUTH, machineEntity.getPublicIp()));
                client.authPublickey(machineEntity.getSshUser(), keys);
                machineEntity.setLifeStatus(MachineRuntime.LifeStatus.CONNECTED);
                return true;
            } else {
                logger.error(String.format("Mehh!! no connection to '%s', is the port '%d' open?", machineEntity.getId(), machineEntity.getSshPort()));
                machineEntity.setLifeStatus(MachineRuntime.LifeStatus.UNREACHABLE);
                return false;
            }
        } catch (UserAuthException ex) {
            String message = "Issue for using ssh keys, make sure you keypair is not password protected..";
            KaramelException exp = new KaramelException(message, ex);
            machineEntity.getGroup().getCluster().issueFailure(new Failure(Failure.Type.SSH_KEY_NOT_AUTH, machineEntity.getPublicIp(), message));
            throw exp;
        } catch (IOException e) {
            throw new KaramelException(e);
        }
    }

    public synchronized void disconnect() {
        logger.info(String.format("Closing ssh session to '%s'", machineEntity.getId()));
        try {
            client.close();
        } catch (IOException ex) {
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
