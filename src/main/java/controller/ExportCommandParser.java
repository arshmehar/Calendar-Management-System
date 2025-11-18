package controller;

/**
 * Parses "export cal" commands into structured ParsedCommand objects.
 * Validates that the filename is provided and ends with .csv extension.
 */
public class ExportCommandParser {
  /**
   * This is the main entry point for this parser.
   *
   * @param command The full command string (e.g., "export cal myCal.csv")
   * @return An ExportCalendarCommand object.
   * @throws CommandParseException if parsing fails.
   */
  ParsedCommand parsedExportCommand(String command) {

    String fileName = command.substring("export cal ".length()).trim();

    if (fileName.isEmpty()) {
      throw new CommandParseException("Missing filename for 'export cal' command.");
    }
    if (fileName.contains(" ")) {
      throw new CommandParseException("Filename cannot contain spaces.");
    }
    if (!fileName.toLowerCase().endsWith(".csv") && !fileName.toLowerCase().endsWith(".ical")) {
      throw new CommandParseException(
          "Invalid 'export cal' format: filename must end with .csv or .ical");
    }

    CommandType commandType = CommandType.EXPORT_CALENDAR_CSV;

    if (fileName.toLowerCase().endsWith(".ical")) {
      commandType = CommandType.EXPORT_CALENDAR_ICAL;
    }

    return new ParsedCommand.Builder(commandType)
        .fileName(fileName)
        .build();

  }

}
