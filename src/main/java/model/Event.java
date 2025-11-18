package model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a calendar event with a subject, start time, end time, and optional details.
 * Events are considered equal if they have the same subject, start time, and end time.
 * All events are immutable once created.
 */
public class Event {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String description;
  private final String location;
  private final EventStatus status;
  private final String seriesId;

  private Event(EventBuilder builder) {
    this.start = builder.start;
    this.end = builder.end;
    this.subject = builder.subject;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.seriesId = builder.seriesId;
  }

  /**
   * Returns a new EventBuilder for constructing Event objects.
   *
   * @return a new EventBuilder instance
   */
  public static EventBuilder getBuilder() {
    return new EventBuilder();
  }

  /**
   * Returns a new EventBuilder initialized with all properties from the given event.
   * This allows creating a copy of an event with modified properties.
   *
   * @param source the event to copy properties from
   * @return a new EventBuilder instance with properties copied from source
   */
  public static EventBuilder getBuilderFrom(Event source) {
    return new EventBuilder()
        .subject(source.subject)
        .start(source.start)
        .end(source.end)
        .description(source.description)
        .location(source.location)
        .status(source.status)
        .seriesId(source.seriesId);
  }


  /**
   * Gets the subject of this event.
   *
   * @return the event subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start date and time of this event.
   *
   * @return the event start date and time
   */
  public LocalDateTime getStart() {
    return start;
  }

  /**
   * Gets the end date and time of this event.
   *
   * @return the event end date and time
   */
  public LocalDateTime getEnd() {
    return end;
  }

  /**
   * Gets the description of this event.
   *
   * @return the event description, or null if not set
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location of this event.
   *
   * @return the event location, or null if not set
   */
  public String getLocation() {
    return location;
  }

  /**
   * Gets the status of this event.
   *
   * @return the event status (PUBLIC or PRIVATE), or null if not set
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Gets the series ID if this event is part of a recurring series.
   *
   * @return the series ID, or null if not part of a series
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Compares this event to another object for equality.
   * Two events are equal if they have the same subject, start time, and end time.
   * Other fields (description, location, status, seriesId) are not considered.
   * Rule: If two objects are equal (according to equals()),
   * they MUST have the same hashCode.
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

    Event event = (Event) o;
    return Objects.equals(subject, event.subject)
        && Objects.equals(start, event.start)
        && Objects.equals(end, event.end);
  }

  /**
   * Returns a hash code value for this event.
   * Hash code is based on subject, start time, and end time only.
   *
   * @return a hash code value for this event
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }

  /**
   * Builder class for constructing Event objects.
   * Provides a fluent interface for setting event properties.
   */
  public static class EventBuilder {
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end = null;
    private String description = null;
    private String location = null;
    private EventStatus status = null;
    private String seriesId = null;

    /**
     * Sets the subject of the event being built.
     *
     * @param subject the event subject
     * @return this builder
     */
    public EventBuilder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start date and time of the event being built.
     *
     * @param start the event start date and time
     * @return this builder
     */
    public EventBuilder start(LocalDateTime start) {
      this.start = start;
      return this;
    }

    /**
     * Sets the end date and time of the event being built.
     *
     * @param end the event end date and time
     * @return this builder
     */
    public EventBuilder end(LocalDateTime end) {
      this.end = end;
      return this;
    }

    /**
     * Sets the description of the event being built.
     *
     * @param description the event description
     * @return this builder
     */
    public EventBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location of the event being built.
     *
     * @param location the event location
     * @return this builder
     */
    public EventBuilder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the status of the event being built.
     *
     * @param status the event status (PUBLIC or PRIVATE)
     * @return this builder
     */
    public EventBuilder status(EventStatus status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the series ID for the event being built.
     *
     * @param seriesId the series ID if event is part of a recurring series
     * @return this builder
     */
    public EventBuilder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Private constructor to enforce use of getBuilder() factory method.
     */
    private EventBuilder() {
    }

    /**
     * Builds and returns an Event with the configured properties.
     * If no end time is provided, creates an all-day event (8 AM to 5 PM).
     *
     * @return a new Event instance
     * @throws IllegalStateException if required fields are missing or invalid
     */
    public Event build() {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalStateException("subject not provided");
      }

      if (start == null) {
        throw new IllegalStateException("start not provided");
      }

      if (end == null) {
        start = LocalDateTime.of(this.start.toLocalDate(), LocalTime.of(8, 0));
        end = LocalDateTime.of(this.start.toLocalDate(), LocalTime.of(17, 0));
      }

      if (end.isBefore(start)) {
        throw new IllegalStateException("end is before start date");
      }

      if (end.equals(start)) {
        throw new IllegalStateException("end date time cant be same as the start date time");
      }
      return new Event(this);
    }
  }
}