package controller;

import java.util.Scanner;
import view.IcalendarView;

/**
 * Executes the calendar application in interactive mode.
 * Displays prompts and processes user commands in real-time until
 * an "exit" command is entered.
 */
public class InteractiveModeExecutor implements ImodeExecutor {
  private final Scanner scanner = new Scanner(System.in);


  /**
   * Executes the application in interactive mode by displaying prompts
   * and processing user input until "exit" is entered.
   *
   * @param calendarController the controller to process commands
   */
  @Override
  public void execute(IcalendarController calendarController) {
    IcalendarView view = calendarController.getView();
    view.displayWelcome();

    while (true) {

      view.displayPrompt();
      String command = readCommand();

      if (command.equals("exit")) {
        break;
      }

      if (command.isEmpty()) {
        continue;
      }

      calendarController.processCommand(command);

    }
    view.displayGoodbye();
    calendarController.shutDown();
  }

  /**
   * Reads the next command from the input source.
   * In interactive mode, reads from console; in headless mode, reads from file.
   *
   * @return the command string entered by the user or read from file
   */
  @Override
  public String readCommand() {
    if (scanner.hasNextLine()) {
      return scanner.nextLine().trim();
    }
    return "exit";
  }
}

