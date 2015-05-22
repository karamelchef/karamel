package se.kth.karamel.common.exception;

/**
 *
 * @author Hooman
 */
public class InvalidCredentialsException extends KaramelException {

    public InvalidCredentialsException() {
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(Throwable exception) {
        super(exception);
    }

    public InvalidCredentialsException(String message, Throwable exception) {
        super(message, exception);
    }
}
