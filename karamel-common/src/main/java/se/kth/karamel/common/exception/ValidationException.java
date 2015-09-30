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
public class ValidationException extends ClusterDefinitionException {

  public ValidationException() {
  }

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(Throwable exception) {
    super(exception);
  }

  public ValidationException(String message, Throwable exception) {
    super(message, exception);
  }
}
