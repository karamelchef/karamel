/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.running.model.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import se.kth.karamel.backend.running.model.NodeRunTime;

import java.lang.reflect.Type;

/**
 *
 * @author kamal
 */
public class MachineEntitySerializer implements JsonSerializer<NodeRunTime> {

  @Override
  public JsonElement serialize(NodeRunTime machineEntity, Type type, JsonSerializationContext context) {
    final JsonObject jsonObj = new JsonObject();
    jsonObj.add("machine", context.serialize(machineEntity.getPublicIp()));
    jsonObj.add("life", context.serialize(machineEntity.getLifeStatus().toString()));
    jsonObj.add("tasksStatus", context.serialize(machineEntity.getTasksStatus().toString()));
    jsonObj.add("privateIp", context.serialize(machineEntity.getPrivateIp()));
    jsonObj.add("user", context.serialize(machineEntity.getSshUser()));
    jsonObj.add("port", context.serialize(machineEntity.getSshPort()));
    jsonObj.add("tasks", context.serialize(machineEntity.getTasks()));
    return jsonObj;
  }

}
