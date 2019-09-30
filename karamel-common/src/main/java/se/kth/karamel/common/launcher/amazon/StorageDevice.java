/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.launcher.amazon;

/**
 *
 * @author kamal
 */

public class StorageDevice {

  public final String mountPoint;
  public final String device;
  public final String vitualName;

  StorageDevice(String dev, String mountPoint, String vitualName){
    this.device = dev;
    this.mountPoint = mountPoint;
    this.vitualName = vitualName;
  }

  public String mappingName() {
//    return Settings.AWS_STORAGE_MAPPINGNAME_PREFIX + name();
    return  mountPoint;
  }

  public String virtualName() {
//    return Settings.AWS_STORAGE_VIRTUALNAME_PREFIX + ordinal();
    return vitualName;
  }

  public String kernelAlias() {
//    return Settings.AWS_STORAGE_KERNELALIAS_PREFIX + name();
    return  device;
  }

  public String mountPoint() {
    return mountPoint;
  }

}
