package controller;

import java.time.LocalDateTime;

/**
 * Parses "show status on" commands into structured ParsedCommand objects.
 * Extracts and validates the date-time for checking busy/available status.
 */
public class StatusCommandParser {

  /**
   * Parses a "show status on" command to check if the user is busy at a specific time.
   *
   * @param command the full command string
   * @return a ParsedCommand with the date-time to check
   * @throws CommandParseException if the date-time format is invalid
   */
  protected ParsedCommand parsedStatusCommand(String command) {
    String dateStr = command.substring("show status on ".length()).trim();

    if (dateStr.isEmpty() || dateStr.contains(" ")) {
      throw new CommandParseException(
        "Invalid date. Check for extra words at the end of the command.");
    }

    LocalDateTime date = DateTimeParsing.parseDateTime(dateStr);

    return new ParsedCommand.Builder(CommandType.SHOW_STATUS)
        .statusDateTime(date)
        .build();
  }
}
