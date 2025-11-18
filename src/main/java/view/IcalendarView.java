package view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.Event;

/**
 * Interface for calendar view implementations.
 * Defines methods for reading user input and displaying calendar information
 * in different modes (interactive or headless).
 */
public interface IcalendarView {


  /**
   * Displays a welcome message when the application starts.
   */
  void displayWelcome();

  /**
   * Displays a prompt for the user to enter a command.
   */
  void displayPrompt();

  /**
   * Displays a success message to the user.
   *
   * @param message the success message to display
   */
  void displaySuccess(String message);

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to display
   */
  void displayError(String message);

  /**
   * Displays the user's busy/available status at a specific date and time.
   *
   * @param isBusy true if the user is busy, false if available
   * @param date the date and time to check
   */
  void displayStatus(boolean isBusy, LocalDateTime date);

  /**
   * Displays a general message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays all events scheduled on a specific date.
   *
   * @param events list of events to display
   * @param date the date for which events are being displayed
   */
  void displayEventsForDate(List<Event> events, LocalDate date);

  /**
   * Displays all events within a specific date/time range.
   *
   * @param events list of events to display
   * @param start the start of the time range
   * @param end the end of the time range
   */
  void displayEventsForRange(List<Event> events, LocalDateTime start, LocalDateTime end);

  /**
   * Displays a goodbye message when the application exits.
   */
  void displayGoodbye();
}