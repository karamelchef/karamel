package se.kth.karamel.common.exception;

/**
 * Created by Alberto on 2015-05-16.
 */
public class InvalidNovaCredentialsException extends KaramelException {

    public InvalidNovaCredentialsException() {
    }

    public InvalidNovaCredentialsException(String message) {
        super(message);
    }

    public InvalidNovaCredentialsException(Throwable exception) {
        super(exception);
    }

    public InvalidNovaCredentialsException(String message, Throwable exception) {
        super(message, exception);
    }
}
