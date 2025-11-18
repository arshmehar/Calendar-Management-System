package controller;

import java.io.IOException;

/**
 * Manages and delegates execution to the appropriate mode executor
 * based on the application mode (interactive/headless).
 */
public class ModeManager {

  private final String commandFilePath;

  /**
   * Constructs a ModeManager and initializes both mode executors.
   */
  public ModeManager(String commandFilePath) {
    this.commandFilePath = commandFilePath;
  }

  /**
   * Executes the calendar application in the specified mode by delegating
   * to the apt executor.
   *
   * @param mode               the application mode (INTERACTIVE or HEADLESS)
   * @param calendarController the controller to run
   */
  public void execute(ApplicationMode mode, CalendarController calendarController)
      throws IOException {

    if (mode == ApplicationMode.HEADLESS) {
      HeadlessModeExecutor headlessModeExecutor = new HeadlessModeExecutor(commandFilePath);
      headlessModeExecutor.execute(calendarController);
    }

    if (mode == ApplicationMode.INTERACTIVE) {
      InteractiveModeExecutor interactiveModeExecutor = new InteractiveModeExecutor();
      interactiveModeExecutor.execute(calendarController);
    }

  }
}
