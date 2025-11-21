// controller/CalendarGuiController.java
package controller;

import controller.features.CalendarFeatures;
import java.util.Set;
import model.Event;
import model.IcalendarSystem;
import model.Icalendar;
import view.CalendarView;
import view.IcalendarView;
import view.gui.CalendarGuiView;
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
public class CalendarGuiController implements CalendarFeatures {
  private final IcalendarSystem model;
  private final CalendarGuiView view;
  private final CommandExecutor executor;
  private Icalendar currentCalendar;

  public CalendarGuiController(IcalendarSystem model) {
    this.model = model;
    this.view = new CalendarGuiView();
    IcalendarView textView = new CalendarView();
    this.executor = new CommandExecutor(model, textView);

    view.setFeatures(this);

    // Create default calendar
    createDefaultCalendar();

    // Initial update
    updateCalendarList();
    if (currentCalendar != null) {
      String defaultTz = currentCalendar.getCalendarTimeZone().getId();;
      view.updateTimezoneDisplay(defaultTz);
    }
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

  // ==================== CALENDAR MANAGEMENT ====================

  @Override
  public void createCalendar(String name, String timezone) {
    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_CALENDAR)
          .calendarName(name)
          .calendarTimeZone(timezone)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      updateCalendarList();
      view.showSuccess("Calendar '" + name + "' created successfully");
    } catch (Exception e) {
      view.showError("Failed to create calendar: " + e.getMessage());
    }
  }

// controller/CalendarGuiController.java

  @Override
  public void selectCalendar(String calendarName) {
    try {
      currentCalendar = model.getCalendar(calendarName);
      view.setSelectedCalendar(calendarName);

      // ADD: Update Timezone Display
      String tzId = currentCalendar.getCalendarTimeZone().getId();;
      view.updateTimezoneDisplay(tzId);

      viewEventsOnDate(LocalDate.now());
    } catch (Exception e) {
      view.showError("Failed to switch calendar: " + e.getMessage());
    }
  }

  @Override
  public void editCalendarName(String calendarName, String newName) {
    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_CALENDAR_NAME)
          .calendarName(calendarName)
          .propertyToEdit("name")
          .newPropertyValue(newName)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      updateCalendarList();
      view.showSuccess("Calendar renamed successfully");
    } catch (Exception e) {
      view.showError("Failed to rename calendar: " + e.getMessage());
    }
  }

// controller/CalendarGuiController.java

// controller/CalendarGuiController.java

  @Override
  public void editCalendarTimezone(String calendarName, String newTimezone) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_CALENDAR_ZONE)
          .calendarName(calendarName)
          .propertyToEdit("timezone")
          .newPropertyValue(newTimezone)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      view.showSuccess("Timezone updated successfully");

      // ADD: Update Timezone Display
      view.updateTimezoneDisplay(newTimezone);

      // ADD: Refresh events using the *selected* date from the view
      LocalDate dateToRefresh = view.getSelectedDate();
      viewEventsOnDate(dateToRefresh);

    } catch (Exception e) {
      view.showError("Failed to update timezone: " + e.getMessage());
    }
  }

  // ==================== EVENT CREATION ====================

  @Override
  public void createSingleEvent(String subject, LocalDateTime start, LocalDateTime end) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_SINGLE_EVENT)
          .subject(subject)
          .startDateTime(start)
          .endDateTime(end)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(start.toLocalDate());
      view.showSuccess("Event created successfully");
    } catch (Exception e) {
      view.showError("Failed to create event: " + e.getMessage());
    }
  }

  @Override
  public void createAllDayEvent(String subject, LocalDate date) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new  ParsedCommand.Builder(CommandType.CREATE_ALL_DAY_EVENT)
          .subject(subject)
          .startDateTime(date.atTime(8, 0))
          .endDateTime(date.atTime(17, 0))
          .build();

      executor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(date);
      view.showSuccess("All-day event created successfully");
    } catch (Exception e) {
      view.showError("Failed to create all-day event: " + e.getMessage());
    }
  }

  @Override
  public void createRecurringEventWithOccurrences(String subject, LocalDateTime start,
                                                  LocalDateTime end,
                                                  Set<DayOfWeekAlphabet> weekdays,
                                                  int occurrences) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_SERIES_REPEATING_N_TIMES)
          .subject(subject)
          .startDateTime(start)
          .endDateTime(end)
          .weekdays(weekdays)
          .occurrences(occurrences)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(start.toLocalDate());
      view.showSuccess("Recurring event created successfully");
    } catch (Exception e) {
      view.showError("Failed to create recurring event: " + e.getMessage());
    }
  }

  @Override
  public void createRecurringEventUntilDate(String subject, LocalDateTime start,
                                            LocalDateTime end,
                                            Set<DayOfWeekAlphabet> weekdays,
                                            LocalDate endDate) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_SERIES_UNTIL)
          .subject(subject)
          .startDateTime(start)
          .endDateTime(end)
          .weekdays(weekdays)
          .seriesEndDate(endDate)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(start.toLocalDate());
      view.showSuccess("Recurring event created successfully");
    } catch (Exception e) {
      view.showError("Failed to create recurring event: " + e.getMessage());
    }
  }

  // ==================== EVENT EDITING ====================

  @Override
  public void editSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                              String property, String newValue) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_SINGLE_EVENT)
          .subject(subject)
          .startDateTime(start)
          .endDateTime(end)
          .propertyToEdit(property)
          .newPropertyValue(newValue)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(start.toLocalDate());
      view.showSuccess("Event updated successfully");
    } catch (Exception e) {
      view.showError("Failed to edit event: " + e.getMessage());
    }
  }

  @Override
  public void editEventsFromDate(String subject, LocalDateTime startDateTime,
                                 String property, String newValue) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_EVENTS)
          .subject(subject)
          .startDateTime(startDateTime)
          .propertyToEdit(property)
          .newPropertyValue(newValue)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(startDateTime.toLocalDate());
      view.showSuccess("Events updated successfully");
    } catch (Exception e) {
      view.showError("Failed to edit events: " + e.getMessage());
    }
  }

  @Override
  public void editAllInSeries(String subject, LocalDateTime anyEventStart,
                              String property, String newValue) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_SERIES)
          .subject(subject)
          .startDateTime(anyEventStart)
          .propertyToEdit(property)
          .newPropertyValue(newValue)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(anyEventStart.toLocalDate());
      view.showSuccess("Series updated successfully");
    } catch (Exception e) {
      view.showError("Failed to edit series: " + e.getMessage());
    }
  }

  // ==================== EVENT QUERYING ====================

  @Override
  public void viewEventsOnDate(LocalDate date) {
    if (currentCalendar == null) {
      view.displayEvents(null, date);
      return;
    }

    List<Event> events = currentCalendar.getEventsOn(date);
    view.displayEvents(events, date);
  }

  @Override
  public void viewEventsFromDate(LocalDateTime startDateTime) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.PRINT_EVENTS_FROM)
          .startDateTime(startDateTime)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      view.showSuccess("Events displayed in console");
    } catch (Exception e) {
      view.showError("Failed to query events: " + e.getMessage());
    }
  }

  @Override
  public void checkStatus(LocalDateTime dateTime) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.SHOW_STATUS)
          .startDateTime(dateTime)
          .build();

      executor.executeCommand(cmd, currentCalendar);
    } catch (Exception e) {
      view.showError("Failed to check status: " + e.getMessage());
    }
  }

  // ==================== EVENT COPYING ====================

  @Override
  public void copySingleEvent(String subject, LocalDateTime sourceDateTime,
                              LocalDateTime targetDateTime, String targetCalendarName) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.COPY_SINGLE_EVENT)
          .subject(subject)
          .startDateTime(sourceDateTime)
          .targetDateTime(targetDateTime)
          .targetCalendarName(targetCalendarName)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      view.showSuccess("Event copied successfully");
    } catch (Exception e) {
      view.showError("Failed to copy event: " + e.getMessage());
    }
  }

  @Override
  public void copyEventsOnDate(LocalDate sourceDate, LocalDate targetDate,
                               String targetCalendarName) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.COPY_EVENTS_ON_DATE)
          .startDateTime(sourceDate.atStartOfDay())
          .targetDateTime(targetDate.atStartOfDay())
          .targetCalendarName(targetCalendarName)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      view.showSuccess("Events copied successfully");
    } catch (Exception e) {
      view.showError("Failed to copy events: " + e.getMessage());
    }
  }

  @Override
  public void copyEventsBetweenDates(LocalDate sourceStartDate, LocalDate sourceEndDate,
                                     LocalDate targetStartDate, String targetCalendarName) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.COPY_EVENTS_BETWEEN_DATES)
          .intervalStartDate(sourceStartDate)
          .intervalEndDate(sourceEndDate)
          .targetDate(targetStartDate)
          .targetCalendarName(targetCalendarName)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      view.showSuccess("Events copied successfully");
    } catch (Exception e) {
      view.showError("Failed to copy events: " + e.getMessage());
    }
  }

  // ==================== EXPORT ====================

  @Override
  public void exportToCSV(String filePath) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EXPORT_CALENDAR_CSV)
          .fileName(filePath)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      view.showSuccess("Calendar exported to CSV successfully");
    } catch (Exception e) {
      view.showError("Failed to export to CSV: " + e.getMessage());
    }
  }

  @Override
  public void exportToICal(String filePath) {
    if (currentCalendar == null) {
      view.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EXPORT_CALENDAR_ICAL)
          .fileName(filePath)
          .build();

      executor.executeCommand(cmd, currentCalendar);
      view.showSuccess("Calendar exported to iCal successfully");
    } catch (Exception e) {
      view.showError("Failed to export to iCal: " + e.getMessage());
    }
  }

  // ==================== HELPER METHODS ====================

  private void updateCalendarList() {
    List<String> calendarNames = model.getAllCalendarNames();
    view.setCalendarNames(calendarNames);

    if (currentCalendar != null) {
      view.setSelectedCalendar(currentCalendar.getCalendarName());
    }
  }
  public void run() {
    SwingUtilities.invokeLater(view::display);
  }


}