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
public class NoKaramelizedCookbookException extends KaramelException {

  public NoKaramelizedCookbookException() {
  }

  public NoKaramelizedCookbookException(String message) {
    super(message);
  }

  public NoKaramelizedCookbookException(Throwable exception) {
    super(exception);
  }

  public NoKaramelizedCookbookException(String message, Throwable exception) {
    super(message, exception);
  }

}
