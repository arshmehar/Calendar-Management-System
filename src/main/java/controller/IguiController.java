package controller;

import java.util.List;
import model.Icalendar;
import model.IcalendarSystem;

/**
 * Controller interface for GUI-specific operations.
 * Extends the base controller with GUI-specific methods.
 */
public interface IguiController {

  /**
   * Execute a parsed command.
   */
  void executeCommand(ParsedCommand cmd) throws Exception;

  /**
   * Get currently selected calendar.
   */
  Icalendar getCurrentCalendar();

  /**
   * Set the current calendar context.
   */
  void setCurrentCalendar(String calendarName);

  /**
   * Refresh the GUI view.
   */
  void refreshView();

  /**
   * Show error message to user.
   */
  void showError(String message);

  /**
   * Show success message to user.
   */
  void showSuccess(String message);

  /**
   * Get the calendar system model.
   */
  IcalendarSystem getModel();

  /**
   * Get all calendar names.
   */
  List<String> getAllCalendarNames();

}
