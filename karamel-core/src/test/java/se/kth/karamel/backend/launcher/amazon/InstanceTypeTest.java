/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.launcher.amazon;

import java.util.List;
import org.jclouds.ec2.domain.BlockDeviceMapping;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author kamal
 */
public class InstanceTypeTest {

  @Test
  public void testGetEphemeralDeviceMappings() {
    InstanceType instance = InstanceType.valueByModel("i2.4xlarge");
    List<BlockDeviceMapping> mappings = instance.getEphemeralDeviceMappings();
    Assert.assertTrue(mappings.size() == 4);
    for (int i = 0; i < 3; i++) {
      Assert.assertTrue(mappings.get(i).getVirtualName().equals("ephemeral" + i));
    }
    Assert.assertTrue(mappings.get(0).getDeviceName().equals("/dev/sdb"));
    Assert.assertTrue(mappings.get(1).getDeviceName().equals("/dev/sdc"));
    Assert.assertTrue(mappings.get(2).getDeviceName().equals("/dev/sdd"));
    Assert.assertTrue(mappings.get(3).getDeviceName().equals("/dev/sde"));

  }
}
