package se.kth.karamel.common.exception;

public class RecipeParseException extends KaramelException {

  public RecipeParseException() {
  }

  public RecipeParseException(String message) {
    super(message);
  }

  public RecipeParseException(Throwable exception) {
    super(exception);
  }

  public RecipeParseException(String message, Throwable exception) {
    super(message, exception);
  }

}
