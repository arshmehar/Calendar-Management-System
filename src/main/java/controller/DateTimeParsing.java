package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


/**
 * A final utility class for parsing date and time strings.
 * All methods are static.
 * This class cannot be instantiated.
 */
public final class DateTimeParsing {

  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Private constructor to prevent this utility class from being instantiated.
   */
  private DateTimeParsing() {}

  /**
   * Parses a "YYYY-MM-DDTHH:mm" string into a LocalDateTime.
   *
   * @param text The string to parse.
   * @return A valid LocalDateTime object.
   * @throws CommandParseException if parsing fails.
   */
  public static LocalDateTime parseDateTime(String text) {
    try {
      return LocalDateTime.parse(text, DATETIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new CommandParseException(
        "Invalid date-time format for \"" + text + "\". Expected 'yyyy-MM-ddTHH:mm'");
    }
  }

  /**
   * Parses a "YYYY-MM-DD" string into a LocalDate.
   *
   * @param text The string to parse.
   * @return A valid LocalDate object.
   * @throws CommandParseException if parsing fails.
   */
  public static LocalDate parseDate(String text) {
    try {
      return LocalDate.parse(text, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new CommandParseException(
        "Invalid date format for \"" + text + "\". Expected 'yyyy-MM-dd'");
    }
  }
}
