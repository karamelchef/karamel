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
public class MetadataParseException extends KaramelException {

  public MetadataParseException() {
  }

  public MetadataParseException(String message) {
    super(message);
  }

  public MetadataParseException(Throwable exception) {
    super(exception);
  }

  public MetadataParseException(String message, Throwable exception) {
    super(message, exception);
  }

}
