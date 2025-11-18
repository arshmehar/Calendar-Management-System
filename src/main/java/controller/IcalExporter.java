package controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import model.Event;
import model.EventStatus;

/**
 * Exports calendar events to iCal format (.ics file).
 * Handles iCal formatting according to RFC 5545 specifications.
 */
public class IcalExporter {

  private static final DateTimeFormatter ICAL_DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  private static final int MAX_LINE_LENGTH = 75;

  /**
   * Exports events to an iCal file.
   *
   * @param events   set of events to export
   * @param filePath path where the iCal file should be created
   * @return absolute path of the created iCal file
   * @throws IOException if file cannot be written
   */
  protected String exportToIcal(Set<Event> events, String filePath) throws IOException {

    Path path = Paths.get(filePath);
    if (path.getParent() != null) {
      Files.createDirectories(path.getParent());
    }

    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      writeCalendarHeader(writer);

      for (Event event : events) {
        writeEventComponent(writer, event);
      }

      writeCalendarFooter(writer);
    }

    return path.toAbsolutePath().toString();
  }

  /**
   * Writes the iCal calendar header.
   *
   * @param writer the buffered writer
   * @throws IOException if write operation fails
   */
  private void writeCalendarHeader(BufferedWriter writer) throws IOException {
    writer.write("BEGIN:VCALENDAR");
    writer.newLine();
    writer.write("VERSION:2.0");
    writer.newLine();
    writer.write("PRODID:-//Calendar Application//EN");
    writer.newLine();
    writer.write("CALSCALE:GREGORIAN");
    writer.newLine();
  }

  /**
   * Writes the iCal calendar footer.
   *
   * @param writer the buffered writer
   * @throws IOException if write operation fails
   */
  private void writeCalendarFooter(BufferedWriter writer) throws IOException {
    writer.write("END:VCALENDAR");
    writer.newLine();
  }

  /**
   * Writes a single event as an iCal VEVENT component.
   *
   * @param writer the buffered writer
   * @param event  the event to write
   * @throws IOException if write operation fails
   */
  private void writeEventComponent(BufferedWriter writer, Event event) throws IOException {
    writer.write("BEGIN:VEVENT");
    writer.newLine();

    writeField(writer, "UID", generateUid(event));

    writeField(writer, "DTSTAMP", formatDateTime(LocalDateTime.now()));

    writeField(writer, "DTSTART", formatDateTime(event.getStart()));

    writeField(writer, "DTEND", formatDateTime(event.getEnd()));


    writeField(writer, "SUMMARY", escapeIcalText(event.getSubject()));


    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      writeField(writer, "DESCRIPTION", escapeIcalText(event.getDescription()));
    }


    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      writeField(writer, "LOCATION", escapeIcalText(event.getLocation()));
    }


    EventStatus status = event.getStatus();
    if (status != null) {
      String classification = status == EventStatus.PRIVATE ? "PRIVATE" : "PUBLIC";
      writeField(writer, "CLASS", classification);
    }

    writer.write("END:VEVENT");
    writer.newLine();
  }

  /**
   * Formats a LocalDateTime as an iCal datetime string.
   *
   * @param dateTime the date time to format
   * @return formatted datetime string in yyyyMMddTHHmmss format
   */
  private String formatDateTime(LocalDateTime dateTime) {
    return dateTime.format(ICAL_DATETIME_FORMATTER);
  }

  /**
   * Generates a stable unique identifier for an event.
   * For events in a series, uses the seriesId to maintain relationship between occurrences.
   * For single events, generates a deterministic UUID from the event's composite key.
   *
   * @param event the event
   * @return a stable unique identifier string
   */
  private String generateUid(Event event) {

    if (event.getSeriesId() != null) {
      return event.getSeriesId() + "-" + event.getStart().toString()
          + "@calendar-app";
    }

    String uniqueString = event.getSubject().toLowerCase() + "|"
        + event.getStart().toString() + "|" + event.getEnd().toString();

    return UUID.nameUUIDFromBytes(uniqueString.getBytes()).toString()
        + "@calendar-app";
  }

  /**
   * Escapes special characters in iCal text fields.
   * Escapes semicolons, commas, backslashes, and newlines according to RFC 5545.
   *
   * @param text the text to escape
   * @return escaped text suitable for iCal
   */
  private String escapeIcalText(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }

    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n");
  }

  /**
   * Writes a field with proper line folding according to RFC 5545.
   * Lines longer than 75 characters are folded with a space continuation.
   *
   * @param writer the buffered writer
   * @param name   the field name
   * @param value  the field value
   * @throws IOException if write operation fails
   */
  private void writeField(BufferedWriter writer, String name, String value)
      throws IOException {
    String line = name + ":" + value;

    if (line.length() <= MAX_LINE_LENGTH) {
      writer.write(line);
      writer.newLine();
    } else {

      writer.write(line.substring(0, MAX_LINE_LENGTH));
      writer.newLine();

      int position = MAX_LINE_LENGTH;
      while (position < line.length()) {
        writer.write(" ");
        int endPosition = Math.min(position + MAX_LINE_LENGTH - 1, line.length());
        writer.write(line.substring(position, endPosition));
        writer.newLine();
        position = endPosition;
      }
    }
  }
}