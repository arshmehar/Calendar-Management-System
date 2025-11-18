package model;

/**
 * Exception thrown when an event cannot be found in the calendar.
 * This is a runtime exception that indicates an attempt to access or modify
 * an event that does not exist in the calendar system.
 */
public class EventNotFoundException extends RuntimeException {

  /**
   * Constructs a new EventNotFoundException with the specified detail message.
   * The message is also printed to standard output.
   *
   * @param message the detail message explaining why the event was not found
   */
  public EventNotFoundException(String message) {
    super(message);
  }
}