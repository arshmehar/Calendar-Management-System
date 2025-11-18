package controller;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * Parses calendar operation commands including create, edit, and use calendar.
 * Handles calendar management commands that operate on the calendar system level
 * rather than on individual events within a calendar.
 */
public class CalendarOperationsCommandParser {

  /**
   * Parses a calendar operation command and returns a structured ParsedCommand.
   * Routes to appropriate handler based on command type.
   *
   * @param command the full command string
   *                (e.g., "create calendar --name MyCalendar --timezone UTC")
   * @return a ParsedCommand containing the parsed calendar operation details
   * @throws CommandParseException if the command format is invalid or unrecognized
   */
  protected ParsedCommand parsedCalendarCommand(String command) {
    String commandLower = command.trim().toLowerCase();

    if (commandLower.startsWith("create calendar ")) {
      return handleCreateCalendar(command);
    } else if (commandLower.startsWith("edit calendar ")) {
      return handleEditCalendar(command);
    } else if (commandLower.startsWith("use calendar ")) {
      return handleUseCalendar(command);
    }

    throw new CommandParseException("Unknown 'calendar operations' command format.");
  }

  /**
   * Parses a create calendar command.
   * Format: create calendar --name {@code <calName>} --timezone {@code <timezone>}
   *
   * @param command the full command string
   * @return a ParsedCommand for creating a calendar
   * @throws CommandParseException if the command format is invalid
   */
  private ParsedCommand handleCreateCalendar(String command) {
    String args = command.substring("create calendar ".length());

    String[] nameAndRest = extractNameAndRest(args, "--name");
    String calendarName = nameAndRest[0];
    String rest = nameAndRest[1];

    String timezone = getZoneId(rest);
    return new ParsedCommand.Builder(CommandType.CREATE_CALENDAR)
        .calendarName(calendarName)
        .calendarTimeZone(timezone)
        .isCalendarSystemOperation(true)
        .build();

  }

  /**
   * Extracts and validates the timezone from the command arguments.
   *
   * @param rest the remaining command string after the calendar name
   * @return the validated timezone identifier
   * @throws CommandParseException if timezone is missing, invalid, or contains multiple words
   */
  private static String getZoneId(String rest) {
    if (!rest.toLowerCase().startsWith("--timezone ")) {
      throw new CommandParseException(
          "Invalid 'create calendar' format. Expected --timezone flag after name.");
    }

    String timezoneStr = rest.substring("--timezone ".length()).trim();

    if (timezoneStr.isEmpty() || timezoneStr.contains(" ")) {
      throw new CommandParseException(
          "Invalid timezone value. Extra words. Timezone should be one token.");
    }

    ZoneId timezone;
    try {
      timezone = ZoneId.of(timezoneStr);
    } catch (DateTimeException e) {
      throw new CommandParseException(
          "Unsupported timezone: \"" + timezoneStr + "\". " + e.getMessage());
    }
    return timezoneStr;
  }

  /**
   * Parses a use calendar command to set the current calendar context.
   * Format: use calendar --name {@code <calName>}
   *
   * @param command the full command string
   * @return a ParsedCommand for setting calendar context
   * @throws CommandParseException if the command format is invalid
   */
  private ParsedCommand handleUseCalendar(String command) {
    String args = command.substring("use calendar ".length());

    String[] nameAndRest = extractNameAndRest(args, "--name");
    String calendarName = nameAndRest[0];
    String rest = nameAndRest[1];

    if (!rest.isEmpty()) {
      throw new CommandParseException(
          "Invalid 'use calendar' format. Extra text after calendar name.");
    }

    return new ParsedCommand.Builder(CommandType.SET_CONTEXT)
        .calendarName(calendarName)
        .context(calendarName)
        .isCalendarSystemOperation(true)
        .build();
  }

  /**
   * Parses an edit calendar command to modify calendar name or timezone.
   * Format: edit calendar --name {@code <calName>} --property {@code <property> <value>}
   * Valid properties: name, timezone
   *
   * @param command the full command string
   * @return a ParsedCommand for editing a calendar
   * @throws CommandParseException if the command format is invalid or property is unsupported
   */
  private ParsedCommand handleEditCalendar(String command) {
    String args = command.substring("edit calendar ".length());

    String[] nameAndRest = extractNameAndRest(args, "--name");
    String rest = nameAndRest[1];

    if (!rest.toLowerCase().startsWith("--property ")) {
      throw new CommandParseException(
          "Invalid 'edit calendar' format. Expected --property flag after name.");
    }

    String propArgs =
        rest.substring("--property ".length()).trim();

    int firstSpaceIdx = propArgs.indexOf(' ');
    if (firstSpaceIdx == -1) {
      throw new CommandParseException(
          "Invalid 'edit calendar' format. Missing property name or value.");
    }

    String propertyName = propArgs.substring(0, firstSpaceIdx);
    String propertyValueStr = propArgs.substring(firstSpaceIdx + 1).trim();

    String propertyValue = extractValue(propertyValueStr);

    if (!propertyName.equalsIgnoreCase("name") && !propertyName.equalsIgnoreCase("timezone")) {
      throw new CommandParseException(
          "Invalid property: \"" + propertyName + "\". Can only edit 'name' or 'timezone'.");
    }

    CommandType cmdType = CommandType.EDIT_CALENDAR_NAME;
    if (propertyName.equalsIgnoreCase("timezone")) {
      try {
        ZoneId.of(propertyValue);
        cmdType = CommandType.EDIT_CALENDAR_ZONE;
      } catch (DateTimeException e) {
        throw new CommandParseException("Unsupported timezone: \"" + propertyValue + "\".");
      }
    }

    String calendarName = nameAndRest[0];
    return new ParsedCommand.Builder(cmdType)
        .calendarName(calendarName)
        .propertyToEdit(propertyName)
        .newPropertyValue(propertyValue)
        .isCalendarSystemOperation(true)
        .build();

  }

  /**
   * Extracts a (potentially quoted) calendar name and the rest of the string.
   * Handles both quoted multi-word names and single-word names.
   *
   * @param args the string after the main command (e.g., after "create calendar ")
   * @param flag the flag to search for (e.g., "--name")
   * @return a String array: [0] = calendarName, [1] = rest of the string
   * @throws CommandParseException if format is invalid or quotes are mismatched
   */
  private String[] extractNameAndRest(String args, String flag) {
    if (!args.toLowerCase().startsWith(flag.toLowerCase() + " ")) {
      throw new CommandParseException("Invalid command format. Expected '" + flag + "' flag.");
    }

    String afterFlag = args.substring(flag.length() + 1).trim();
    String calendarName;
    String rest;

    if (afterFlag.startsWith("\"")) {

      int endQuoteIdx = afterFlag.indexOf('"', 1);
      if (endQuoteIdx == -1) {
        throw new CommandParseException("Mismatched quotes in calendar name.");
      }
      calendarName = afterFlag.substring(1, endQuoteIdx);
      rest = afterFlag.substring(endQuoteIdx + 1).trim();
    } else {

      int firstSpaceIdx = afterFlag.indexOf(' ');
      if (firstSpaceIdx == -1) {
        calendarName = afterFlag;
        rest = "";
      } else {
        calendarName = afterFlag.substring(0, firstSpaceIdx);
        rest = afterFlag.substring(firstSpaceIdx + 1).trim();
      }
    }

    if (calendarName.isEmpty()) {
      throw new CommandParseException("Calendar name cannot be empty.");
    }

    return new String[] {calendarName, rest};
  }

  /**
   * Extracts a single value, which is either quoted or a single word.
   * Used for parsing property values in edit commands.
   *
   * @param valueStr the string to parse (e.g., "\"New Value\"" or "America/New_York")
   * @return the unquoted value
   * @throws CommandParseException if quotes are mismatched or multi-word value is not quoted
   */
  private String extractValue(String valueStr) {
    if (valueStr.startsWith("\"")) {
      int endQuoteIdx = valueStr.indexOf('"', 1);
      if (endQuoteIdx == -1) {
        throw new CommandParseException("Mismatched quotes in property value.");
      }
      if (endQuoteIdx != valueStr.length() - 1) {
        throw new CommandParseException("Invalid format. Extra text after closing quote in value.");
      }
      return valueStr.substring(1, endQuoteIdx);
    } else {
      if (valueStr.contains(" ")) {
        throw new CommandParseException("Invalid value: multi-word values must be in quotes.");
      }
      return valueStr;
    }
  }

}