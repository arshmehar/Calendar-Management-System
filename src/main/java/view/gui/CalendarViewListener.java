// view/gui/CalendarViewListener.java
package view.gui;

import java.time.LocalDate;

/**
 * Listener interface for GUI events.
 * Controller implements this to handle user actions.
 */
public interface CalendarViewListener {
  /**
   * Called when user wants to create a new calendar.
   */
  void onCreateCalendarRequested(String name, String timezone);

  /**
   * Called when user selects a different calendar.
   */
  void onCalendarSelected(String calendarName);

  /**
   * Called when user selects a date.
   */
  void onDateSelected(LocalDate date);

  /**
   * Called when user wants to create an event.
   */
  void onCreateEventRequested(LocalDate date, String subject,
                              LocalDate startDate, int startHour, int startMinute,
                              LocalDate endDate, int endHour, int endMinute);

  /**
   * Called when user wants to edit an event.
   */
  void onEditEventRequested(LocalDate date, String eventIdentifier,
                            String newSubject, int newStartHour, int newStartMinute,
                            int newEndHour, int newEndMinute);


  /**
   * Called when user navigates to different month.
   */
  void onMonthChanged(int year, int month);
}