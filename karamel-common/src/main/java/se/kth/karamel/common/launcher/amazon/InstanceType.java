/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.launcher.amazon;

import java.util.ArrayList;
import java.util.List;
import org.jclouds.ec2.domain.BlockDeviceMapping;

/**
 *
 * @author kamal
 */
public enum InstanceType {

  t2_nano("t2.nano", 1, 0.5, null, null),
  t2_micro("t2.micro", 1, 1, null, null),
  t2_small("t2.small", 1, 2, null, null),
  t2_medium("t2.medium", 2, 4, null, null),
  t2_large("t2.large", 2, 8, null, null),
  m4_large("m4.large", 2, 8, null, null),
  m4_xlarge("m4.xlarge", 4, 16, null, null),
  m4_2xlarge("m4.2xlarge", 8, 32, null, null),
  m4_4xlarge("m4.4xlarge", 16, 64, null, null),
  m4_10xlarge("m4.10xlarge", 40, 160, null, null),
  m3_medium("m3.medium", 1, 3.75, 1, 4),
  m3_large("m3.large", 2, 7.5, 1, 32),
  m3_xlarge("m3.xlarge", 4, 15, 2, 40),
  m3_2xlarge("m3.2xlarge", 8, 30, 2, 80),
  c4_large("c4.large", 2, 3.75, null, null),
  c4_xlarge("c4.xlarge", 4, 7.5, null, null),
  c4_2xlarge("c4.2xlarge", 8, 15, null, null),
  c4_4xlarge("c4.4xlarge", 16, 30, null, null),
  c4_8xlarge("c4.8xlarge", 36, 60, null, null),
  c3_large("c3.large", 2, 3.7, 2, 16),
  c3_xlarge("c3.xlarge", 4, 7.5, 2, 40),
  c3_2xlarge("c3.2xlarge", 8, 15, 2, 80),
  c3_4xlarge("c3.4xlarge", 16, 30, 2, 160),
  c3_8xlarge("c3.8xlarge", 32, 60, 2, 320),
  g2_2xlarge("g2.2xlarge", 8, 15, 1, 160),
  g2_8xlarge("g2.8xlarge", 32, 60, 2, 120),
  r3_large("r3.large", 2, 15.25, 1, 32),
  r3_xlarge("r3.xlarge", 4, 30.5, 1, 80),
  r3_2xlarge("r3.2xlarge", 8, 61, 1, 160),
  r3_4xlarge("r3.4xlarge", 16, 122, 1, 320),
  r3_8xlarge("r3.8xlarge", 32, 244, 2, 320),
  i2_xlarge("i2.xlarge", 4, 30.5, 1, 800),
  i2_2xlarge("i2.2xlarge", 8, 61, 2, 800),
  i2_4xlarge("i2_4xlarge", 16, 122, 4, 800),
  i2_8xlarge("i2.8xlarge", 32, 244, 8, 800),
  d2_xlarge("d2.xlarge", 4, 30.5, 3, 2000),
  d2_2xlarge("d2.2xlarge", 8, 61, 6, 2000),
  d2_4xlarge("d2.4xlarge", 16, 122, 12, 2000),
  d2_8xlarge("d2.8xlarge", 36, 244, 24, 2000),;

  public final String model;
  public final int numVCpu;
  public final double memInGig;
  public final Integer numDisks;
  public final Integer diskSize;

  private InstanceType(String model, int numVCpu, double memInGig, Integer numDisks, Integer diskSize) {
    this.model = model;
    this.numVCpu = numVCpu;
    this.memInGig = memInGig;
    this.numDisks = numDisks;
    this.diskSize = diskSize;
  }

  public static InstanceType valueByModel(String model) {
    return valueOf(model.replaceAll("\\.", "_"));
  }

  public StorageDevice[] getStorageDevices() {
    StorageDevice[] values = StorageDevice.values();
    StorageDevice[] devices = new StorageDevice[numDisks];
    for (int i = 0; i < numDisks; i++) {
      devices[i] = values[i];
    }
    return devices;
  }

  public List<BlockDeviceMapping> getEphemeralDeviceMappings() {
    ArrayList<BlockDeviceMapping> maps = new ArrayList<>();
    if (numDisks != null) {
      for (StorageDevice device : getStorageDevices()) {
        BlockDeviceMapping map
            = new BlockDeviceMapping.MapEphemeralDeviceToDevice(device.mappingName(), device.virtualName());
        maps.add(map);
      }
    }
    return maps;
  }

}
