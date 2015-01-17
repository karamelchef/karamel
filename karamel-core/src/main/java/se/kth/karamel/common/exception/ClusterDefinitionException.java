/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.exception;

/**
 *
 * @author kamal
 */
public class ClusterDefinitionException extends KaramelException {

  public ClusterDefinitionException() {
  }

  public ClusterDefinitionException(String message) {
    super(message);
  }

  public ClusterDefinitionException(Throwable exception) {
    super(exception);
  }

  public ClusterDefinitionException(String message, Throwable exception) {
    super(message, exception);
  }
}
