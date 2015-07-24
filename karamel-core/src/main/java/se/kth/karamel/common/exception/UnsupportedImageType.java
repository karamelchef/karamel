package se.kth.karamel.common.exception;

/**
 *
 * @author Hooman
 */
public class UnsupportedImageType extends KaramelException {

  public UnsupportedImageType() {
  }

  public UnsupportedImageType(String message) {
    super(message);
  }

  public UnsupportedImageType(Throwable exception) {
    super(exception);
  }

  public UnsupportedImageType(String message, Throwable exception) {
    super(message, exception);
  }

}
