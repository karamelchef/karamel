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

  private Baremetal baremetal;

  public Scope() {
  }

  public Scope(Scope scope) {
    this.baremetal = scope.getBaremetal();
  }

  public abstract Object getAttr(String key);

  public Provider getProvider() {
    return baremetal;
  }

  public Baremetal getBaremetal() {
    return baremetal;
  }

  public void setBaremetal(Baremetal baremetal) {
    this.baremetal = baremetal;
  }

  public void validate() throws ValidationException {
    if (baremetal != null) {
      baremetal.validate();
    }
  }
}
