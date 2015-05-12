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
public class DagConstructionException extends KaramelException {

  public DagConstructionException() {
  }

  public DagConstructionException(String message) {
    super(message);
  }

  public DagConstructionException(Throwable exception) {
    super(exception);
  }

  public DagConstructionException(String message, Throwable exception) {
    super(message, exception);
  }
}
