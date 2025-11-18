package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * command parsing for copy commands i.e.
 * 1.copy event eventName on {@code <dateTimeString>}  --target calendarName.
 *    to dateTimeString
 * 2.copy events on {@code <dateString>}  --target {@code <calendarName>} to {@code <dateString>}
 * 3.copy events --target calendarName to {@code <dateString>} .
 */
public class CopyCommandParser {

  /**
   * parses a "copy event" or "copy events" command.
   *
   * @param command the full command string starting with "copy"
   * @return ParsedCommand containing the parsed event details
   */
  protected ParsedCommand parsedCopyCommand(String command) {
    String commandLower = command.trim().toLowerCase();


    if (!commandLower.contains(" --target ")) {
      throw new CommandParseException(
          "Invalid copy command: missing '--target <calendarName>'.");
    }
    if (!commandLower.contains(" to ")) {
      throw new CommandParseException(
          "Invalid copy command: missing 'to <date/dateTime>'.");
    }

    if (commandLower.startsWith("copy event ")) {
      return parseCopySingleEvent(command);
    } else if (commandLower.startsWith("copy events on ")) {
      return parseCopyEventsOnDate(command);
    } else if (commandLower.startsWith("copy events between ")) {
      return parseCopyEventsBetween(command);
    }

    throw new CommandParseException("Unknown copy command format.");
  }

  /**
   * Parses: copy event eventName on dateTimeString --target calendarName.
   * to dateTimeString
   *
   * @param command the full command string
   * @return ParsedCommand for copying a single event
   * @throws CommandParseException if format is invalid
   */
  private ParsedCommand parseCopySingleEvent(String command) {
    String args = command.substring("copy event ".length());

    String[] nameAndRest = ExtractUtil.extractSubjectAndRest(args);
    String rest = nameAndRest[1]; // "on <datetime> --target <cal> to <datetime>"

    if (!rest.toLowerCase().startsWith("on ")) {
      throw new CommandParseException(
          "Invalid copy event format: expected 'on <dateTime>' after event name.");
    }

    int targetIdx = rest.toLowerCase().indexOf(" --target ");

    if (targetIdx <= 0) {
      throw new CommandParseException(
          "Invalid copy event format: expected 'on <dateTime> --target <cal> to <dateTime>'.");
    }

    String sourceDateTimeStr = rest.substring("on ".length(), targetIdx).trim();
    if (sourceDateTimeStr.isEmpty() || sourceDateTimeStr.contains(" ")) {
      throw new CommandParseException(
          "Invalid source date-time. Check for extra words before '--target'.");
    }

    String afterTarget = rest.substring(targetIdx + " --target ".length()).trim();

    String[] calAndRest = extractCalendarNameAndRest(afterTarget);
    String targetCalendar = calAndRest[0];
    String targetDateTimeStr = getTargetDateTimeStr(calAndRest);

    LocalDateTime sourceDateTime = DateTimeParsing.parseDateTime(sourceDateTimeStr);
    LocalDateTime targetDateTime = DateTimeParsing.parseDateTime(targetDateTimeStr);

    String eventName = nameAndRest[0];

    return new ParsedCommand.Builder(CommandType.COPY_SINGLE_EVENT)
        .subject(eventName)
        .startDateTime(sourceDateTime)
        .targetCalendarName(targetCalendar)
        .targetDateTime(targetDateTime)
        .build();
  }

  private static String getTargetDateTimeStr(String[] calAndRest) {
    String remaining = calAndRest[1]; // should be "to <datetime>"

    if (!remaining.toLowerCase().startsWith("to ")) {
      throw new CommandParseException(
          "Invalid copy event format: expected 'to <dateTime>' after calendar name.");
    }

    String targetDateTimeStr = remaining.substring("to ".length()).trim();
    if (targetDateTimeStr.isEmpty() || targetDateTimeStr.contains(" ")) {
      throw new CommandParseException(
          "Invalid target date-time. Check for extra words at the end.");
    }
    return targetDateTimeStr;
  }


  private ParsedCommand parseCopyEventsOnDate(String command) {
    String args = command.substring("copy events on ".length());
    TargetInfo targetInfo = extractTargetInfo(args);

    String sourceDateStr = args.substring(0, targetInfo.targetIdx).trim();
    if (sourceDateStr.isEmpty() || sourceDateStr.contains(" ")) {
      throw new CommandParseException("Invalid source date before '--target'.");
    }

    LocalDate sourceDate = DateTimeParsing.parseDate(sourceDateStr);

    return new ParsedCommand.Builder(CommandType.COPY_EVENTS_ON_DATE)
        .queryDate(sourceDate)
        .targetCalendarName(targetInfo.calendar)
        .targetDate(targetInfo.date)
        .build();
  }


  private static String getTargetDateStr(String[] calAndRest) {
    String remaining = calAndRest[1];

    if (!remaining.toLowerCase().startsWith("to ")) {
      throw new CommandParseException(
          "Invalid copy events format: expected 'to <date>' after calendar name.");
    }

    String targetDateStr = remaining.substring("to ".length()).trim();
    if (targetDateStr.isEmpty() || targetDateStr.contains(" ")) {
      throw new CommandParseException(
          "Invalid target date. Check for extra words at the end.");
    }
    return targetDateStr;
  }

  /**
   * Parses: copy events --target calendarName to dateString.
   *
   * @param command the full command string
   * @return ParsedCommand for copying events within a date range
   * @throws CommandParseException if format is invalid
   */

  private ParsedCommand parseCopyEventsBetween(String command) {
    String args = command.substring("copy events between ".length());

    int andIdx = args.toLowerCase().indexOf(" and ");
    TargetInfo targetInfo = extractTargetInfo(args);

    if (!(andIdx > 0 && targetInfo.targetIdx > andIdx)) {
      throw new CommandParseException("Expected '<date> and <date> --target <cal> to <date>'.");
    }

    String startDateStr = args.substring(0, andIdx).trim();
    String endDateStr = args.substring(andIdx + " and ".length(), targetInfo.targetIdx).trim();

    LocalDate startDate = DateTimeParsing.parseDate(startDateStr);
    LocalDate endDate = DateTimeParsing.parseDate(endDateStr);

    if (endDate.isBefore(startDate)) {
      throw new CommandParseException("End date cannot be before start date.");
    }

    return new ParsedCommand.Builder(CommandType.COPY_EVENTS_BETWEEN_DATES)
        .intervalStartDate(startDate)
        .intervalEndDate(endDate)
        .targetCalendarName(targetInfo.calendar)
        .targetDate(targetInfo.date)
        .build();
  }


  /**
   * Extracts calendar name (which may be quoted) and the remaining text after it.
   *
   * @param text the text starting with the calendar name
   * @return array with [0]=calendar name, [1]=remaining text after the name
   * @throws CommandParseException if calendar name is invalid or quotes mismatched
   */
  private String[] extractCalendarNameAndRest(String text) {
    text = text.trim();
    String[] result = new String[2];

    if (text.startsWith("\"")) {
      // Quoted calendar name (multi-word)
      int endQuoteIdx = text.indexOf('"', 1);
      if (endQuoteIdx == -1) {
        throw new CommandParseException("Mismatched quotes in calendar name.");
      }

      result[0] = text.substring(1, endQuoteIdx);
      result[1] = text.substring(endQuoteIdx + 1).trim();

    } else {
      // Unquoted calendar name (single word)
      int firstSpaceIdx = text.indexOf(' ');
      if (firstSpaceIdx == -1) {
        throw new CommandParseException("Command missing arguments after calendar name.");
      }

      result[0] = text.substring(0, firstSpaceIdx);
      result[1] = text.substring(firstSpaceIdx + 1).trim();
    }

    if (result[0].isEmpty()) {
      throw new CommandParseException("Calendar name cannot be empty.");
    }

    return result;
  }


  private TargetInfo extractTargetInfo(String args) {
    int targetIdx = args.toLowerCase().indexOf(" --target ");
    if (targetIdx < 0) {
      throw new CommandParseException("Missing '--target' keyword.");
    }

    String afterTarget = args.substring(targetIdx + " --target ".length()).trim();
    String[] calAndRest = extractCalendarNameAndRest(afterTarget);
    String targetCalendar = calAndRest[0];
    String targetDateStr = getTargetDateStr(calAndRest);

    LocalDate targetDate = DateTimeParsing.parseDate(targetDateStr);

    return new TargetInfo(targetCalendar, targetDate, targetIdx);
  }

  private static class TargetInfo {
    String calendar;
    LocalDate date;
    int targetIdx;

    TargetInfo(String calendar, LocalDate date, int targetIdx) {
      this.calendar = calendar;
      this.date = date;
      this.targetIdx = targetIdx;
    }
  }


}

