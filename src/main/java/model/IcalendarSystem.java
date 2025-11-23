package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Represents a calendar system that manages multiple calendars.
 * Provides operations for creating calendars, switching between them,
 * and copying events across calendars with timezone conversion support.
 */
public interface IcalendarSystem {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name the name of the calendar to create.
   * @param timezone the timezone identifier (e.g., "America/New_York", "UTC").
   * @throws DuplicateCalendarException if a calendar with this name already exists.
   * @throws InvalidTimezoneException if the timezone identifier is not valid.
   */
  void createCalendar(String name, String timezone)
      throws DuplicateCalendarException, InvalidTimezoneException;

  /**
   * Retrieves a calendar by name.
   *
   * @param name the name of the calendar to retrieve.
   * @return the calendar with the specified name.
   * @throws CalendarNotFoundException if no calendar with this name exists.
   */
  Icalendar getCalendar(String name) throws CalendarNotFoundException;

  /**
   * Renames an existing calendar.
   *
   * @param oldName the current name of the calendar.
   * @param newName the new name for the calendar.
   * @throws CalendarNotFoundException if no calendar with oldName exists.
   * @throws DuplicateCalendarException if a calendar with newName already exists.
   */
  void renameCalendar(String oldName, String newName) throws DuplicateCalendarException;

  /**
   * Changes the timezone of an existing calendar.
   *
   * @param calendarName the name of the calendar to modify.
   * @param newTimezone the new timezone identifier.
   * @throws CalendarNotFoundException if no calendar with this name exists.
   * @throws InvalidTimezoneException if the timezone identifier is not valid.
   */
  void changeTimezone(String calendarName, String newTimezone);

  /**
   * Copies a single event from one calendar to another at a specified target time.
   * The event duration is preserved, and timezone conversion is applied.
   *
   * @param subject the subject of the event to copy.
   * @param sourceDateTime the start date and time of the event in the source calendar.
   * @param targetDateTime the target date and time for the copied event.
   * @param sourceCalendarName the name of the source calendar.
   * @param targetCalendarName the name of the target calendar.
   * @throws CalendarNotFoundException if either calendar does not exist.
   * @throws EventNotFoundException if no matching event is found in the source calendar.
   * @throws DuplicateEventException if the copied event conflicts with an existing event.
   */
  void copyEvent(String subject, LocalDateTime sourceDateTime, LocalDateTime targetDateTime,
                 String sourceCalendarName, String targetCalendarName);

  /**
   * Copies all events occurring on a specific date from one calendar to another.
   * Events are copied to the corresponding target date with timezone conversion applied.
   *
   * @param sourceDate the date to copy events from.
   * @param targetDate the date to copy events to.
   * @param sourceCalendarName the name of the source calendar.
   * @param targetCalendarName the name of the target calendar.
   * @throws CalendarNotFoundException if either calendar does not exist.
   * @throws DuplicateEventException if any copied event conflicts with existing events.
   */
  void copyEventsOn(LocalDate sourceDate, LocalDate targetDate, String sourceCalendarName,
                    String targetCalendarName);

  /**
   * Copies all events within a date range from one calendar to another.
   * Timezone conversion is applied to all event times.
   *
   * @param sourceStartDate the start of the source date range (inclusive).
   * @param sourceEndDate the end of the source date range (inclusive).
   * @param targetStartDate the start date in the target calendar.
   * @param sourceCalendarName the name of the source calendar.
   * @param targetCalendarName the name of the target calendar.
   * @throws CalendarNotFoundException if either calendar does not exist.
   * @throws DuplicateEventException if any copied event conflicts with existing events.
   */
  void copyEventsBetween(LocalDate sourceStartDate,
                         LocalDate sourceEndDate,
                         LocalDate targetStartDate,
                         String sourceCalendarName,
                         String targetCalendarName);

  /**
   * Retrieves a list of all calendar names managed by the system.
   *
   * @return a list of all available calendar names.
   */
  List<String> getAllCalendarNames();
}