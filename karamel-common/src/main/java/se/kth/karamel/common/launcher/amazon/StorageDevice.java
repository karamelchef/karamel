/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.launcher.amazon;

import se.kth.karamel.common.util.Settings;

/**
 *
 * @author kamal
 */
public enum StorageDevice {

  b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;

  public String mountPoint;

  public String mappingName() {
    return Settings.AWS_STORAGE_MAPPINGNAME_PREFIX + name();
  }

  public String virtualName() {
    return Settings.AWS_STORAGE_VIRTUALNAME_PREFIX + ordinal();
  }

  public String kernelAlias() {
    return Settings.AWS_STORAGE_KERNELALIAS_PREFIX + name();
  }

  public String mountPoint() {
    return Settings.AWS_STORAGE_MOUNTPOINT_PREFIX + (ordinal() + 1);
  }

}
