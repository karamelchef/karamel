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
public class Vagrant extends Provider {

  @Override
  public Provider cloneMe() {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  @Override
  public Provider applyParentScope(Provider parentScopeProvider) {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  @Override
  public Provider applyDefaults() {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  @Override
  public void validate() throws ValidationException {
    throw new UnsupportedOperationException("Not supported yet."); 
  }
  
}
