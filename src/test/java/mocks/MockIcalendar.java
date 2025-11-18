package mocks;

import controller.DayOfWeekAlphabet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.DuplicateEventException;
import model.Event;
import model.EventNotFoundException;
import model.Icalendar;

/**
 * Mock implementation of Icalendar for testing.
 */
public class MockIcalendar implements Icalendar {
  private final StringBuilder log;
  private final int uniqueCode;
  private final String calendarName;
  private final ZoneId timezone;
  private boolean shouldThrowDuplicate;
  private boolean shouldThrowNotFound;

  /**
   * Constructs a mockIcalendar with log and unique code.
   *
   * @param log        StringBuilder to record all method calls
   * @param uniqueCode predictable value to return from queries
   */
  public MockIcalendar(StringBuilder log, int uniqueCode) {
    this(log, uniqueCode, "MockCalendar", ZoneId.of("America/New_York"));
  }

  /**
   * Constructs a mockIcalendar with custom name and timezone.
   *
   * @param log          StringBuilder to record all method calls
   * @param uniqueCode   predictable value to return from queries
   * @param calendarName name of the calendar
   * @param timezone     timezone of the calendar
   */
  public MockIcalendar(StringBuilder log, int uniqueCode, String calendarName, ZoneId timezone) {
    this.log = log;
    this.uniqueCode = uniqueCode;
    this.calendarName = calendarName;
    this.timezone = timezone;
    this.shouldThrowDuplicate = false;
    this.shouldThrowNotFound = false;
  }

  /**
   * Sets whether to throw duplicate exception.
   *
   * @param shouldThrow true to throw exception
   */
  public void setShouldThrowDuplicate(boolean shouldThrow) {
    this.shouldThrowDuplicate = shouldThrow;
  }

  /**
   * Sets whether to throw not found exception.
   *
   * @param shouldThrow true to throw exception
   */
  public void setShouldThrowNotFound(boolean shouldThrow) {
    this.shouldThrowNotFound = shouldThrow;
  }

  @Override
  public void addEvent(String subject, LocalDateTime startTime, LocalDateTime endTime)
      throws DuplicateEventException {
    log.append("addEvent: ").append(subject).append(" ")
        .append(startTime).append(" ").append(endTime).append(System.lineSeparator());

    if (shouldThrowDuplicate) {
      throw new DuplicateEventException("Mock duplicate event");
    }
  }

  @Override
  public void addEventSeriesOccurrences(String subject, LocalDateTime startTime,
                                        LocalDateTime endTime, int occurrences,
                                        Set<DayOfWeekAlphabet> weekdays)
      throws DuplicateEventException {
    log.append("addEventSeriesOccurrences: ").append(subject).append(" ")
        .append(startTime).append(" ").append(endTime).append(" ")
        .append("occurrences=").append(occurrences).append(" ")
        .append("weekdays=").append(weekdays).append(System.lineSeparator());

    if (shouldThrowDuplicate) {
      throw new DuplicateEventException("Mock duplicate series event");
    }
  }

  @Override
  public void addEventSeriesUntil(String subject, LocalDateTime startTime,
                                  LocalDateTime endTime, LocalDate endDate,
                                  Set<DayOfWeekAlphabet> weekdays)
      throws DuplicateEventException {
    log.append("addEventSeriesUntil: ").append(subject).append(" ")
        .append(startTime).append(" ").append(endTime).append(" ")
        .append("endDate=").append(endDate).append(" ")
        .append("weekdays=").append(weekdays).append(System.lineSeparator());

    if (shouldThrowDuplicate) {
      throw new DuplicateEventException("Mock duplicate series event");
    }
  }

  @Override
  public void editEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                        String property, String newValue)
      throws EventNotFoundException, DuplicateEventException {
    log.append("editEvent: ").append(subject).append(" ")
        .append(startTime).append(" ").append(endTime).append(" ")
        .append("property=").append(property).append(" ")
        .append("newValue=").append(newValue).append(System.lineSeparator());

    if (shouldThrowNotFound) {
      throw new EventNotFoundException("Mock event not found");
    }
    if (shouldThrowDuplicate) {
      throw new DuplicateEventException("Mock duplicate after edit");
    }
  }

  @Override
  public void editEventsFromDate(String subject, LocalDateTime startTime,
                                 String property, String newValue)
      throws EventNotFoundException, DuplicateEventException {
    log.append("editEventsFromDate: ").append(subject).append(" ")
        .append(startTime).append(" ")
        .append("property=").append(property).append(" ")
        .append("newValue=").append(newValue).append(System.lineSeparator());

    if (shouldThrowNotFound) {
      throw new EventNotFoundException("Mock events not found");
    }
    if (shouldThrowDuplicate) {
      throw new DuplicateEventException("Mock duplicate after edit");
    }
  }

  @Override
  public void editAllInSeries(String subject, LocalDateTime startTime,
                              String property, String newValue)
      throws EventNotFoundException, DuplicateEventException {
    log.append("editAllInSeries: ").append(subject).append(" ")
        .append(startTime).append(" ")
        .append("property=").append(property).append(" ")
        .append("newValue=").append(newValue).append(System.lineSeparator());

    if (shouldThrowNotFound) {
      throw new EventNotFoundException("Mock series not found");
    }
    if (shouldThrowDuplicate) {
      throw new DuplicateEventException("Mock duplicate after series edit");
    }
  }

  @Override
  public List<Event> getEventsOn(LocalDate date) {
    log.append("getEventsOn: ").append(date).append(System.lineSeparator());
    return new ArrayList<>();
  }

  @Override
  public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    log.append("getEventsBetween: ").append(start).append(" ").append(end)
        .append(System.lineSeparator());
    return new ArrayList<>();
  }

  @Override
  public boolean isOccupied(LocalDateTime dateTime) {
    log.append("isOccupied: ").append(dateTime).append(System.lineSeparator());
    return uniqueCode % 2 == 0;
  }

  @Override
  public Set<Event> getAllEvents() {
    log.append("getAllEvents").append(System.lineSeparator());
    return new HashSet<>();
  }

  @Override
  public String getCalendarName() {
    log.append("getCalendarName").append(System.lineSeparator());
    return calendarName;
  }

  @Override
  public ZoneId getCalendarTimeZone() {
    log.append("getCalendarTimeZone").append(System.lineSeparator());
    return timezone;
  }

  /**
   * Gets the log of method calls.
   *
   * @return the accumulated log
   */
  public String getLog() {
    return log.toString();
  }

  /**
   * Clears the log.
   */
  public void clearLog() {
    log.setLength(0);
  }
}