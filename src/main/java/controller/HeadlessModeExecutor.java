package controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import view.IcalendarView;

/**
 * Executes the calendar application in headless mode.
 * Reads commands sequentially from a file until an "exit" command
 * is encountered.
 */
public class HeadlessModeExecutor implements ImodeExecutor {

  private final List<String> commands;
  private final Iterator<String> commandIterator;

  /**
   * Constructor for HeadlessModeExecutor. Calls readCommandsFromFile
   * and stores the commands as a list of String.
   * Validates the file contains exit command.
   * Stores the iterator for the commands.
   *
   * @param commandFilePath path of the commands file given in arguments.
   * @throws IOException in case of File IO exception.
   */
  public HeadlessModeExecutor(String commandFilePath) throws IOException {
    this.commands = readCommandsFromFile(commandFilePath);
    validateExitCommand();
    this.commandIterator = commands.iterator();
  }

  /**
   * Executes application in headless mode by reading commands from
   * the view's input source until "exit" is encountered.
   *
   * @param calendarController controller to process commands
   */
  @Override
  public void execute(IcalendarController calendarController) {
    IcalendarView view = calendarController.getView();

    while (true) {
      String command = readCommand();

      if (command.trim().isEmpty()) {
        continue;
      }

      if (command.trim().equalsIgnoreCase("exit")) {
        break;
      }

      calendarController.processCommand(command);
    }

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
    if (commandIterator.hasNext()) {
      return commandIterator.next();
    }
    return "exit";
  }

  /**
   * Reads all commands from the specified file.
   *
   * @param filePath the path to the command file
   * @return list of command strings from the file
   * @throws IOException if the file does not exist or is not readable
   */
  private List<String> readCommandsFromFile(String filePath) throws IOException {
    Path path = Paths.get(filePath);

    if (!Files.exists(path)) {
      throw new IOException("File not found: " + filePath);
    }

    if (!Files.isReadable(path)) {
      throw new IOException("File is not readable: " + filePath);
    }

    return Files.readAllLines(path);
  }

  /**
   * Validates that the command file ends with an 'exit' command.
   * Checks the last non-empty line in the file.
   *
   * @throws IOException if the file is empty, contains only empty lines,
   *                     or does not end with 'exit' command
   */
  private void validateExitCommand() throws IOException {
    if (commands.isEmpty()) {
      throw new IOException("Command file is empty");
    }

    for (int i = commands.size() - 1; i >= 0; i--) {
      String command = commands.get(i).trim();
      if (!command.isEmpty()) {
        if (!command.equalsIgnoreCase("exit")) {
          throw new IOException("Command file must end with 'exit' command");
        }
        return;
      }
    }

    throw new IOException("Command file contains only empty lines");
  }


}