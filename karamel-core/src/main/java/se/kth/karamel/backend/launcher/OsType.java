/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.launcher;

import se.kth.karamel.common.exception.KaramelException;

/**
 *
 * @author kamal
 */
public enum OsType {

  CENTOS("centos", LinuxFamily.REDHAT),
  FEDORA("fedora", LinuxFamily.REDHAT),
  REDHAT("redhat", LinuxFamily.REDHAT),
  UBUNTU("ubuntu", LinuxFamily.UBUNTU),
  DEBIAN("debian", LinuxFamily.UBUNTU);

  public static enum LinuxFamily {

    REDHAT, UBUNTU
  };

  public final String distro;
  public final LinuxFamily family;

  private OsType(String distro, LinuxFamily family) {
    this.distro = distro;
    this.family = family;
  }

  public static OsType valuebyDestroString(String distro) throws KaramelException {
    for (OsType osType : OsType.values()) {
      if (distro.toLowerCase().contains(osType.distro)) {
        return osType;
      }
    }
    throw new KaramelException("Unrecognized linux distro: " + distro);
  }

}
