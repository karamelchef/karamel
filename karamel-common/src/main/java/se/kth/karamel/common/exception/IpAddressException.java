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
public class IpAddressException extends ValidationException {

  public IpAddressException() {
  }

  public IpAddressException(String message) {
    super(message);
  }

  public IpAddressException(Throwable exception) {
    super(exception);
  }

  public IpAddressException(String message, Throwable exception) {
    super(message, exception);
  }
}
