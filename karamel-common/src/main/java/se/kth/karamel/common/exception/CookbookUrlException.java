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
public class CookbookUrlException extends ClusterDefinitionException {

  public CookbookUrlException() {
  }

  public CookbookUrlException(String message) {
    super(message);
  }

  public CookbookUrlException(Throwable exception) {
    super(exception);
  }

  public CookbookUrlException(String message, Throwable exception) {
    super(message, exception);
  }
}
