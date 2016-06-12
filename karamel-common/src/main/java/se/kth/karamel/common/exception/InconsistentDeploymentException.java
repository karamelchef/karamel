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
public class InconsistentDeploymentException extends ValidationException {

  public InconsistentDeploymentException() {
  }

  public InconsistentDeploymentException(String message) {
    super(message);
  }

  public InconsistentDeploymentException(Throwable exception) {
    super(exception);
  }

  public InconsistentDeploymentException(String message, Throwable exception) {
    super(message, exception);
  }
}
