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
public class KaramelException extends Exception {

  public KaramelException() {
  }

  public KaramelException(String message) {
    super(message);
  }
  
  public KaramelException(Throwable exception) {
    super(exception);
  }
  
  public KaramelException(String message, Throwable exception) {
    super(message, exception);
  } 
}
