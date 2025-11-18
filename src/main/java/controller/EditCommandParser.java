package controller;

import java.time.LocalDateTime;

/**
 * Parses "edit" commands into structured ParsedCommand objects.
 * Handles three types of edits: single event, events from a date forward,
 * and entire series.
 */
public class EditCommandParser {

  /**
   * This is the main entry point for this parser.
   *
   * @param command The full command string (e.g., "edit event ...")
   * @return A Command object (EditEventCommand, EditEventsCommand, etc.)
   * @throws CommandParseException if parsing fails.
   */
  protected ParsedCommand parsedEditCommand(String command) {
    String commandLower = command.trim().toLowerCase();

    if (commandLower.startsWith("edit events ")) {
      return handleEditEvents(command);
    } else if (commandLower.startsWith("edit series ")) {
      return handleEditSeries(command);
    } else if (commandLower.startsWith("edit event ")) {
      return handleEditEvent(command);
    }

    throw new CommandParseException("Unknown 'edit' command format.");
  }

  /**
   * Strictly parses: edit event {@code <prop> <subj>}
   * from {@code <start>} to {@code <end>} with {@code <val>}.
   */
  private ParsedCommand handleEditEvent(String command) {

    String args = command.substring("edit event ".length());
    String[] propAndRest = extractFirstWord(args);


    String[] subjAndRest = ExtractUtil.extractSubjectAndRest(propAndRest[1]);
    String rest = subjAndRest[1];

    if (!rest.toLowerCase().startsWith("from ") || !rest.toLowerCase().contains(" to ")
        || !rest.toLowerCase().contains(" with ")) {
      throw new CommandParseException(
          "Invalid 'edit event' format: expected 'from <start> to <end> with <value>'.");
    }

    int toIdx = rest.toLowerCase().indexOf(" to ");
    int withIdx = rest.toLowerCase().indexOf(" with ");

    if (!(toIdx < withIdx)) {
      throw new CommandParseException("Invalid 'edit event' format: 'to' must come before 'with'.");
    }

    String startStr = rest.substring("from ".length(), toIdx).trim();
    String endStr = rest.substring(toIdx + " to ".length(), withIdx).trim();
    String newValue = extractValue(rest.substring(withIdx + " with ".length()));

    if (startStr.isEmpty() || startStr.contains(" ")) {
      throw new CommandParseException("Invalid start time. Check for extra words.");
    }
    if (endStr.isEmpty() || endStr.contains(" ")) {
      throw new CommandParseException("Invalid end time. Check for extra words.");
    }

    LocalDateTime startTime = DateTimeParsing.parseDateTime(startStr);
    LocalDateTime endTime = DateTimeParsing.parseDateTime(endStr);

    String property = propAndRest[0];
    String subject = subjAndRest[0];
    return new ParsedCommand.Builder(CommandType.EDIT_SINGLE_EVENT)
        .propertyToEdit(property)
        .newPropertyValue(newValue)
        .subject(subject)
        .startDateTime(startTime)
        .endDateTime(endTime)
        .build();
  }

  /**
   * Strictly parses: edit events {@code <prop> <subj>} from {@code <start>} with {@code <val>}.
   */
  private ParsedCommand handleEditEvents(String command) {
    return handleEditSeriesAndEvents(command, "edit events ", CommandType.EDIT_EVENTS);
  }

  /**
   * Strictly parses: edit series {@code <prop> <subj>} from {@code <start>} with {@code <val>}.
   */
  private ParsedCommand handleEditSeries(String command) {
    return handleEditSeriesAndEvents(command, "edit series ", CommandType.EDIT_SERIES);
  }

  /**
   * Helper for parsing Series and Events editing.
   *
   * @param command command string input.
   * @param prefix  "edit series" or "edit events".
   * @param type    type of command.
   * @return ParsedCommand object.
   */
  private ParsedCommand handleEditSeriesAndEvents(String command, String prefix, CommandType type) {
    String args = command.substring(prefix.length());
    String[] propAndRest = extractFirstWord(args);
    String property = propAndRest[0];

    String[] subjAndRest = ExtractUtil.extractSubjectAndRest(propAndRest[1]);
    String subject = subjAndRest[0];
    String rest = subjAndRest[1];

    String lowerRest = rest.toLowerCase();
    if (!lowerRest.startsWith("from ") || !lowerRest.contains(" with ")) {
      throw new CommandParseException(
          "Invalid format: expected 'from <start> with <value>'.");
    }

    int withIdx = lowerRest.indexOf(" with ");
    String startStr = rest.substring("from ".length(), withIdx).trim();
    String newValue = extractValue(rest.substring(withIdx + " with ".length()));

    if (startStr.isEmpty() || startStr.contains(" ")) {
      throw new CommandParseException("Invalid start time. Check for extra words.");
    }

    LocalDateTime startTime = DateTimeParsing.parseDateTime(startStr);

    return new ParsedCommand.Builder(type)
        .propertyToEdit(property)
        .newPropertyValue(newValue)
        .subject(subject)
        .startDateTime(startTime)
        .build();
  }


  /**
   * Extracts the very first word from a string.
   * [0] = first word, [1] = rest of string
   */
  private String[] extractFirstWord(String text) {
    text = text.trim();
    int firstSpaceIdx = text.indexOf(' ');
    if (firstSpaceIdx == -1) {
      throw new CommandParseException("Command missing arguments after property.");
    }
    return new String[] {
        text.substring(0, firstSpaceIdx),
        text.substring(firstSpaceIdx + 1).trim()
    };
  }

  /**
   * Extracts a new value, which is either quoted or a single word.
   */
  private String extractValue(String text) {
    text = text.trim();
    if (text.startsWith("\"")) {
      int endQuoteIdx = text.indexOf('"', 1);
      if (endQuoteIdx == -1) {
        throw new CommandParseException("Mismatched quotes in 'with' value.");
      }
      if (endQuoteIdx != text.length() - 1) {
        throw new CommandParseException("Invalid 'with' value: extra text after closing quote.");
      }
      return text.substring(1, endQuoteIdx);
    } else {
      if (text.contains(" ")) {
        throw new CommandParseException(
            "Invalid 'with' value: multi-word values must be in quotes.");
      }
      return text;
    }
  }

}

