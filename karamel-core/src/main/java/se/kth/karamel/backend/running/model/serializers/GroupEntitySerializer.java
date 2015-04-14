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
import java.lang.reflect.Type;
import se.kth.karamel.backend.running.model.GroupRuntime;

/**
 *
 * @author kamal
 */
public class GroupEntitySerializer implements JsonSerializer<GroupRuntime> {

  @Override
  public JsonElement serialize(GroupRuntime groupEntity, Type type, JsonSerializationContext context) {
    final JsonObject jsonObj = new JsonObject();
    jsonObj.add("group", context.serialize(groupEntity.getName()));
    jsonObj.add("phase", context.serialize(groupEntity.getPhase().toString()));
    jsonObj.add("machines", context.serialize(groupEntity.getMachines()));
    return jsonObj;
  }

}
