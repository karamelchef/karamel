/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.mocking.MockingUtil;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.client.model.json.JsonCluster;
import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public class TextTableTest {

  @Test
  public void testMakeTable() throws IOException, KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/hopshub.yml"), Charsets.UTF_8);
    JsonCluster definition = ClusterDefinitionService.yamlToJsonObject(ymlString);
    ClusterRuntime dummyRuntime = MockingUtil.dummyRuntime(definition);
    List<MachineRuntime> machines = dummyRuntime.getGroups().get(1).getMachines();
    String[] columnNames = {"Machine", "Public IP", "Private IP", "SSH Port", "SSH User", "Life Status", "Task Status"};

    String[][] data = new String[machines.size()][columnNames.length];
    for (int i = 0; i < machines.size(); i++) {
      MachineRuntime machine = machines.get(i);
      data[i][0] = machine.getName();
      data[i][1] = "<a kref='shellconnect " + machine.getPublicIp() + "'>" + machine.getPublicIp() + "</a>";
      data[i][2] = machine.getPrivateIp();
      data[i][3] = machine.getSshPort() + "";
      data[i][4] = machine.getSshUser();
      data[i][5] = machine.getLifeStatus() + "";
      data[i][6] = machine.getTasksStatus() + "";
    }
    String table = TextTable.makeTable(columnNames, 0, data, true);
    System.out.println(table);
    table = TextTable.makeTable(columnNames, 0, data, false);
    System.out.println(table);
  }

  @Test
  public void testRealDataLen() {
    String data = null;
    int len = TextTable.realDataLen(data);
    Assert.assertEquals(0, len);
    data = "";
    len = TextTable.realDataLen(data);
    Assert.assertEquals(0, len);
    data = " ";
    len = TextTable.realDataLen(data);
    Assert.assertEquals(1, len);
    data = "<a kref='shellconnect hopshub1'>hopshub1</a>";
    len = TextTable.realDataLen(data);
    Assert.assertEquals(8, len);
    data = "baba <a kref='shellconnect hopshub1'>hopshub1</a> mkmk<<>>";
    len = TextTable.realDataLen(data);
    Assert.assertEquals(22, len);
    data = "baba <a kref='shellconnect hopshub1'>hopshub1</a> mkmk<<>> <a href='http://www.karamel.io/'>Karamel Website</a> !!'''";
    len = TextTable.realDataLen(data);
    Assert.assertEquals(44, len);
  }
}
