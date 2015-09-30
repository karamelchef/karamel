/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import se.kth.karamel.common.util.IpAddressUtil;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class Baremetal extends Provider {

  private final List<String> ips = new ArrayList<>();
  
  private String sudoPassword="";

  public void setSudoPassword(String sudoPassword) {
    this.sudoPassword = sudoPassword;
  }

  public String getSudoPassword() {
    return sudoPassword;
  }

  public List<String> getIps() {
    return ips;
  }

  public void setIps(List<String> ips) {
    for (String ip : ips) {
      this.ips.add(ip);
    }
  }

  public void setIp(String ip) {
    ips.add(ip);
  }

  public HashSet<String> retriveAllIps() throws ValidationException {
    HashSet<String> indivIps = new HashSet<>();
    for (String iprange : ips) {
      List<String> ips1 = IpAddressUtil.ipRange(iprange);
      for (String ip1 : ips1) {
        if (indivIps.contains(ip1)) {
          throw new ValidationException("ip-address already exist " + ip1);
        }
        indivIps.add(ip1);
      }
    }
    return indivIps;
  }

  public static Baremetal makeDefault() {
    Baremetal baremetal = new Baremetal();
    return baremetal.applyDefaults();
  }

  @Override
  public Baremetal applyDefaults() {
    Baremetal clone = cloneMe();
    if (clone.getUsername() == null) {
      clone.setUsername(Settings.PROVIDER_BAREMETAL_DEFAULT_USERNAME);
    }
    return clone;
  }

  @Override
  public Baremetal cloneMe() {
    Baremetal baremetal = new Baremetal();
    baremetal.setUsername(getUsername());
    baremetal.setIps(ips);
    baremetal.setSudoPassword(getSudoPassword());
    return baremetal;
  }

  @Override
  public Provider applyParentScope(Provider parentScopeProvider) {
    Baremetal clone = cloneMe();
    if (parentScopeProvider instanceof Baremetal) {
      Baremetal parentBm = (Baremetal) parentScopeProvider;
      if (clone.getUsername() == null) {
        clone.setUsername(parentBm.getUsername());
      }
    }
    return clone;
  }

  @Override
  public void validate() throws ValidationException {
    retriveAllIps();
  }
}
