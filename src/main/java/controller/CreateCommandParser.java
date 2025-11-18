package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses "create event" commands into ParsedCommand objects.
 * Handles single events, all-day events, and recurring event series
 * with various repeat patterns (for N times/until a date).
 */
public class CreateCommandParser {

  /**
   * Parses a "create event" command and routes to appropriate handler
   * based on command format (single event, all-day, or series).
   *
   * @param command the full command string starting with "create event"
   * @return ParsedCommand containing the parsed event details
   * @throws CommandParseException if command format is invalid
   */
  protected ParsedCommand parsedCreateCommand(String command) {

    String args = command.substring("create event ".length());

    String[] subjectAndRest = ExtractUtil.extractSubjectAndRest(args);
    String subject = subjectAndRest[0];
    String rest = subjectAndRest[1];

    if (rest.toLowerCase().contains(" repeats ")) {

      if (rest.toLowerCase().startsWith("from ")) {
        return handleCreateTimedSeries(subject, rest);
      } else if (rest.toLowerCase().startsWith("on ")) {
        return handleCreateAllDaySeries(subject, rest);
      }
    } else if (rest.startsWith("from ") && rest.contains(" to ")) {

      return handleCreateSingleTimedEvent(subject, rest);
    } else if (rest.startsWith("on ")) {

      return handleCreateSingleAllDayEvent(subject, rest);
    }

    throw new CommandParseException("Invalid 'create event' format. "
        + "Expected 'from...to...', 'on...', or '...repeats...'.");
  }

  /**
   * Parses a single timed event command.
   * Format: from {@code <dateTime>} to {@code <dateTime>}
   *
   * @param subject the event subject
   * @param rest the remaining command text after subject
   * @return ParsedCommand for a single timed event
   * @throws CommandParseException if format is invalid
   */
  private ParsedCommand handleCreateSingleTimedEvent(String subject, String rest) {
    int toIdx = rest.indexOf(" to ");
    String startStr = rest.substring("from ".length(), toIdx).trim();
    String endStr = rest.substring(toIdx + " to ".length()).trim();

    if (startStr.isEmpty() || startStr.contains(" ")) {
      throw new CommandParseException("Invalid start time.  extra words before 'to'.");
    }
    if (endStr.isEmpty() || endStr.contains(" ")) {
      throw new CommandParseException("Invalid end time. extra words at the end of the command.");
    }

    LocalDateTime startTime = DateTimeParsing.parseDateTime(startStr);
    LocalDateTime endTime = DateTimeParsing.parseDateTime(endStr);

    return new ParsedCommand.Builder(CommandType.CREATE_SINGLE_EVENT)
        .subject(subject)
        .startDateTime(startTime)
        .endDateTime(endTime)
        .build();

  }

  /**
   * This is now a sub-router for TIMED series events.
   * It parses the common parts and delegates to "for" or "until" handlers.
   *
   * @param subject the event subject
   * @param rest the remaining command text after subject
   * @return ParsedCommand for a timed event series
   * @throws CommandParseException if format is invalid
   */
  private ParsedCommand handleCreateTimedSeries(String subject, String rest) {
    String restLower = rest.toLowerCase();
    int toIdx = rest.indexOf(" to ");
    int repeatsIdx = getRepeatsIdx(rest, restLower, toIdx);

    String startStr = rest.substring("from ".length(), toIdx).trim();
    String endStr = rest.substring(toIdx + " to ".length(), repeatsIdx).trim();

    if (startStr.isEmpty() || startStr.contains(" ")) {
      throw new CommandParseException("Invalid start time. Check for extra words before 'to'.");
    }
    if (endStr.isEmpty() || endStr.contains(" ")) {
      throw new CommandParseException("Invalid end time. Check for extra words before 'repeats'.");
    }

    LocalDateTime startTime = DateTimeParsing.parseDateTime(startStr);
    LocalDateTime endTime = DateTimeParsing.parseDateTime(endStr);

    return getSeriesParsedCommand(subject, rest, repeatsIdx, startTime, endTime);
  }

  private ParsedCommand getSeriesParsedCommand(String subject, String rest, int repeatsIdx,
                                               LocalDateTime startTime, LocalDateTime endTime) {
    String afterRepeats = rest.substring(repeatsIdx + " repeats ".length());
    String afterRepeatsLower = afterRepeats.toLowerCase();

    int forIdx = afterRepeatsLower.indexOf(" for ");
    int untilIdx = afterRepeatsLower.indexOf(" until ");

    if (forIdx != -1 && (untilIdx == -1 || forIdx < untilIdx)) {
      return handleCreateSeriesForNtimes(subject, startTime, endTime, afterRepeats, forIdx);
    } else if (untilIdx != -1 && (forIdx == -1 || untilIdx < forIdx)) {
      return handleCreateSeriesUntilDate(subject, startTime, endTime, afterRepeats, untilIdx);
    } else {
      throw new CommandParseException(
        "Invalid 'repeats' format. Expected 'for <N> times' or 'until <date>' after weekdays.");
    }
  }

  private static int getRepeatsIdx(String rest, String restLower, int toIdx) {
    int repeatsIdx = restLower.indexOf(" repeats ");

    if (toIdx == -1 || !rest.startsWith("from ")) {
      throw new CommandParseException(
        "Invalid event series: must include 'from <start> to <end>'.");
    }
    if (repeatsIdx == -1) {
      throw new CommandParseException("Invalid 'create series' format: missing 'repeats'.");
    }
    if (!(toIdx < repeatsIdx)) {
      throw new CommandParseException(
        "Invalid 'create series' format: 'to' must come before 'repeats'.");
    }
    return repeatsIdx;
  }

  /**
   * Case 1: Strictly parses: (from...to...repeats) {@code <weekdays>} for {@code <N>} times.
   *
   * @param subject the event subject
   * @param startTime the start date and time for each occurrence
   * @param endTime the end date and time for each occurrence
   * @param afterRepeats the text after "repeats" keyword
   * @param forIdx the index of "for" keyword
   * @return ParsedCommand for series with occurrence count
   * @throws CommandParseException if format is invalid or N is not positive
   */
  private ParsedCommand handleCreateSeriesForNtimes(String subject, LocalDateTime startTime,
                                                    LocalDateTime endTime, String afterRepeats,
                                                    int forIdx) {
    String weekdays = afterRepeats.substring(0, forIdx).trim();
    String numTimesStr = afterRepeats.substring(forIdx + " for ".length()).trim();


    int numTimes = getNumTimes(weekdays, numTimesStr);
    if (numTimes <= 0) {
      throw new CommandParseException("Number of times must be a positive integer.");
    }

    Set<DayOfWeekAlphabet> daySet = convertToDayOfWeekSet(weekdays);
    return new ParsedCommand.Builder(CommandType.CREATE_SERIES_REPEATING_N_TIMES)
        .subject(subject)
        .startDateTime(startTime)
        .endDateTime(endTime)
        .occurrences(numTimes)
        .weekdays(daySet)
        .build();
  }

  private static int getNumTimes(String weekdays, String numTimesStr) {
    if (weekdays.isEmpty() || weekdays.contains(" ")) {
      throw new CommandParseException("Invalid weekdays. Check for extra words before 'for'.");
    }
    if (!numTimesStr.toLowerCase().endsWith(" times")) {
      throw new CommandParseException("Invalid 'create series' format. Must end with '<N> times'.");
    }

    String numStr = numTimesStr.substring(0, numTimesStr.length() - " times".length()).trim();
    if (numStr.isEmpty()) {
      throw new CommandParseException("Missing number for 'for <N> times'.");
    }

    int numTimes;
    try {
      numTimes = Integer.parseInt(numStr);
    } catch (NumberFormatException e) {
      throw new CommandParseException("Invalid number \"" + numStr + "\" for 'for <N> times'.");
    }
    return numTimes;
  }

  /**
   * Case 2: Strictly parses: (from...to...repeats) {@code <weekdays>} until {@code <date>}.
   *
   * @param subject the event subject
   * @param startTime the start date and time for each occurrence
   * @param endTime the end date and time for each occurrence
   * @param afterRepeats the text after "repeats" keyword
   * @param untilIdx the index of "until" keyword
   * @return ParsedCommand for series with end date
   * @throws CommandParseException if format is invalid
   */
  private ParsedCommand handleCreateSeriesUntilDate(String subject, LocalDateTime startTime,
                                                    LocalDateTime endTime, String afterRepeats,
                                                    int untilIdx) {
    String weekdays = afterRepeats.substring(0, untilIdx).trim();
    String untilDateStr = afterRepeats.substring(untilIdx + " until ".length()).trim();

    Set<DayOfWeekAlphabet> daySet = convertToDayOfWeekSet(weekdays);


    if (weekdays.isEmpty() || weekdays.contains(" ")) {
      throw new CommandParseException("Invalid weekdays. Check for extra words before 'until'.");
    }
    if (untilDateStr.isEmpty() || untilDateStr.contains(" ")) {
      throw new CommandParseException(
        "Invalid 'until' date. Check for extra words at the end of the command.");
    }

    LocalDate untilDate = DateTimeParsing.parseDate(untilDateStr);

    return new ParsedCommand.Builder(CommandType.CREATE_SERIES_UNTIL)
        .subject(subject)
        .startDateTime(startTime)
        .endDateTime(endTime)
        .weekdays(daySet)
        .seriesEndDate(untilDate)
        .build();

  }


  /**
   * Parses a single all-day event command.
   * Format: on {@code <date>}
   * Creates event from 8am to 5pm on the specified date.
   *
   * @param subject the event subject
   * @param rest the remaining command text after subject
   * @return ParsedCommand for a single all-day event
   * @throws CommandParseException if format is invalid
   */
  private ParsedCommand handleCreateSingleAllDayEvent(String subject, String rest) {
    if (!rest.toLowerCase().startsWith("on ")) {
      throw new CommandParseException("Invalid all-day event. Expected 'on <date>'.");
    }
    String dateStr = rest.substring("on ".length()).trim();
    if (dateStr.isEmpty() || dateStr.contains(" ")) {
      throw new CommandParseException(
        "Invalid date. Check for extra words at the end of the command.");
    }

    String onDate =  rest.substring("on ".length());
    LocalDate eventDate = DateTimeParsing.parseDate(onDate);
    LocalDateTime startTime = eventDate.atTime(8, 0);
    LocalDateTime endTime = eventDate.atTime(17, 0);
    return new ParsedCommand.Builder(CommandType.CREATE_ALL_DAY_EVENT)
        .subject(subject)
        .startDateTime(startTime)
        .endDateTime(endTime)
        .build();

  }


  /**
   * Case 3: This is the sub-router for ALL-DAY series events.
   * It parses: on {@code <date>} repeats {@code <weekdays>} for/until {@code <value>}.
   *
   * @param subject the event subject
   * @param rest the remaining command text after subject
   * @return ParsedCommand for an all-day event series
   * @throws CommandParseException if format is invalid
   */
  private ParsedCommand handleCreateAllDaySeries(String subject, String rest) {

    String restLower = rest.toLowerCase();
    int repeatsIdx = restLower.indexOf(" repeats ");

    if (repeatsIdx == -1 || !restLower.startsWith("on ")) {
      throw new CommandParseException(
        "Invalid all-day series format: expected 'on <date> repeats ...'");
    }

    String dateStr = rest.substring("on ".length(), repeatsIdx).trim();
    if (dateStr.isEmpty() || dateStr.contains(" ")) {
      throw new CommandParseException("Invalid date. Check for extra words before 'repeats'.");
    }

    LocalDate eventDate = DateTimeParsing.parseDate(dateStr);
    LocalDateTime startTime = eventDate.atTime(8, 0);
    LocalDateTime endTime = eventDate.atTime(17, 0);

    return getSeriesParsedCommand(subject, rest, repeatsIdx, startTime, endTime);
  }

  /**
   * Converts a weekday string to a set of DayOfWeekAlphabet enums.
   * Each character represents a day: M=Monday, T=Tuesday, W=Wednesday, R=Thursday,
   * F=Friday, S=Saturday, U=Sunday.
   *
   * @param weekdays string of day characters
   * @return set of DayOfWeekAlphabet values
   * @throws CommandParseException if any character is not a valid day
   */
  public Set<DayOfWeekAlphabet> convertToDayOfWeekSet(String weekdays)
      throws CommandParseException {
    Set<DayOfWeekAlphabet> result = new HashSet<>();
    for (char ch : weekdays.toCharArray()) {
      try {
        result.add(DayOfWeekAlphabet.valueOf(String.valueOf(ch)));
      } catch (IllegalArgumentException e) {
        throw new CommandParseException("Invalid day: " + ch);
      }
    }
    return result;
  }

}
