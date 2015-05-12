/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.tasks;

import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.dag.DagTask;
import se.kth.karamel.backend.dag.DagTaskCallback;
import se.kth.karamel.backend.machines.TaskSubmitter;
import se.kth.karamel.backend.running.model.Failure;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public abstract class Task implements DagTask, TaskCallback {

    private static final Logger logger = Logger.getLogger(Task.class);

    public static enum Status {

        WAITING, READY, ONGOING, DONE, FAILED;
    }
    private Status status = Status.WAITING;
    private final String name;
    private final String machineId;
    protected List<ShellCommand> commands;
    private final MachineRuntime machine;
    private final String uuid;

    private JsonArray results;

    private DagTaskCallback dagCallback;
    private final TaskSubmitter submitter;

    public Task(String name, MachineRuntime machine, TaskSubmitter submitter) {
        this.name = name;
        this.machineId = machine.getId();
        this.machine = machine;
        this.uuid = UUID.randomUUID().toString();
        this.submitter = submitter;
    }

    public Status getStatus() {
        return status;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getName() {
        return name;
    }

    public abstract List<ShellCommand> getCommands() throws IOException;

    public void setCommands(List<ShellCommand> commands) {
        this.commands = commands;
    }

    @Override
    public String toString() {
        return name + " on " + machineId;
    }

    public abstract String uniqueId();

    public MachineRuntime getMachine() {
        return machine;
    }

    public String getUuid() {
        return uuid;
    }

    public JsonArray getResults() {
        return results;
    }

    public void setResults(JsonArray results) {
        this.results = results;
    }

    @Override
    public String dagNodeId() {
        return uniqueId();
    }

    @Override
    public void prepareToStart() {
        try {
            submitter.prepareToStart(this);
        } catch (KaramelException ex) {
            machine.getGroup().getCluster().issueFailure(new Failure(Failure.Type.TASK_FAILED, uuid, ex.getMessage()));
            logger.error("", ex);
            dagCallback.failed(ex.getMessage());
        }
    }

    @Override
    public void submit(DagTaskCallback callback) {
        this.dagCallback = callback;
        try {
            submitter.submitTask(this);
        } catch (KaramelException ex) {
            machine.getGroup().getCluster().issueFailure(new Failure(Failure.Type.TASK_FAILED, uuid, ex.getMessage()));
            logger.error("", ex);
            dagCallback.failed(ex.getMessage());
        }
    }

    @Override
    public void queued() {
        status = Status.READY;
        dagCallback.queued();
    }

    @Override
    public void started() {
        status = Status.ONGOING;
        dagCallback.started();
    }

    @Override
    public void succeed() {
        status = Status.DONE;
        dagCallback.succeed();
    }

    @Override
    public void failed(String reason) {
        this.status = Status.FAILED;
        machine.setTasksStatus(MachineRuntime.TasksStatus.FAILED, uuid, reason);
        dagCallback.failed(reason);
    }

}
