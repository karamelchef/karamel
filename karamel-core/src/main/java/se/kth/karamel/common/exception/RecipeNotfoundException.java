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
public class RecipeNotfoundException extends KaramelException {

  public RecipeNotfoundException() {
  }

  public RecipeNotfoundException(String message) {
    super(message);
  }

  public RecipeNotfoundException(Throwable exception) {
    super(exception);
  }

  public RecipeNotfoundException(String message, Throwable exception) {
    super(message, exception);
  }
}
