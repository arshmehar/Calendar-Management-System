package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Parses "print events" query commands into structured ParsedCommand objects.
 * Handles queries for events on a specific date or within a date range.
 */
public class QueryCommandParser {

  /**
   * This is the main entry point for this parser.
   *
   * @param command The full command string (e.g., "print events on ...")
   * @return A Command object (PrintEventsOnCommand, etc.)
   * @throws CommandParseException if parsing fails.
   */
  protected ParsedCommand parsedQueryCommand(String command) {
    String commandLower = command.trim().toLowerCase();

    if (commandLower.startsWith("print events on ")) {
      return handlePrintEventsOn(command);
    } else if (commandLower.startsWith("print events from ")) {
      return handlePrintEventsFromTo(command);
    }
    throw new CommandParseException("Unknown query command format.");
  }

  /**
   * Strictly parses: print events on {@code <dateString>}.
   */
  private ParsedCommand handlePrintEventsOn(String command) {
    String dateStr = command.substring("print events on ".length()).trim();

    if (dateStr.isEmpty() || dateStr.contains(" ")) {
      throw new CommandParseException(
        "Invalid date. Check for extra words at the end of the command.");
    }

    LocalDate date = DateTimeParsing.parseDate(dateStr);

    return new ParsedCommand.Builder(CommandType.PRINT_EVENTS_ON)
        .queryDate(date)
        .build();
  }

  /**
   * Strictly parses: print events from {@code <dateStringTtimeString>}
   * to {@code <dateStringTtimeString>}.
   */
  private ParsedCommand handlePrintEventsFromTo(String command) {
    String args = command.substring("print events from ".length());
    int toIdx = args.toLowerCase().indexOf(" to ");

    if (toIdx == -1) {
      throw new CommandParseException("Invalid 'print events' format: missing 'to'.");
    }

    String startStr = args.substring(0, toIdx).trim();
    String endStr = args.substring(toIdx + " to ".length()).trim();

    if (startStr.isEmpty() || startStr.contains(" ")) {
      throw new CommandParseException("Invalid start time. Check for extra words before 'to'.");
    }
    if (endStr.isEmpty() || endStr.contains(" ")) {
      throw new CommandParseException(
        "Invalid end time. Check for extra words at the end of the command.");
    }

    LocalDateTime startTime = DateTimeParsing.parseDateTime(startStr);
    LocalDateTime endTime = DateTimeParsing.parseDateTime(endStr);

    return new ParsedCommand.Builder(CommandType.PRINT_EVENTS_FROM)
        .queryStartDateTime(startTime)
        .queryEndDateTime(endTime)
        .build();
  }

}
