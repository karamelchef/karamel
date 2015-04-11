/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model;

import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class Baremetal extends Provider {

  @Override
  public Baremetal cloneMe() {
    Baremetal baremetal = new Baremetal();
    baremetal.setUsername(Settings.PROVIDER_BAREMETAL_DEFAULT_USERNAME);
    return baremetal;
  }

  public static Baremetal makeDefault() {
    Baremetal baremetal = new Baremetal();
    baremetal.setUsername(Settings.PROVIDER_BAREMETAL_DEFAULT_USERNAME);
    return baremetal;
  }

  @Override
  public Provider applyParentScope(Provider parentScopeProvider) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Provider applyDefaults() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void validate() throws ValidationException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
