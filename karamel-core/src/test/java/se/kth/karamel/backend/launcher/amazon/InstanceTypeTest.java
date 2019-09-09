/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.launcher.amazon;

import se.kth.karamel.common.launcher.amazon.InstanceType;

import java.util.List;

import org.jclouds.ec2.domain.BlockDeviceMapping;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.common.launcher.amazon.StorageDevice;

/**
 * @author kamal
 */
public class InstanceTypeTest {

  @Test
  public void testGetEphemeralDeviceMappings() {
    InstanceType instance = InstanceType.valueByModel("i2.4xlarge");
    StorageDevice[] mappings = instance.getStorageDevices();
    for (int i = 0; i < 4; i++) {
      System.out.println("Device: " + mappings[i].device + " Mount Pt: " + mappings[i].mountPoint);
      Assert.assertTrue(mappings[i].device.equals("/dev/xvd" + (char)('b'+ i ) ));
      Assert.assertTrue(mappings[i].mountPoint.equals("/mnt/ssd" + i));
    }
  }

  @Test
  public void testMappings1() {
    InstanceType instance = InstanceType.valueByModel("z1d.metal");
    StorageDevice[] mappings = instance.getStorageDevices();
    for (int i = 0; i < 2; i++) {
      System.out.println("Device: " + mappings[i].device + " Mount Pt: " + mappings[i].mountPoint);
      Assert.assertTrue(mappings[i].device.equals("/dev/nvme" + i + "n1"));
      Assert.assertTrue(mappings[i].mountPoint.equals("/mnt/nvme_ssd" + i));
    }
  }

  @Test
  public void testMappings2() {
    InstanceType instance = InstanceType.valueByModel("m3.2xlarge");
    StorageDevice[] mappings = instance.getStorageDevices();
    for (int i = 0; i < 2; i++) {
      System.out.println("Device: " + mappings[i].device + " Mount Pt: " + mappings[i].mountPoint);
      Assert.assertTrue(mappings[i].device.equals("/dev/xvd" + (char)('b'+ i ) ));
      Assert.assertTrue(mappings[i].mountPoint.equals("/mnt/ssd" + i));
    }
  }

  @Test
  public void testMappings3() {
    InstanceType instance = InstanceType.valueByModel("m3.xlarge");
    StorageDevice[] mappings = instance.getStorageDevices();
    for (int i = 0; i < 2; i++) {
      System.out.println("Device: " + mappings[i].device + " Mount Pt: " + mappings[i].mountPoint);
      Assert.assertTrue(mappings[i].device.equals("/dev/xvd" + (char)('b'+ i ) ));
      Assert.assertTrue(mappings[i].mountPoint.equals("/mnt/ssd" + i));
    }
  }

  @Test
  public void testMappings4() {
    InstanceType instance = InstanceType.valueByModel("u-9tb1.metal");
    StorageDevice[] mappings = instance.getStorageDevices();
    assert mappings.length == 0;
  }
}
