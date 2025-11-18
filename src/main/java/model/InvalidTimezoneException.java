package model;

/**
 * exception carried out when an invalid timezone is passed.
 */
public class InvalidTimezoneException extends RuntimeException {

  /**
   * throws message along with exception.
   *
   * @param message message to show to the user.
   */
  public InvalidTimezoneException(String message) {
    super(message);
  }
}
