package model;

/**
 * Throws an exception when a calendar already exists
 * checks using name.
 */
public class DuplicateCalendarException extends RuntimeException {

  /**
   * when error is encountered a message is shown to user.
   *
   * @param message the message for the user.
   */
  public DuplicateCalendarException(String message) {
    super(message);
  }
}
