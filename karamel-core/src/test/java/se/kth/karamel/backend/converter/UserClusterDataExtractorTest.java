/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.converter;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.dag.Dag;
import se.kth.karamel.backend.running.model.ClusterEntity;
import se.kth.karamel.backend.running.model.GroupEntity;
import se.kth.karamel.backend.running.model.MachineEntity;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.client.model.json.JsonGroup;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class UserClusterDataExtractorTest {

//  @Test
  public void clusterLinksTest() throws IOException, KaramelException {
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/hopshub.yml"), Charsets.UTF_8);
    JsonCluster json = ClusterDefinitionService.yamlToJsonObject(ymlString);
    String links = UserClusterDataExtractor.clusterLinks(json, null);
    System.out.println(links);
  }
//  @Test

  public void dagTest() throws IOException, KaramelException {
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/spark.yml"), Charsets.UTF_8);
    JsonCluster json = ClusterDefinitionService.yamlToJsonObject(ymlString);
    ClusterEntity entity = new ClusterEntity(json);
    int gn = 0;
    for (JsonGroup g : json.getGroups()) {
      GroupEntity gent = entity.getGroups().get(gn);
      List<MachineEntity> mes = new ArrayList<>();
      for (int i = 0; i < g.getSize(); i++) {
        MachineEntity me = new MachineEntity(gent);
        me.setPublicIp(gent.getName());
        me.setSshUser("" + ++i);
        mes.add(me);
      }
      gent.setMachines(mes);
    }
    Dag dag = UserClusterDataExtractor.getInstallationDag(json, entity, null, null, true);
    System.out.println(dag.d3Json());
  }
}
