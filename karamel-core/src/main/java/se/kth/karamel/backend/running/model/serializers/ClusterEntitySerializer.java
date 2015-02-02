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
import se.kth.karamel.backend.running.model.ClusterEntity;

/**
 *
 * @author kamal
 */
public class ClusterEntitySerializer implements JsonSerializer<ClusterEntity> {

  @Override
  public JsonElement serialize(ClusterEntity clusterEntity, Type type, JsonSerializationContext context) {
    final JsonObject jsonObj = new JsonObject();
    jsonObj.add("cluster", context.serialize(clusterEntity.getName()));
    jsonObj.add("phase", context.serialize(clusterEntity.getPhase().toString()));
    if (clusterEntity.isFailed()) {
      jsonObj.add("failed", context.serialize(clusterEntity.isFailed()));
    }
    if (clusterEntity.isPaused()) {
      jsonObj.add("paused", context.serialize(clusterEntity.isPaused()));
    }
    jsonObj.add("groups", context.serialize(clusterEntity.getGroups()));
    return jsonObj;
  }

}
