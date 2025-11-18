package model;

/**
 * exception thrown when calendar of given name is not found.
 * due to incorrect details or it doesn't exist yet.
 */
public class CalendarNotFoundException extends RuntimeException {

  /**
   * A message is shown to the user signalling calendar is unfound.
   *
   * @param message the message shown.
   */
  public CalendarNotFoundException(String message) {
    super(message);
  }
}
