//package mocks;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import model.CalendarNotFoundException;
//import model.DuplicateCalendarException;
//import model.DuplicateEventException;
//import model.EventNotFoundException;
//import model.Icalendar;
//import model.IcalendarSystem;
//import model.InvalidTimezoneException;
//
///**
// * Mock implementation of IcalendarSystem for testing.
// * Logs all method calls and can be configured to throw exceptions.
// */
//public class MockIcalendarSystem implements IcalendarSystem {
//  private final StringBuilder log;
//  private final MockIcalendar mockCalendar;
//  private boolean shouldThrowDuplicate;
//  private boolean shouldThrowNotFound;
//  private boolean shouldThrowInvalidTimezone;
//  private boolean shouldThrowDuplicateOnCopy;
//  private boolean shouldThrowNotFoundOnCopy;
//  private boolean shouldThrowCalendarNotFoundOnCopy;
//
//
//  /**
//   * Constructs a MockIcalendarSystem.
//   *
//   * @param log StringBuilder to record all method calls
//   * @param mockCalendar the mock calendar to return from getCalendar
//   */
//  public MockIcalendarSystem(StringBuilder log, MockIcalendar mockCalendar) {
//    this.log = log;
//    this.mockCalendar = mockCalendar;
//    this.shouldThrowDuplicate = false;
//    this.shouldThrowNotFound = false;
//    this.shouldThrowInvalidTimezone = false;
//    this.shouldThrowDuplicateOnCopy = false;
//    this.shouldThrowNotFoundOnCopy = false;
//  }
//
//  /**
//   * Sets whether to throw duplicate calendar exception.
//   *
//   * @param shouldThrow true to throw exception
//   */
//  public void setShouldThrowDuplicate(boolean shouldThrow) {
//    this.shouldThrowDuplicate = shouldThrow;
//  }
//
//  /**
//   * Sets whether to throw calendar not found exception.
//   *
//   * @param shouldThrow true to throw exception
//   */
//  public void setShouldThrowNotFound(boolean shouldThrow) {
//    this.shouldThrowNotFound = shouldThrow;
//  }
//
//  /**
//   * Sets whether to throw invalid timezone exception.
//   *
//   * @param shouldThrow true to throw exception
//   */
//  public void setShouldThrowInvalidTimezone(boolean shouldThrow) {
//    this.shouldThrowInvalidTimezone = shouldThrow;
//  }
//
//  /**
//   * Sets whether to throw duplicate event exception during copy operations.
//   *
//   * @param shouldThrow true to throw exception
//   */
//  public void setShouldThrowDuplicateOnCopy(boolean shouldThrow) {
//    this.shouldThrowDuplicateOnCopy = shouldThrow;
//  }
//
//  /**
//   * sets to throw exception when calendar not found while doing copy commands.
//   *
//   * @param shouldThrow true to throw exception
//   */
//  public void setShouldThrowCalendarNotFoundOnCopy(boolean shouldThrow) {
//    this.shouldThrowCalendarNotFoundOnCopy = shouldThrow;
//  }
//
//  /**
//   * Sets whether to throw event not found exception during copy operations.
//   *
//   * @param shouldThrow true to throw exception
//   */
//  public void setShouldThrowNotFoundOnCopy(boolean shouldThrow) {
//    this.shouldThrowNotFoundOnCopy = shouldThrow;
//  }
//
//  @Override
//  public void createCalendar(String name, String timezone)
//      throws DuplicateCalendarException, InvalidTimezoneException {
//    log.append("createCalendar: name=").append(name)
//        .append(" timezone=").append(timezone).append(System.lineSeparator());
//
//    if (shouldThrowDuplicate) {
//      throw new DuplicateCalendarException("Mock duplicate calendar");
//    }
//    if (shouldThrowInvalidTimezone) {
//      throw new InvalidTimezoneException("Mock invalid timezone");
//    }
//  }
//
//  @Override
//  public Icalendar getCalendar(String name) throws CalendarNotFoundException {
//    log.append("getCalendar: ").append(name).append(System.lineSeparator());
//
//    if (shouldThrowNotFound) {
//      throw new CalendarNotFoundException("Mock calendar not found: " + name);
//    }
//
//    return mockCalendar;
//  }
//
//  @Override
//  public void renameCalendar(String oldName, String newName)
//      throws DuplicateCalendarException {
//    log.append("renameCalendar: oldName=").append(oldName)
//        .append(" newName=").append(newName).append(System.lineSeparator());
//
//    if (shouldThrowNotFound) {
//      throw new CalendarNotFoundException("Mock calendar not found: " + oldName);
//    }
//    if (shouldThrowDuplicate) {
//      throw new DuplicateCalendarException("Mock duplicate calendar name");
//    }
//  }
//
//  @Override
//  public void changeTimezone(String calendarName, String newTimezone) {
//    log.append("changeTimezone: calendar=").append(calendarName)
//        .append(" timezone=").append(newTimezone).append(System.lineSeparator());
//
//    if (shouldThrowNotFound) {
//      throw new CalendarNotFoundException("Mock calendar not found: " + calendarName);
//    }
//    if (shouldThrowInvalidTimezone) {
//      throw new InvalidTimezoneException("Mock invalid timezone");
//    }
//  }
//
//  @Override
//  public void copyEvent(String subject, LocalDateTime sourceDateTime,
//                        LocalDateTime targetDateTime, String sourceCalendarName,
//                        String targetCalendarName) {
//    log.append("copyEvent: subject=").append(subject)
//        .append(" from=").append(sourceDateTime)
//        .append(" to=").append(targetDateTime)
//        .append(" sourceCalendar=").append(sourceCalendarName)
//        .append(" targetCalendar=").append(targetCalendarName).append(System.lineSeparator());
//
//    if (shouldThrowNotFoundOnCopy) {
//      throw new EventNotFoundException("Mock event not found");
//    }
//    if (shouldThrowDuplicateOnCopy) {
//      throw new DuplicateEventException("Mock duplicate on copy");
//    }
//  }
//
//  @Override
//  public void copyEventsOn(LocalDate sourceDate, LocalDate targetDate,
//                           String sourceCalendarName, String targetCalendarName) {
//    log.append("copyEventsOn: sourceDate=").append(sourceDate)
//        .append(" targetDate=").append(targetDate)
//        .append(" sourceCalendar=").append(sourceCalendarName)
//        .append(" targetCalendar=").append(targetCalendarName).append(System.lineSeparator());
//
//    if (shouldThrowCalendarNotFoundOnCopy) {
//      throw new CalendarNotFoundException("Mock calendar not found in copy");
//    }
//    if (shouldThrowNotFoundOnCopy) {
//      throw new EventNotFoundException("Mock events not found");
//    }
//    if (shouldThrowDuplicateOnCopy) {
//      throw new DuplicateEventException("Mock duplicate on copy");
//    }
//  }
//
//  @Override
//  public void copyEventsBetween(LocalDate sourceStartDate, LocalDate sourceEndDate,
//                                LocalDate targetStartDate, String sourceCalendarName,
//                                String targetCalendarName) {
//    log.append("copyEventsBetween: sourceStart=").append(sourceStartDate)
//        .append(" sourceEnd=").append(sourceEndDate)
//        .append(" targetStart=").append(targetStartDate)
//        .append(" sourceCalendar=").append(sourceCalendarName)
//        .append(" targetCalendar=").append(targetCalendarName).append(System.lineSeparator());
//
//    if (shouldThrowDuplicateOnCopy) {
//      throw new DuplicateEventException("Mock duplicate on copy");
//    }
//  }
//
//  /**
//   * Gets the log of all method calls.
//   *
//   * @return the accumulated log
//   */
//  public String getLog() {
//    return log.toString();
//  }
//
//  /**
//   * Clears the log.
//   */
//  public void clearLog() {
//    log.setLength(0);
//  }
//}