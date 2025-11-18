package model;

/**
 * Exception thrown when attempting to add an event that already exists in the calendar.
 * Two events are considered duplicates if they have the same subject, start time, and end time.
 */
public class DuplicateEventException extends RuntimeException {

  /**
   * Constructs a new DuplicateEventException with the specified detail message.
   *
   * @param message the detail message explaining which event is duplicated
   */
  public DuplicateEventException(String message) {
    super("Duplicate event. " + message);
  }
}
