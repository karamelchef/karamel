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
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.karamel.backend.running.model.tasks.ShellCommand;
import se.kth.karamel.backend.running.model.tasks.Task;

/**
 *
 * @author kamal
 */
public abstract class AbstractTaskSerializer implements JsonSerializer<Task> {

  @Override
  public JsonElement serialize(Task task, Type type, JsonSerializationContext context) {
    final JsonObject jsonObj = new JsonObject();
    jsonObj.add("task", context.serialize(task.getName()));
    jsonObj.add("status", context.serialize(task.getStatus().toString()));
    addExtraFields(jsonObj);
    try {
      List<ShellCommand> commands = task.getCommands();
      jsonObj.add("commands", context.serialize(commands));
    } catch (IOException ex) {
    }

    return jsonObj;
  }
  
  public abstract void addExtraFields(JsonObject object);

}
