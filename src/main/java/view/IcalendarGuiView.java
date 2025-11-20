package view;

import controller.features.CalendarFeatures;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IcalendarGuiView {
  /**
   * Registers the callbacks (the controller) with the view.
   */
  void setFeatures(CalendarFeatures features);

  /**
   * Makes the view visible.
   */
  void display();

  /**
   * Updates the grid of days for the month view.
   * @param currentMonth The date representing the month being viewed.
   * @param eventsSummary A map where Key=DayOfMonth, Value=String summary of events.
   */
  void updateMonthView(LocalDate currentMonth, Map<Integer, String> eventsSummary);

  /**
   * populates the calendar selection dropdown.
   */
  void updateCalendarList(List<String> calendarNames);

  /**
   * Updates the specific event list for a selected day.
   */
  void updateDayDetails(LocalDate date, List<String> events);

  /**
   * Displays a success or info message.
   */
  void displayMessage(String message);

  /**
   * Displays an error popup.
   */
  void displayError(String error);

  /**
   * Clears input forms.
   */
  void clearInputForms();
}
