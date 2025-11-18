package controller;


/**
 * Defines the contract for executing the calendar application in
 * either interactive or headless execution.
 */
public interface ImodeExecutor {

  /**
   * Executes the calendar application in the mode-specific manner.
   *
   * @param calendarController the controller to run
   */
  void execute(IcalendarController calendarController);

  /**
   * Reads the next command from the input source.
   * In interactive mode, reads from console; in headless mode, reads from file.
   *
   * @return the command string entered by the user or read from file
   */
  String readCommand();
}
