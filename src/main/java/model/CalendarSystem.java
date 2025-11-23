package model;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the calendar system that manages multiple calendars.
 * Provides operations for creating, retrieving, renaming calendars, and
 * copying events between calendars with timezone conversion support.
 * Calendar names are case-insensitive for ease of lookup.
 */
public class CalendarSystem implements IcalendarSystem {

  private final Map<String, CalendarImp> calendarsByName;
  private final Map<String, String> nameLookup;

  /**
   * Constructs a new empty calendar system.
   * Initializes internal storage for calendars and case-insensitive name lookup.
   */
  public CalendarSystem() {
    this.calendarsByName = new HashMap<>();
    nameLookup = new HashMap<>();
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   * Calendar names are case-insensitive and must be unique.
   *
   * @param name the name of the calendar to create
   * @param timezone the timezone identifier (e.g., "America/New_York", "UTC")
   * @throws DuplicateCalendarException if a calendar with this name already exists
   * @throws InvalidTimezoneException if the timezone identifier is not valid
   */
  @Override
  public void createCalendar(String name, String timezone)
      throws DuplicateCalendarException, InvalidTimezoneException {

    if (nameLookup.containsKey(name.toLowerCase())) {
      throw new DuplicateCalendarException("Calendar already exists: " + name);
    }
    ZoneId zone;

    try {
      zone = ZoneId.of(timezone);
    } catch (DateTimeException e) {
      throw new InvalidTimezoneException(
          "Unsupported timezone: \"" + timezone + "\". " + e.getMessage());
    }

    CalendarImp calendar = new CalendarImp(name, zone);
    nameLookup.put(name.toLowerCase(), name);
    calendarsByName.put(name, calendar);

  }

  /**
   * Retrieves a calendar by name.
   * Lookup is case-insensitive.
   *
   * @param name the name of the calendar to retrieve
   * @return the calendar with the specified name
   * @throws CalendarNotFoundException if no calendar with this name exists
   */
  @Override
  public Icalendar getCalendar(String name) throws CalendarNotFoundException {
    String actualName = nameLookup.get(name.toLowerCase());
    if (actualName == null) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }
    return calendarsByName.get(actualName);
  }

  /**
   * Renames an existing calendar.
   * The new name must not conflict with any existing calendar.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar
   * @throws CalendarNotFoundException if no calendar with oldName exists
   * @throws DuplicateCalendarException if a calendar with newName already exists
   */
  @Override
  public void renameCalendar(String oldName, String newName) throws DuplicateCalendarException {

    String actualName = nameLookup.get(oldName.toLowerCase());
    if (actualName == null) {
      throw new CalendarNotFoundException("Calendar not found: " + oldName);
    }

    if (nameLookup.containsKey(newName.toLowerCase())) {
      throw new DuplicateCalendarException("Calendar already exists: " + newName);
    }

    calendarsByName.get(actualName).setCalendarName(newName);

    CalendarImp oldCalendar = calendarsByName.get(actualName);
    calendarsByName.remove(oldName);
    calendarsByName.put(newName, oldCalendar);
    nameLookup.remove(oldName.toLowerCase());
    nameLookup.put(newName.toLowerCase(), newName);

  }

  /**
   * Changes the timezone of an existing calendar.
   * This does not modify existing event times, only the calendar's timezone setting.
   *
   * @param calendarName the name of the calendar to modify
   * @param newTimezone the new timezone identifier
   * @throws CalendarNotFoundException if no calendar with this name exists
   * @throws InvalidTimezoneException if the timezone identifier is not valid
   */
  @Override
  public void changeTimezone(String calendarName, String newTimezone)
      throws InvalidTimezoneException {
    String actualName = nameLookup.get(calendarName.toLowerCase());
    if (actualName == null) {
      throw new CalendarNotFoundException("Calendar not found: " + calendarName);
    }

    ZoneId newZone;

    try {
      newZone = ZoneId.of(newTimezone);

    } catch (DateTimeException e) {
      throw new InvalidTimezoneException(
          "Unsupported timezone: \"" + newTimezone + "\". " + e.getMessage());
    }


    CalendarImp calendar = calendarsByName.get(actualName);
    ZoneId oldZone = calendar.getCalendarTimeZone();

    if (oldZone.equals(newZone)) {
      return;
    }

    Set<Event> allEvents =  calendar.getAllEvents();

    for (Event event : allEvents) {
      calendar.removeFromIndexes(event);
    }

    calendar.setCalendarTimezone(newZone);

    for (Event event : allEvents) {

      ZonedDateTime oldStart = ZonedDateTime.of(event.getStart(), oldZone);
      ZonedDateTime newStart = oldStart.withZoneSameInstant(newZone);
      LocalDateTime convertedStart = newStart.toLocalDateTime();

      ZonedDateTime oldEnd = ZonedDateTime.of(event.getEnd(), oldZone);
      ZonedDateTime newEnd = oldEnd.withZoneSameInstant(newZone);
      LocalDateTime convertedEnd = newEnd.toLocalDateTime();

      try {

        Event convertedEvent = Event.getBuilderFrom(event)
            .start(convertedStart)
            .end(convertedEnd)
            .build();

        calendar.addToIndexes(convertedEvent);
      } catch (DuplicateEventException e) {
        throw new DuplicateEventException("Event already exists: " + event);
      }
    }
  }

  /**
   * Copies a single event from one calendar to another at a specified target time.
   * The event duration is preserved. If multiple events match the subject and start time,
   * all matching events are copied.
   *
   * @param subject the subject of the event to copy
   * @param startDateTime the start date and time of the event in the source calendar
   * @param targetDateTime the target date and time for the copied event
   * @param sourceCalendarName the name of the source calendar
   * @param targetCalendarName the name of the target calendar
   * @throws CalendarNotFoundException if either calendar does not exist
   * @throws EventNotFoundException if no matching event is found in the source calendar
   * @throws DuplicateEventException if the copied event conflicts with an existing event
   */
  @Override
  public void copyEvent(String subject, LocalDateTime startDateTime, LocalDateTime targetDateTime,
                        String sourceCalendarName, String targetCalendarName)
      throws CalendarNotFoundException, EventNotFoundException {

    CalendarImp sourceCalendar = calendarsByName.get(sourceCalendarName);
    CalendarImp targetCalendar = calendarsByName.get(targetCalendarName);

    if (targetCalendar == null) {
      throw new CalendarNotFoundException("Target calendar not found: " + targetCalendarName);
    }

    if (sourceCalendar == null) {
      throw new CalendarNotFoundException("Source calendar not found: " + sourceCalendarName);
    }

    List<Event> matchedEvents = sourceCalendar.findEventsBySubjectStart(subject, startDateTime);

    for (Event event : matchedEvents) {

      Duration eventDuration = Duration.between(
          event.getStart(),
          event.getEnd()
      );


      LocalDateTime targetEnd = targetDateTime.plus(eventDuration);


      targetCalendar.copyEventToCalendar(event, targetDateTime, targetEnd);
    }

  }

  /**
   * Copies all events occurring on a specific date from one calendar to another.
   * Events are copied to the corresponding target date with timezone conversion applied.
   * The conversion adjusts event times based on the timezone difference between calendars.
   *
   * @param sourceDate the date to copy events from
   * @param targetDate the date to copy events to
   * @param sourceCalendarName the name of the source calendar
   * @param targetCalendarName the name of the target calendar
   * @throws CalendarNotFoundException if either calendar does not exist
   * @throws DuplicateEventException if any copied event conflicts with existing events
   */
  @Override
  public void copyEventsOn(LocalDate sourceDate, LocalDate targetDate, String sourceCalendarName,
                           String targetCalendarName) {

    CalendarImp sourceCalendar = calendarsByName.get(sourceCalendarName);
    CalendarImp targetCalendar = calendarsByName.get(targetCalendarName);

    if (targetCalendar == null) {
      throw new CalendarNotFoundException("Target calendar not found: " + targetCalendarName);
    }

    if (sourceCalendar == null) {
      throw new CalendarNotFoundException("Source calendar not found: " + sourceCalendarName);
    }

    List<Event> events = sourceCalendar.getEventsOn(sourceDate);

    for (Event event : events) {
      LocalTime startTime = event.getStart().toLocalTime();
      LocalTime endTime = event.getEnd().toLocalTime();

      LocalDateTime sourceStart = LocalDateTime.of(sourceDate, startTime);
      LocalDateTime sourceEnd = LocalDateTime.of(sourceDate, endTime);

      ZonedDateTime sourceStartZone =
          ZonedDateTime.of(sourceStart, sourceCalendar.getCalendarTimeZone());
      ZonedDateTime sourceEndZone =
          ZonedDateTime.of(sourceEnd, sourceCalendar.getCalendarTimeZone());

      ZonedDateTime targetStartZone =
          sourceStartZone.withZoneSameInstant(targetCalendar.getCalendarTimeZone());
      ZonedDateTime targetEndZone =
          sourceEndZone.withZoneSameInstant(targetCalendar.getCalendarTimeZone());

      LocalTime targetStartTime = targetStartZone.toLocalTime();
      LocalTime targetEndTime = targetEndZone.toLocalTime();

      LocalDateTime targetStart = LocalDateTime.of(targetDate, targetStartTime);
      LocalDateTime targetEnd = LocalDateTime.of(targetDate, targetEndTime);

      targetCalendar.copyEventToCalendar(event, targetStart, targetEnd);
    }

  }

  /**
   * Copies all events within a date range from one calendar to another.
   * The relative positioning of events within the range is preserved.
   * Timezone conversion is applied to all event times based on the calendar timezones.
   * Handles cases where timezone conversion causes end times to roll over to the next day.
   *
   * @param sourceStartDate the start of the source date range (inclusive)
   * @param sourceEndDate the end of the source date range (inclusive)
   * @param targetStartDate the start date in the target calendar
   * @param sourceCalendarName the name of the source calendar
   * @param targetCalendarName the name of the target calendar
   * @throws CalendarNotFoundException if either calendar does not exist
   * @throws DuplicateEventException if any copied event conflicts with existing events
   */
  @Override
  public void copyEventsBetween(LocalDate sourceStartDate,
                                LocalDate sourceEndDate,
                                LocalDate targetStartDate,
                                String sourceCalendarName,
                                String targetCalendarName) {

    CalendarImp sourceCalendar = calendarsByName.get(sourceCalendarName);
    CalendarImp targetCalendar = calendarsByName.get(targetCalendarName);

    if (sourceCalendar == null) {
      throw new CalendarNotFoundException("Source calendar not found: " + sourceCalendarName);
    }
    if (targetCalendar == null) {
      throw new CalendarNotFoundException("Target calendar not found: " + targetCalendarName);
    }

    List<Event> eventsInRange = sourceCalendar.getEventsBetween(sourceStartDate.atStartOfDay(),
        sourceEndDate.atStartOfDay());

    for (Event event : eventsInRange) {
      LocalDate eventDate = event.getStart().toLocalDate();
      long daysFromIntervalStart = ChronoUnit.DAYS.between(sourceStartDate, eventDate);

      LocalDate targetEventDate = targetStartDate.plusDays(daysFromIntervalStart);

      ZonedDateTime sourceStartZone = ZonedDateTime.of(
          event.getStart(),
          sourceCalendar.getCalendarTimeZone()
      );
      ZonedDateTime sourceEndZone = ZonedDateTime.of(
          event.getEnd(),
          sourceCalendar.getCalendarTimeZone()
      );

      ZonedDateTime targetStartZone = sourceStartZone.withZoneSameInstant(
          targetCalendar.getCalendarTimeZone()
      );
      ZonedDateTime targetEndZone = sourceEndZone.withZoneSameInstant(
          targetCalendar.getCalendarTimeZone()
      );

      LocalTime targetStartTime = targetStartZone.toLocalTime();
      LocalTime targetEndTime = targetEndZone.toLocalTime();

      LocalDateTime targetStart = LocalDateTime.of(targetEventDate, targetStartTime);
      LocalDateTime targetEnd = LocalDateTime.of(targetEventDate, targetEndTime);

      if (targetEndTime.isBefore(targetStartTime)) {
        targetEnd = targetEnd.plusDays(1);
      }

      targetCalendar.copyEventToCalendar(event, targetStart, targetEnd);
    }
  }

  /**
   * Retrieves a list of all calendar names managed by the system.
   *
   * @return a list of all available calendar names.
   */
  @Override
  public List<String> getAllCalendarNames() {
    return List.copyOf(calendarsByName.keySet());
  }

}