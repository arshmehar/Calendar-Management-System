package controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import model.Event;
import model.EventStatus;

/**
 * Exports calendar events to CSV format compatible with Google Calendar.
 * Handles CSV field escaping and formatting according to Google Calendar specifications.
 */
public class CsvExporter {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");

  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("hh:mm a");

  /**
   * Exports events to a CSV file.
   *
   * @param events   set of events to export
   * @param filePath path where the CSV file should be created
   * @return absolute path of the created CSV file
   * @throws IOException if file cannot be written
   */
  protected String exportToCsv(Set<Event> events, String filePath) throws IOException {

    Path path = Paths.get(filePath);
    if (path.getParent() != null) {
      Files.createDirectories(path.getParent());
    }

    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      writeHeader(writer);

      for (Event event : events) {
        writeEventRow(writer, event);
      }
    }

    return path.toAbsolutePath().toString();
  }

  /**
   * Writes the CSV header row.
   *
   * @param writer the buffered writer
   * @throws IOException if write operation fails
   */
  private void writeHeader(BufferedWriter writer) throws IOException {
    writer.write(
        "Subject,Start Date,Start Time,End Date,End Time,"
            + "All Day Event,Description,Location,Private");
    writer.newLine();
  }

  /**
   * Writes a single event as a CSV row.
   *
   * @param writer the buffered writer
   * @param event  the event to write
   * @throws IOException if write operation fails
   */
  private void writeEventRow(BufferedWriter writer, Event event) throws IOException {

    String row = escapeCsvField(event.getSubject()) + ","

        + formatDate(event.getStart()) + ","

        + formatTime(event.getStart()) + ","

        + formatDate(event.getEnd()) + ","

        + formatTime(event.getEnd()) + ","

        + (isAllDayEvent(event) ? "True" : "False") + ","

        + escapeCsvField(event.getDescription()) + ","

        + escapeCsvField(event.getLocation()) + ","

        + escapeCsvField(privateEventStatus(event)) + ",";

    writer.write(row);
    writer.newLine();
  }

  /**
   * Formats a LocalDateTime as a date string.
   *
   * @param dateTime the date time to format
   * @return formatted date string in MM/dd/yyyy format
   */
  private String formatDate(LocalDateTime dateTime) {
    return dateTime.format(DATE_FORMATTER);
  }

  /**
   * Formats a LocalDateTime as a time string.
   *
   * @param dateTime the date time to format
   * @return formatted time string in hh:mm a format
   */
  private String formatTime(LocalDateTime dateTime) {
    return dateTime.format(TIME_FORMATTER);
  }

  /**
   * Determines if an event is an all-day event.
   * An all-day event starts at 8:00 AM and ends at 5:00 PM on the same day.
   *
   * @param event the event to check
   * @return true if the event is an all-day event, false otherwise
   */
  private boolean isAllDayEvent(Event event) {
    LocalDateTime start = event.getStart();
    LocalDateTime end = event.getEnd();

    return start.getHour() == 8 && start.getMinute() == 0
        && end.getHour() == 17 && end.getMinute() == 0
        && start.toLocalDate().equals(end.toLocalDate());
  }

  /**
   * Converts event status to CSV format.
   *
   * @param event the event
   * @return "True" if private, "False" if public, null if status not set
   */
  private String privateEventStatus(Event event) {
    EventStatus status = event.getStatus();
    if (status == null) {
      return null;
    }
    if (status == EventStatus.PRIVATE) {
      return "True";
    } else if (status == EventStatus.PUBLIC) {
      return "False";
    }
    return null;
  }

  /**
   * Escapes a CSV field according to RFC 4180.
   * Wraps field in quotes if it contains commas, quotes, or newlines.
   *
   * @param field the field to escape
   * @return escaped field suitable for CSV
   */
  private String escapeCsvField(String field) {
    if (field == null || field.isEmpty()) {
      return "";
    }

    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      String escaped = field.replace("\"", "\"\"");
      return "\"" + escaped + "\"";
    }

    return field;
  }
}