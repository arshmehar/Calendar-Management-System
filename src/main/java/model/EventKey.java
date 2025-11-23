package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a unique identifier for calendar events based on their
 * subject, start time, and end time.
 * Two events are considered to have the same key if they have identical
 * subject (case-insensitive), start time, and end time.
 * This class is immutable and uses value-based equality.
 */
public class EventKey {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Constructs an EventKey with the specified subject, start time, and end time.
   * The subject is normalized to lowercase for case-insensitive comparison.
   *
   * @param subject the event subject (will be converted to lowercase)
   * @param start the event start date and time
   * @param end the event end date and time
   */
  public EventKey(String subject, LocalDateTime start, LocalDateTime end) {
    this.subject = subject.toLowerCase();
    this.start = start;
    this.end = end;
  }

  /**
   * Compares this EventKey to another object for equality.
   * Two EventKeys are equal if they have the same subject, start time, and end time.
   *
   * @param o the object to compare to
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventKey eventKey = (EventKey) o;
    return Objects.equals(subject, eventKey.subject)
        && Objects.equals(start, eventKey.start)
        && Objects.equals(end, eventKey.end);
  }

  /**
   * Returns a hash code value for this EventKey.
   * Hash code is based on subject, start time, and end time.
   *
   * @return a hash code value for this EventKey
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }

}