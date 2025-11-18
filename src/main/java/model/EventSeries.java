package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a series of recurring calendar events.
 * An event series defines a pattern of events that repeat on specific days of the week,
 * either for a fixed number of occurrences or until a specific end date.
 * All events in a series share the same subject, times, and optional properties,
 * but occur on different dates according to the recurrence pattern.
 */
public class EventSeries {

  private final String seriesId;
  private final String subject;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final LocalDate startDate;
  private final String description;
  private final String location;
  private final Set<DayOfWeek> days;
  private final Integer occurrences;
  private final LocalDate endDate;
  private final EventStatus status;

  /**
   * Private constructor that creates an EventSeries from a builder.
   *
   * @param builder the EventSeriesBuilder containing all series properties
   */
  private EventSeries(EventSeriesBuilder builder) {
    this.subject = builder.subject;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.startDate = builder.startDate;
    this.days = Set.copyOf(builder.days);
    this.seriesId = UUID.randomUUID().toString();

    this.description = builder.description;
    this.location = builder.location;
    this.occurrences = builder.occurrences;
    this.endDate = builder.endDate;
    this.status = builder.status;
  }

  /**
   * Returns a new EventSeriesBuilder for constructing EventSeries objects.
   *
   * @return a new EventSeriesBuilder instance
   */
  public static EventSeriesBuilder getBuilder() {
    return new EventSeriesBuilder();
  }

  /**
   * Generates all individual Event objects based on this series' recurrence pattern.
   * Events are created for each date that matches the specified days of the week,
   * either until the occurrence count is reached or the end date is passed.
   *
   * @return list of generated Event objects
   */
  public List<Event> generateEvents() {
    List<Event> events = new ArrayList<>();
    LocalDate currentDate = startDate;

    if (occurrences != null) {
      int count = 0;

      while (count < occurrences) {
        if (days.contains(currentDate.getDayOfWeek())) {
          LocalDateTime start = LocalDateTime.of(currentDate, startTime);
          LocalDateTime end = LocalDateTime.of(currentDate, endTime);
          Event event = Event.getBuilder()
              .subject(this.subject)
              .start(start)
              .end(end)
              .description(this.description)
              .location(this.location)
              .status(this.status)
              .seriesId(this.seriesId)
              .build();

          events.add(event);
          count++;
        }
        currentDate = currentDate.plusDays(1);
      }
    } else {
      while (!currentDate.isAfter(endDate)) {
        if (days.contains(currentDate.getDayOfWeek())) {
          LocalDateTime start = LocalDateTime.of(currentDate, startTime);
          LocalDateTime end = LocalDateTime.of(currentDate, endTime);
          Event event = Event.getBuilder()
              .subject(this.subject)
              .start(start)
              .end(end)
              .description(this.description)
              .location(this.location)
              .status(this.status)
              .seriesId(this.seriesId)
              .build();

          events.add(event);
        }
        currentDate = currentDate.plusDays(1);
      }
    }
    return events;
  }

  /**
   * Gets the subject of events in this series.
   *
   * @return the event subject
   */
  public String subject() {
    return subject;
  }

  /**
   * Gets the start date of this series.
   *
   * @return the series start date
   */
  public LocalDate startDate() {
    return startDate;
  }

  /**
   * Gets the start time for events in this series.
   *
   * @return the event start time
   */
  public LocalTime getStartTime() {
    return startTime;
  }

  /**
   * Gets the days of the week on which events in this series occur.
   *
   * @return set of days of the week
   */
  public Set<DayOfWeek> days() {
    return days;
  }

  /**
   * Gets the end time for events in this series.
   *
   * @return the event end time
   */
  public LocalTime getEndTime() {
    return endTime;
  }

  /**
   * Gets the end date of this series, if set.
   *
   * @return the series end date, or null if occurrences-based
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Gets the status for events in this series.
   *
   * @return the event status (PUBLIC or PRIVATE), or null if not set
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Gets the description for events in this series.
   *
   * @return the event description, or null if not set
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location for events in this series.
   *
   * @return the event location, or null if not set
   */
  public String getLocation() {
    return location;
  }

  /**
   * Gets the unique identifier for this series.
   *
   * @return the series ID
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Gets the number of occurrences for this series, if set.
   *
   * @return the number of occurrences, or null if date-based
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * Builder class for constructing EventSeries objects.
   * Provides a fluent interface for setting series properties.
   * Either occurrences or endDate must be set, but not both.
   */
  public static class EventSeriesBuilder {
    private String subject;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Set<DayOfWeek> days;

    private String description = null;
    private String location = null;
    private EventStatus status = null;
    private LocalDate endDate = null;
    private Integer occurrences = null;

    /**
     * Sets the subject for events in the series.
     *
     * @param subject the event subject
     * @return this builder
     */
    public EventSeriesBuilder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start time for events in the series.
     *
     * @param startTime the event start time
     * @return this builder
     */
    public EventSeriesBuilder startTime(LocalTime startTime) {
      this.startTime = startTime;
      return this;
    }

    /**
     * Sets the start date of the series.
     *
     * @param startDate the series start date
     * @return this builder
     */
    public EventSeriesBuilder startDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    /**
     * Sets the end time for events in the series.
     *
     * @param endTime the event end time
     * @return this builder
     */
    public EventSeriesBuilder endTime(LocalTime endTime) {
      this.endTime = endTime;
      return this;
    }

    /**
     * Sets the days of the week on which events should occur.
     *
     * @param days set of days of the week
     * @return this builder
     */
    public EventSeriesBuilder days(Set<DayOfWeek> days) {
      this.days = days;
      return this;
    }

    /**
     * Sets the description for events in the series.
     *
     * @param description the event description
     * @return this builder
     */
    public EventSeriesBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location for events in the series.
     *
     * @param location the event location
     * @return this builder
     */
    public EventSeriesBuilder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the status for events in the series.
     *
     * @param status the event status (PUBLIC or PRIVATE)
     * @return this builder
     */
    public EventSeriesBuilder status(EventStatus status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the end date for the series (inclusive).
     * Cannot be used with occurrences.
     *
     * @param endDate the series end date
     * @return this builder
     */
    public EventSeriesBuilder endDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    /**
     * Sets the number of occurrences for the series.
     * Cannot be used with endDate.
     *
     * @param occurrences the number of events to generate
     * @return this builder
     */
    public EventSeriesBuilder occurrences(Integer occurrences) {
      this.occurrences = occurrences;
      return this;
    }

    /**
     * Private constructor to enforce use of getBuilder() factory method.
     */
    private EventSeriesBuilder() {
    }

    /**
     * Builds and returns an EventSeries with the configured properties.
     * Validates that all required fields are set and that constraints are met.
     *
     * @return a new EventSeries instance
     * @throws IllegalStateException if required fields are missing or invalid
     */
    public EventSeries build() {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalStateException("subject not provided");
      }

      if (startDate == null) {
        throw new IllegalStateException("start date not provided");
      }

      if (startTime == null) {
        throw new IllegalStateException("start time not provided");
      }

      if (endTime == null) {
        throw new IllegalStateException("end time not provided");
      }

      if (days == null) {
        throw new IllegalStateException("days not provided");
      }

      if (days.isEmpty()) {
        throw new IllegalStateException("should provide atleast one day");
      }

      if (endDate != null && endDate.isBefore(startDate)) {
        throw new IllegalStateException("end date is before start date");
      }

      if (endTime.isBefore(startTime)) {
        throw new IllegalStateException("end time is before start time on same day");
      }

      if (endTime.equals(startTime)) {
        throw new IllegalStateException("End time cannot equal start time");
      }

      if (occurrences != null && occurrences <= 0) {
        throw new IllegalStateException("occurrences must be greater than 0");
      }

      if (endDate != null && occurrences != null) {
        throw new IllegalStateException("Cannot set both endDate and occurrences");
      }

      if (endDate == null && occurrences == null) {
        throw new IllegalStateException("endDate and occurrences are both null");
      }

      return new EventSeries(this);
    }
  }
}