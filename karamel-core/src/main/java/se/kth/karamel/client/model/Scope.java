/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model;

import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public abstract class Scope {

  private Ec2 ec2;
  private Vagrant vagrant;
  private Baremetal baremetal;

  public Scope() {
  }

  public Scope(Scope scope) {
    this.ec2 = scope.getEc2();
    this.vagrant = scope.getVagrant();
    this.baremetal = scope.getBaremetal();
  }

  public abstract String getAttr(String key);

  public Provider getProvider() {
    if (ec2 != null) {
      return ec2;
    } else if (vagrant != null) {
      return vagrant;
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

  public void validate() throws ValidationException {
    if (ec2 != null)
      ec2.validate();
    if (baremetal != null)
      baremetal.validate();
    if(vagrant != null)
      vagrant.validate();
  };
}
