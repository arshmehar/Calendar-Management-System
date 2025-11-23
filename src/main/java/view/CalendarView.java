package view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.Event;

/**
 * Calendar view implementation for headless and interactive mode.
 * Handles all text based display functionality. Does not handle
 * reading input, only display.
 */
public class CalendarView implements IcalendarView {


  private final DateTimeFormatter dateFormatter;
  private final DateTimeFormatter dateTimeFormatter;

  /**
   * Constructs an CalendarView with standard date and time formatters.
   * Date format: yyyy-MM-dd
   * DateTime format: yyyy-MM-dd HH:mm
   */
  public CalendarView() {
    this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    this.dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  }

  /**
   * Displays a welcome message when the application starts.
   */
  @Override
  public void displayWelcome() {
    System.out.println("Welcome to Calendar Application!");
    System.out.println("Type 'exit' to quit.");
    System.out.println();
  }

  /**
   * Displays a prompt for the user to enter a command.
   */
  @Override
  public void displayPrompt() {
    System.out.print("> ");
  }

  /**
   * Displays a success message to the user.
   *
   * @param message the success message to display
   */
  @Override
  public void displaySuccess(String message) {
    System.out.println("Success: " + message);
  }

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to display
   */
  @Override
  public void displayError(String message) {
    System.out.println("Error: " + message);
  }

  /**
   * Displays the user's busy/available status at a specific date and time.
   *
   * @param isBusy true if the user is busy, false if available
   * @param date   the date and time to check
   */
  @Override
  public void displayStatus(boolean isBusy, LocalDateTime date) {
    String st;
    if (isBusy) {
      st = "Busy";
    } else {
      st = "Available";
    }
    System.out.println("Status at " + date + " is: " + st);
  }

  /**
   * Displays a general message to the user.
   *
   * @param message the message to display
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Displays all events scheduled on a specific date in bulleted format.
   *
   * @param events list of events to display
   * @param date   the date for which events are being displayed
   */
  @Override
  public void displayEventsForDate(List<Event> events, LocalDate date) {
    System.out.println("Events on " + date.format(dateFormatter) + ":");

    if (events.isEmpty()) {
      System.out.println("  No events scheduled");
      return;
    }

    for (Event event : events) {
      displayEventBullet(event);
    }
  }

  /**
   * Displays all events within a specific date/time range with full details.
   *
   * @param events list of events to display
   * @param start  the start of the time range
   * @param end    the end of the time range
   */
  @Override
  public void displayEventsForRange(List<Event> events, LocalDateTime start, LocalDateTime end) {
    System.out.println("Events from " + start.format(dateTimeFormatter)
        + " to " + end.format(dateTimeFormatter) + ":");

    if (events.isEmpty()) {
      System.out.println("  No events in this range");
      return;
    }

    for (Event event : events) {
      displayEventWithFullDetails(event);
    }
  }

  /**
   * Displays a goodbye message when the application exits.
   */
  @Override
  public void displayGoodbye() {
    System.out.println("Goodbye!");
  }

  /**
   * Displays an event in bulleted format with time and location.
   *
   * @param event the event to display
   */
  protected void displayEventBullet(Event event) {
    System.out.print("  • " + event.getSubject());
    System.out.print(": " + formatTime(event.getStart()));
    System.out.print(" - " + formatTime(event.getEnd()));

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      System.out.print(". Location: " + event.getLocation());
    }

    System.out.println();
  }

  /**
   * Displays an event with full details including dates, times, and location.
   *
   * @param event the event to display
   */
  protected void displayEventWithFullDetails(Event event) {
    System.out.print("  • " + event.getSubject());
    System.out.print(" starting on " + event.getStart().toLocalDate().format(dateFormatter));
    System.out.print(" at " + formatTime(event.getStart()));
    System.out.print(", ending on " + event.getEnd().toLocalDate().format(dateFormatter));
    System.out.print(" at " + formatTime(event.getEnd()));

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      System.out.print(". Location: " + event.getLocation());
    }

    System.out.println();
  }

  /**
   * Formats a LocalDateTime as a time string (HH:mm).
   *
   * @param dateTime the date time to format
   * @return formatted time string
   */
  protected String formatTime(LocalDateTime dateTime) {
    return String.format("%02d:%02d", dateTime.getHour(), dateTime.getMinute());
  }


}
