// controller/CalendarGuiController.java
package controller;

import model.Event;
import model.IcalendarSystem;
import model.Icalendar;
import view.gui.CalendarGuiView;
import view.gui.CalendarViewListener;
import javax.swing.SwingUtilities;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GUI Controller - implements listener to handle view events.
 * Only the controller calls the model.
 */
public class CalendarGuiController implements CalendarViewListener {
  private final IcalendarSystem model;
  private final CalendarGuiView view;
  private Icalendar currentCalendar;

  public CalendarGuiController(IcalendarSystem model) {
    this.model = model;
    this.view = new CalendarGuiView();

    // Register as listener
    view.addListener(this);

    // Create default calendar
    createDefaultCalendar();

    // Initial update
    updateCalendarList();
  }

  private void createDefaultCalendar() {
    try {
      ZoneId systemZone = ZoneId.systemDefault();
      model.createCalendar("Default", systemZone.getId());
      currentCalendar = model.getCalendar("Default");
    } catch (Exception e) {
      view.showError("Failed to create default calendar: " + e.getMessage());
    }
  }

  // Implement CalendarViewListener methods

  @Override
  public void onCreateCalendarRequested(String name, String timezone) {
    try {
      model.createCalendar(name, timezone);
      updateCalendarList();
      view.showSuccess("Calendar '" + name + "' created successfully");
    } catch (Exception e) {
      view.showError("Failed to create calendar: " + e.getMessage());
    }
  }

  @Override
  public void onCalendarSelected(String calendarName) {
    try {
      currentCalendar = model.getCalendar(calendarName);
      view.setSelectedCalendar(calendarName);
      // Refresh event display if date is selected
      onDateSelected(LocalDate.now());
    } catch (Exception e) {
      view.showError("Failed to switch calendar: " + e.getMessage());
    }
  }

  @Override
  public void onDateSelected(LocalDate date) {
    updateEventDisplay(date);
  }

  @Override
  public void onCreateEventRequested(LocalDate date, String subject,
                                     LocalDate startDate, int startHour, int startMinute,
                                     LocalDate endDate, int endHour, int endMinute) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      LocalDateTime start = LocalDateTime.of(startDate,
          java.time.LocalTime.of(startHour, startMinute));
      LocalDateTime end = LocalDateTime.of(endDate,
          java.time.LocalTime.of(endHour, endMinute));

      currentCalendar.addEvent(subject, start, end);
      updateEventDisplay(date);
      view.showSuccess("Event created successfully");
    } catch (Exception e) {
      view.showError("Failed to create event: " + e.getMessage());
    }
  }

  @Override
  public void onEditEventRequested(LocalDate date, String eventIdentifier,
                                   String newSubject, int newStartHour, int newStartMinute,
                                   int newEndHour, int newEndMinute) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      // Find the event by name and date
      List<Event> events = currentCalendar.getEventsOn(date);
      Event targetEvent = null;

      for (Event event : events) {
        if (event.getSubject().equalsIgnoreCase(eventIdentifier)) {
          targetEvent = event;
          break;
        }
      }

      if (targetEvent == null) {
        view.showError("Event not found: " + eventIdentifier);
        return;
      }

      // Edit the event
      currentCalendar.editEvent(
          targetEvent.getSubject(),
          targetEvent.getStart(),
          targetEvent.getEnd(),
          "subject",
          newSubject
      );

      updateEventDisplay(date);
      view.showSuccess("Event updated successfully");
    } catch (Exception e) {
      view.showError("Failed to edit event: " + e.getMessage());
    }
  }

  @Override
  public void onMonthChanged(int year, int month) {
    // Could update display if needed
  }

  // Helper methods

  private void updateCalendarList() {
    List<String> calendarNames = getAllCalendarNames();
    view.setCalendarNames(calendarNames);

    if (currentCalendar != null) {
      view.setSelectedCalendar(currentCalendar.getCalendarName());
    }
  }

  private void updateEventDisplay(LocalDate date) {
    if (currentCalendar == null) {
      view.setEventDisplayText("No calendar selected");
      return;
    }

    List<Event> events = currentCalendar.getEventsOn(date);

    if (events.isEmpty()) {
      view.setEventDisplayText("No events on " + date);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("Events on ").append(date).append(":\n\n");
      DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm a");

      for (Event event : events) {
        sb.append("• ").append(event.getSubject()).append("\n");
        sb.append("  ").append(event.getStart().format(timeFormat))
            .append(" - ").append(event.getEnd().format(timeFormat)).append("\n");

        if (event.getSeriesId() != null) {
          sb.append("  (Recurring)\n");
        }
        sb.append("\n");
      }

      view.setEventDisplayText(sb.toString());
    }
  }

  private List<String> getAllCalendarNames() {
    // You'll need to add this method to IcalendarSystem
    return model.getAllCalendarNames();
  }

  public void run() {
    SwingUtilities.invokeLater(view::display);
  }
}