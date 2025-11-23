package view.gui;

import controller.features.CalendarFeatures;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JFrame;
import model.Event;

/**
 * Interface for the Graphical User Interface of the Calendar Application.
 * Defines the contract for displaying calendar data, handling user interactions,
 * and providing feedback to the controller.
 */
public interface IcalendarGuiView {

  /**
   * Sets the feature listener (controller) for this view.
   * The view calls methods on this listener when user actions occur (e.g., creating events).
   *
   * @param features the controller implementation of CalendarFeatures
   */
  void setFeatures(CalendarFeatures features);

  /**
   * Makes the GUI visible to the user.
   * Should be called after initialization and configuration is complete.
   */
  void display();

  /**
   * Updates the list of available calendars shown in the calendar selector.
   *
   * @param names the list of calendar names to display
   */
  void setCalendarNames(List<String> names);

  /**
   * Sets the currently selected calendar in the UI dropdown.
   *
   * @param name the name of the calendar to select
   */
  void setSelectedCalendar(String name);

  /**
   * Updates the timezone display label in the UI.
   *
   * @param timezoneId the ID of the timezone to display (e.g., "America/New_York")
   */
  void updateTimezoneDisplay(String timezoneId);

  /**
   * Displays the list of events for a specific date in the event panel.
   *
   * @param events the list of events to display (can be null or empty)
   * @param date   the date associated with these events
   */
  void displayEvents(List<Event> events, LocalDate date);

  /**
   * Displays a success message to the user, typically as a modal popup.
   *
   * @param message the success message to display
   */
  void showSuccess(String message);

  /**
   * Displays an error message to the user, typically as a warning modal popup.
   *
   * @param message the error description
   */
  void showError(String message);

  /**
   * Retrieves the date currently selected by the user in the calendar grid.
   * This is useful for the controller to know which context to refresh.
   *
   * @return the selected LocalDate
   */
  LocalDate getSelectedDate();

  /**
   * Retrieves the main JFrame of the application.
   * Required for centering dialogs or acting as a parent for OptionPanes.
   *
   * @return the main application frame
   */
  JFrame getFrame();
}