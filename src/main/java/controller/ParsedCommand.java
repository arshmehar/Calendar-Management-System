package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents a parsed command entered by the user.
 * Holds all parameters extracted from the input such as subject, date, and calendar names.
 * This class is immutable and created using the Builder pattern.
 * Different command types use different subsets of the available fields.
 */
public class ParsedCommand {

  private final CommandType commandType;

  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final LocalDate startDate;
  private final String description;
  private final String location;
  private final String status;
  private final String context;
  private final boolean isCalendarSystemOperation;
  private final String calendarName;
  private final String calendarTimeZone;

  private final String targetCalendarName;
  private final LocalDateTime targetDateTime;
  private final LocalDate targetDate;
  private final LocalDate intervalStartDate;
  private final LocalDate intervalEndDate;

  private final Set<DayOfWeekAlphabet> weekdays;
  private final Integer occurrences;
  private final LocalDate seriesEndDate;

  private final String propertyToEdit;
  private final String newPropertyValue;

  private final LocalDate queryDate;
  private final LocalDateTime queryStartDateTime;
  private final LocalDateTime queryEndDateTime;

  private final String fileName;

  private final LocalDateTime statusDateTime;

  /**
   * Private constructor that creates a ParsedCommand from a Builder.
   *
   * @param builder the builder containing all command parameters
   */
  private ParsedCommand(Builder builder) {
    this.commandType = builder.commandType;
    this.subject = builder.subject;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.startDate = builder.startDate;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.weekdays = builder.weekdays;
    this.occurrences = builder.occurrences;
    this.seriesEndDate = builder.seriesEndDate;
    this.propertyToEdit = builder.propertyToEdit;
    this.newPropertyValue = builder.newPropertyValue;
    this.queryDate = builder.queryDate;
    this.queryStartDateTime = builder.queryStartDateTime;
    this.queryEndDateTime = builder.queryEndDateTime;
    this.fileName = builder.fileName;
    this.statusDateTime = builder.statusDateTime;
    this.context = builder.context;
    this.isCalendarSystemOperation = builder.isCalendarSystemOperation;
    this.calendarName = builder.calendarName;
    this.calendarTimeZone = builder.calendarTimeZone;
    this.targetCalendarName = builder.targetCalendarName;
    this.targetDateTime = builder.targetDateTime;
    this.targetDate = builder.targetDate;
    this.intervalStartDate = builder.intervalStartDate;
    this.intervalEndDate = builder.intervalEndDate;
  }

  /**
   * Gets the type of command.
   *
   * @return the command type
   */
  public CommandType getCommandType() {
    return commandType;
  }

  /**
   * Gets the event subject.
   *
   * @return the event subject, or null if not set
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the event start date and time.
   *
   * @return the start date and time, or null if not set
   */
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets the event end date and time.
   *
   * @return the end date and time, or null if not set
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Gets the start date for all-day events.
   *
   * @return the start date, or null if not an all-day event
   */
  public LocalDate getStartDate() {
    return startDate;
  }

  /**
   * Gets the event description.
   *
   * @return the event description, or null if not set
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the event location.
   *
   * @return the event location, or null if not set
   */
  public String getLocation() {
    return location;
  }

  /**
   * Gets the event status.
   *
   * @return the event status (PUBLIC or PRIVATE), or null if not set
   */
  public String getStatus() {
    return status;
  }

  /**
   * Gets the weekdays for recurring event series.
   *
   * @return set of weekdays, or null if not a recurring event
   */
  public Set<DayOfWeekAlphabet> getWeekdays() {
    return weekdays;
  }

  /**
   * Gets the number of occurrences for recurring event series.
   *
   * @return the number of occurrences, or null if using end date instead
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * Gets the end date for recurring event series.
   *
   * @return the series end date, or null if using occurrences instead
   */
  public LocalDate getSeriesEndDate() {
    return seriesEndDate;
  }

  /**
   * Gets the property name to edit.
   *
   * @return the property name, or null if not an edit command
   */
  public String getPropertyToEdit() {
    return propertyToEdit;
  }

  /**
   * Gets the new value for the property being edited.
   *
   * @return the new property value, or null if not an edit command
   */
  public String getNewPropertyValue() {
    return newPropertyValue;
  }

  /**
   * Gets the calendar context name.
   *
   * @return the context calendar name, or null if not set
   */
  public String getContext() {
    return context;
  }

  /**
   * Gets the calendar name for calendar operations.
   *
   * @return the calendar name, or null if not a calendar operation
   */
  public String getCalendarName() {
    return calendarName;
  }

  /**
   * Gets the calendar timezone.
   *
   * @return the timezone identifier, or null if not set
   */
  public String getCalendarTimeZone() {
    return calendarTimeZone;
  }

  /**
   * Checks if this is a calendar system operation.
   *
   * @return true if this operates on the calendar system itself, false if on calendar events
   */
  public boolean isCalendarSystemOperation() {
    return isCalendarSystemOperation;
  }

  /**
   * Gets the target calendar name for copy operations.
   *
   * @return the target calendar name, or null if not a copy operation
   */
  public String getTargetCalendarName() {
    return targetCalendarName;
  }

  /**
   * Gets the target date and time for single event copy operations.
   *
   * @return the target date and time, or null if not a single event copy
   */
  public LocalDateTime getTargetDateTime() {
    return targetDateTime;
  }

  /**
   * Gets the target date for bulk copy operations.
   *
   * @return the target date, or null if not a bulk copy operation
   */
  public LocalDate getTargetDate() {
    return targetDate;
  }

  /**
   * Gets the start date of the interval for copyEventsBetween.
   *
   * @return the interval start date, or null if not a between copy operation
   */
  public LocalDate getIntervalStartDate() {
    return intervalStartDate;
  }

  /**
   * Gets the end date of the interval for copyEventsBetween.
   *
   * @return the interval end date, or null if not a between copy operation
   */
  public LocalDate getIntervalEndDate() {
    return intervalEndDate;
  }

  /**
   * Gets the query date for print events on date.
   *
   * @return the query date, or null if not a date query
   */
  public LocalDate getQueryDate() {
    return queryDate;
  }

  /**
   * Gets the query start date and time for print events in range.
   *
   * @return the query start date and time, or null if not a range query
   */
  public LocalDateTime getQueryStartDateTime() {
    return queryStartDateTime;
  }

  /**
   * Gets the query end date and time for print events in range.
   *
   * @return the query end date and time, or null if not a range query
   */
  public LocalDateTime getQueryEndDateTime() {
    return queryEndDateTime;
  }

  /**
   * Gets the file name for export operations.
   *
   * @return the file name, or null if not an export command
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Gets the date and time for status check.
   *
   * @return the status check date and time, or null if not a status command
   */
  public LocalDateTime getStatusDateTime() {
    return statusDateTime;
  }

  /**
   * Checks if this represents an all-day event.
   *
   * @return true if this is an all-day event (has startDate but no startDateTime)
   */
  public boolean isAllDayEvent() {
    return startDate != null && startDateTime == null;
  }

  /**
   * Builder class for constructing ParsedCommand objects using the builder pattern.
   * Provides a fluent interface for setting command parameters.
   */
  public static class Builder {

    private CommandType commandType;

    private String subject;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDate startDate;
    private String description;
    private String location;
    private String status;
    private Set<DayOfWeekAlphabet> weekdays;
    private Integer occurrences;
    private LocalDate seriesEndDate;
    private String propertyToEdit;
    private String newPropertyValue;
    private LocalDate queryDate;
    private LocalDateTime queryStartDateTime;
    private LocalDateTime queryEndDateTime;
    private String fileName;
    private LocalDateTime statusDateTime;
    private String context;
    private boolean isCalendarSystemOperation;
    private String calendarName;
    private String calendarTimeZone;
    public String targetCalendarName;
    public LocalDateTime targetDateTime;
    public LocalDate targetDate;
    public LocalDate intervalStartDate;
    public LocalDate intervalEndDate;

    /**
     * Constructs a new Builder with the specified command type.
     *
     * @param commandType the type of command being built
     */
    public Builder(CommandType commandType) {
      this.commandType = commandType;
    }

    /**
     * Sets the event subject.
     *
     * @param subject the event subject
     * @return this builder
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the event start date and time.
     *
     * @param startDateTime the start date and time
     * @return this builder
     */
    public Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the event end date and time.
     *
     * @param endDateTime the end date and time
     * @return this builder
     */
    public Builder endDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets the start date for all-day events.
     *
     * @param startDate the start date
     * @return this builder
     */
    public Builder startDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    /**
     * Sets the event description.
     *
     * @param description the event description
     * @return this builder
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the event location.
     *
     * @param location the event location
     * @return this builder
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the event status.
     *
     * @param status the event status
     * @return this builder
     */
    public Builder status(String status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the weekdays for recurring event series.
     *
     * @param weekdays set of weekdays
     * @return this builder
     */
    public Builder weekdays(Set<DayOfWeekAlphabet> weekdays) {
      this.weekdays = weekdays;
      return this;
    }

    /**
     * Sets the number of occurrences for recurring event series.
     *
     * @param occurrences the number of occurrences
     * @return this builder
     */
    public Builder occurrences(Integer occurrences) {
      this.occurrences = occurrences;
      return this;
    }

    /**
     * Sets the end date for recurring event series.
     *
     * @param seriesEndDate the series end date
     * @return this builder
     */
    public Builder seriesEndDate(LocalDate seriesEndDate) {
      this.seriesEndDate = seriesEndDate;
      return this;
    }

    /**
     * Sets the property name to edit.
     *
     * @param propertyToEdit the property name
     * @return this builder
     */
    public Builder propertyToEdit(String propertyToEdit) {
      this.propertyToEdit = propertyToEdit;
      return this;
    }

    /**
     * Sets the new value for the property being edited.
     *
     * @param newPropertyValue the new property value
     * @return this builder
     */
    public Builder newPropertyValue(String newPropertyValue) {
      this.newPropertyValue = newPropertyValue;
      return this;
    }

    /**
     * Sets the query date for print events on date.
     *
     * @param queryDate the query date
     * @return this builder
     */
    public Builder queryDate(LocalDate queryDate) {
      this.queryDate = queryDate;
      return this;
    }

    /**
     * Sets the query start date and time for print events in range.
     *
     * @param queryStartDateTime the query start date and time
     * @return this builder
     */
    public Builder queryStartDateTime(LocalDateTime queryStartDateTime) {
      this.queryStartDateTime = queryStartDateTime;
      return this;
    }

    /**
     * Sets the query end date and time for print events in range.
     *
     * @param queryEndDateTime the query end date and time
     * @return this builder
     */
    public Builder queryEndDateTime(LocalDateTime queryEndDateTime) {
      this.queryEndDateTime = queryEndDateTime;
      return this;
    }

    /**
     * Sets the file name for export operations.
     *
     * @param fileName the file name
     * @return this builder
     */
    public Builder fileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    /**
     * Sets the date and time for status check.
     *
     * @param statusDateTime the status check date and time
     * @return this builder
     */
    public Builder statusDateTime(LocalDateTime statusDateTime) {
      this.statusDateTime = statusDateTime;
      return this;
    }

    /**
     * Sets the calendar context name.
     *
     * @param context the context calendar name
     * @return this builder
     */
    public Builder context(String context) {
      this.context = context;
      return this;
    }

    /**
     * Sets whether this is a calendar system operation.
     *
     * @param isCalendarSystemOperation true if calendar system operation
     * @return this builder
     */
    public Builder isCalendarSystemOperation(boolean isCalendarSystemOperation) {
      this.isCalendarSystemOperation = isCalendarSystemOperation;
      return this;
    }

    /**
     * Sets the calendar name for calendar operations.
     *
     * @param calendarName the calendar name
     * @return this builder
     */
    public Builder calendarName(String calendarName) {
      this.calendarName = calendarName;
      return this;
    }

    /**
     * Sets the calendar timezone.
     *
     * @param calendarTimeZone the timezone identifier
     * @return this builder
     */
    public Builder calendarTimeZone(String calendarTimeZone) {
      this.calendarTimeZone = calendarTimeZone;
      return this;
    }

    /**
     * Sets the target calendar name for copy operations.
     *
     * @param targetCalendarName the target calendar name
     * @return this builder
     */
    public Builder targetCalendarName(String targetCalendarName) {
      this.targetCalendarName = targetCalendarName;
      return this;
    }

    /**
     * Sets the target date and time for single event copy operations.
     *
     * @param targetDateTime the target date and time
     * @return this builder
     */
    public Builder targetDateTime(LocalDateTime targetDateTime) {
      this.targetDateTime = targetDateTime;
      return this;
    }

    /**
     * Sets the target date for bulk copy operations.
     *
     * @param targetDate the target date
     * @return this builder
     */
    public Builder targetDate(LocalDate targetDate) {
      this.targetDate = targetDate;
      return this;
    }

    /**
     * Sets the start date of the interval for copyEventsBetween.
     *
     * @param intervalStartDate the interval start date
     * @return this builder
     */
    public Builder intervalStartDate(LocalDate intervalStartDate) {
      this.intervalStartDate = intervalStartDate;
      return this;
    }

    /**
     * Sets the end date of the interval for copyEventsBetween.
     *
     * @param intervalEndDate the interval end date
     * @return this builder
     */
    public Builder intervalEndDate(LocalDate intervalEndDate) {
      this.intervalEndDate = intervalEndDate;
      return this;
    }

    /**
     * Builds and returns a ParsedCommand with the configured parameters.
     *
     * @return a new ParsedCommand instance
     */
    public ParsedCommand build() {
      return new ParsedCommand(this);
    }
  }
}