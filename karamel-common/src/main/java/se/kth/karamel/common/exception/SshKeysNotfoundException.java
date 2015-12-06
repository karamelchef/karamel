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
public class SshKeysNotfoundException extends KaramelException {

  public SshKeysNotfoundException() {
  }

  public SshKeysNotfoundException(String message) {
    super(message);
  }

  public SshKeysNotfoundException(Throwable exception) {
    super(exception);
  }

  public SshKeysNotfoundException(String message, Throwable exception) {
    super(message, exception);
  }

}
