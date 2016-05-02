package se.kth.karamel.common.exception;

/**
 * Created by Mamut on 2016-01-18.
 */
public class InvalidOcciCredentialsException extends KaramelException {

  public InvalidOcciCredentialsException() {
  }

  public InvalidOcciCredentialsException(String message) {
    super(message);
  }

  public InvalidOcciCredentialsException(Throwable exception) {
    super(exception);
  }

  public InvalidOcciCredentialsException(String message, Throwable exception) {
    super(message, exception);
  }
}
