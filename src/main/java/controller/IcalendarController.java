package controller;

import java.io.IOException;
import view.IcalendarView;

/**
 * Defines the contract for a controller in the calendar application.
 * The controller is responsible for managing the application's lifecycle,
 * parsing user input, and coordinating between the model and the view.
 */
public interface IcalendarController {

  /**
   * Starts the application in the configured mode (interactive or headless).
   * Also passes the file path of commands which is null in case of interactive
   * and the file path string in case of headless mode.
   */
  void run(ApplicationMode mode, String commandFilePath) throws IOException;

  /**
   * Processes a single command string by parsing and executing it.
   *
   * @param command the command string to process
   */
  void processCommand(String command);

  /**
   * Shuts down the controller and releases resources.
   */
  void shutDown();

  /**
   * Returns the view associated with this controller.
   *
   * @return the calendar view
   */
  IcalendarView getView();
}
