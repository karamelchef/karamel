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
public class TablespoonNotfoundException extends KaramelException {
  
  public TablespoonNotfoundException() {
  }

  public TablespoonNotfoundException(String message) {
    super(message);
  }

  public TablespoonNotfoundException(Throwable exception) {
    super(exception);
  }

  public TablespoonNotfoundException(String message, Throwable exception) {
    super(message, exception);
  }
}
