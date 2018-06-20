/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.clusterdef;

import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public abstract class Scope {

  private Ec2 ec2;
  private Vagrant vagrant;
  private Baremetal baremetal;
  private Gce gce;
  private Nova nova;
  private Occi occi;

  public Scope() {
  }

  public Scope(Scope scope) {
    this.ec2 = scope.getEc2();
    this.vagrant = scope.getVagrant();
    this.baremetal = scope.getBaremetal();
    this.gce = scope.getGce();
    this.nova = scope.getNova();
    this.occi = scope.getOcci();
  }

  public abstract Object getAttr(String key);

  public Provider getProvider() {
    if (ec2 != null) {
      return ec2;
    } else if (gce != null) {
      return gce;
    } else if (vagrant != null) {
      return vagrant;
    } else if(nova != null) {
      return nova;
    } else if(occi != null) {
      return occi;
    } else {
      return baremetal;
    }
  }

  public Baremetal getBaremetal() {
    return baremetal;
  }

  public void setBaremetal(Baremetal baremetal) {
    this.baremetal = baremetal;
  }

  public Ec2 getEc2() {
    return ec2;
  }

  public void setEc2(Ec2 ec2) {
    this.ec2 = ec2;
  }

  public Vagrant getVagrant() {
    return vagrant;
  }

  public void setVagrant(Vagrant vagrant) {
    this.vagrant = vagrant;
  }

  public Gce getGce() {
    return gce;
  }

  public void setGce(Gce gce) {
    this.gce = gce;
  }
  
  public Occi getOcci() {
    return occi;
  }
  
  public void setOcci(Occi occi) {
    this.occi = occi;
  }

  public Nova getNova() {return nova;}
  
  public void setNova(Nova nova) {
    this.nova = nova;
  }

  public void validate() throws ValidationException {
    if (ec2 != null) {
      ec2.validate();
    }
    if (baremetal != null) {
      baremetal.validate();
    }
    if (gce != null) {
      gce.validate();
    }
    if (vagrant != null) {
      vagrant.validate();
    }
    if(nova != null) {
      nova.validate();
    }
    if(occi != null) {
      occi.validate();
    }
  }
;
}
