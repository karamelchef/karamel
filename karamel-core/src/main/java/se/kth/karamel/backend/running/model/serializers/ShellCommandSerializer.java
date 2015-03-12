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
import se.kth.karamel.backend.running.model.tasks.ShellCommand;

/**
 *
 * @author kamal
 */
public class ShellCommandSerializer implements JsonSerializer<ShellCommand> {

  @Override
  public JsonElement serialize(ShellCommand cmd, Type type, JsonSerializationContext context) {
    final JsonObject jsonObj = new JsonObject();
    jsonObj.add("status", context.serialize(cmd.getStatus().toString()));
    jsonObj.add("cmdStr", context.serialize(cmd.getCmdStr()));
    return jsonObj;
  }

}
