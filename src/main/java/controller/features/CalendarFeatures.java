package controller.features;
import controller.DayOfWeekAlphabet;
import controller.IguiController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents a user-triggered feature/action in the GUI.
 * Each feature knows how to gather its required input and execute.
 */
public interface CalendarFeatures {
  // ==================== CALENDAR MANAGEMENT ====================

  /**
   * Create a new calendar.
   */
  void createCalendar(String name, String timezone);

  /**
   * Select/switch to a different calendar.
   */
  void selectCalendar(String calendarName);

  /**
   * Edit calendar name.
   */
  void editCalendarName(String calendarName, String newName);

  /**
   * Edit calendar timezone.
   */
  void editCalendarTimezone(String calendarName, String newTimezone);

  // ==================== EVENT CREATION ====================

  /**
   * Create a single event.
   */
  void createSingleEvent(String subject, LocalDateTime start, LocalDateTime end);

  /**
   * Create an all-day event.
   */
  void createAllDayEvent(String subject, LocalDate date);

  /**
   * Create a recurring event series with N occurrences.
   */
  void createRecurringEventWithOccurrences(String subject,
                                           LocalDateTime start,
                                           LocalDateTime end,
                                           Set<DayOfWeekAlphabet> weekdays,
                                           int occurrences);

  /**
   * Create a recurring event series until a specific date.
   */
  void createRecurringEventUntilDate(String subject,
                                     LocalDateTime start,
                                     LocalDateTime end,
                                     Set<DayOfWeekAlphabet> weekdays,
                                     LocalDate endDate);

  // ==================== EVENT EDITING ====================

  /**
   * Edit a single event.
   */
  void editSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                       String property, String newValue);

  /**
   * Edit events from a specific date onward.
   */
  void editEventsFromDate(String subject, LocalDateTime startDateTime,
                          String property, String newValue);

  /**
   * Edit all events in a series.
   */
  void editAllInSeries(String subject, LocalDateTime anyEventStart,
                       String property, String newValue);

  // ==================== EVENT QUERYING ====================

  /**
   * View events on a specific date.
   */
  void viewEventsOnDate(LocalDate date);

  /**
   * View events from a specific date onwards.
   */
  void viewEventsFromDate(LocalDateTime startDateTime);

  /**
   * Check if user is busy at a specific time.
   */
  void checkStatus(LocalDateTime dateTime);

  // ==================== EVENT COPYING ====================

  /**
   * Copy a single event to another calendar.
   */
  void copySingleEvent(String subject, LocalDateTime sourceDateTime,
                       LocalDateTime targetDateTime,
                       String targetCalendarName);

  /**
   * Copy all events on a specific date to another calendar.
   */
  void copyEventsOnDate(LocalDate sourceDate, LocalDate targetDate,
                        String targetCalendarName);

  /**
   * Copy events between dates to another calendar.
   */
  void copyEventsBetweenDates(LocalDate sourceStartDate, LocalDate sourceEndDate,
                              LocalDate targetStartDate,
                              String targetCalendarName);

  // ==================== EXPORT ====================

  /**
   * Export calendar to CSV.
   */
  void exportToCSV(String filePath);

  /**
   * Export calendar to iCal format.
   */
  void exportToICal(String filePath);

}