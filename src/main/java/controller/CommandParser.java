package controller;

/**
 * Parses command strings into structured ParsedCommand objects.
 * Delegates parsing to specialized parsers based on command type
 * (create, edit, print, show status, export).
 */
public class CommandParser {

  private final CreateCommandParser createParser;
  private final EditCommandParser editParser;
  private final QueryCommandParser queryParser;
  private final StatusCommandParser statusParser;
  private final ExportCommandParser exportParser;
  private final CopyCommandParser copyParser;
  private final CalendarOperationsCommandParser calendarOperationsParser;

  /**
   * Constructs a CommandParser and initializes all specialized parsers
   * for handling different command types.
   */
  public CommandParser() {
    this.createParser = new CreateCommandParser();
    this.editParser = new EditCommandParser();
    this.queryParser = new QueryCommandParser();
    this.statusParser = new StatusCommandParser();
    this.exportParser = new ExportCommandParser();
    this.copyParser = new CopyCommandParser();
    this.calendarOperationsParser = new CalendarOperationsCommandParser();
  }


  /**
   * Parses a command string and returns a ParsedCommand object.
   * Determines the command type and delegates to the appropriate specialized parser.
   *
   * @param command the command string to parse
   * @return a ParsedCommand object containing the parsed command details
   * @throws CommandParseException if the command is invalid or unrecognized
   */
  public ParsedCommand parse(String command) throws CommandParseException {

    String cmd = command.trim().toLowerCase();

    if (cmd.startsWith("create event ")) {
      return createParser.parsedCreateCommand(command);
    } else if (cmd.startsWith("create calendar ")
        || cmd.startsWith("use calendar ")
        || cmd.startsWith("edit calendar ")) {
      return calendarOperationsParser.parsedCalendarCommand(command);
    } else if (cmd.startsWith("edit ")) {
      return editParser.parsedEditCommand(command);
    } else if (cmd.startsWith("print event")) {
      return queryParser.parsedQueryCommand(command);
    } else if (cmd.startsWith("show status on ")) {
      return statusParser.parsedStatusCommand(command);
    } else if (cmd.startsWith("export cal ")) {
      return exportParser.parsedExportCommand(command);
    } else if (command.trim().toLowerCase().startsWith("copy ")) {
      return copyParser.parsedCopyCommand(command);
    } else {
      throw new CommandParseException("Invalid command");
    }
  }
}
