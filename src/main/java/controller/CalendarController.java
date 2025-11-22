package controller;

import controller.features.CalendarFeatures;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import model.DuplicateCalendarException;
import model.Icalendar;
import model.IcalendarSystem;
import view.CalendarView;
import view.IcalendarView;
import view.gui.CalendarGuiView;

import javax.swing.SwingUtilities;
import view.gui.GuiViewAdapter;

/**
 * Unified Controller that handles both Text-based (Headless/Interactive)
 * and GUI-based interactions.
 */
public class CalendarController implements IcalendarController, CalendarFeatures {

  private final IcalendarSystem model;
  private IcalendarView view;        // The generic interface used by Executor (and for text output)
  private CalendarGuiView guiView;   // The specific GUI implementation (only for GUI mode)

  private Icalendar currentCalendar;
  private final CommandParser commandParser;
  private CommandExecutor commandExecutor;

  public CalendarController(IcalendarSystem model) {
    this.model = model;
    this.currentCalendar = null;
    this.commandParser = new CommandParser();
  }

  // ==================== MODE MANAGEMENT ====================

  @Override
  public void run(ApplicationMode mode, String commandFilePath) {
    if (mode == ApplicationMode.GUI) {
      startGuiMode();
    } else {
      startTextMode(mode, commandFilePath);
    }
  }

  private void startGuiMode() {
    // 1. Initialize GUI specific components
    this.guiView = new CalendarGuiView();
    this.guiView.setFeatures(this);

    // 2. Wrap GUI in Adapter so generic Executor works
    this.view = new GuiViewAdapter(this.guiView);
    this.commandExecutor = new CommandExecutor(model, view);

    // 3. Initialize default state
    createDefaultCalendar();
    updateCalendarList();
    if (currentCalendar != null) {
      guiView.updateTimezoneDisplay(currentCalendar.getCalendarTimeZone().getId());
    }

    // 4. Launch
    SwingUtilities.invokeLater(guiView::display);
  }

  private void startTextMode(ApplicationMode mode, String commandFilePath) {
    this.view = new CalendarView();
    this.commandExecutor = new CommandExecutor(model, view);

    ModeManager modeManager = new ModeManager(commandFilePath);
    try {
      modeManager.execute(mode, this);
    } catch (IOException e) {
      view.displayError(e.getMessage());
    }
  }

  // ==================== TEXT INPUT IMPLEMENTATION ====================

  @Override
  public void processCommand(String command) {
    try {
      ParsedCommand cmd = commandParser.parse(command);

      if (cmd.getCommandType() == CommandType.SET_CONTEXT) {
        this.currentCalendar = commandExecutor.handleContextSetting(cmd);
        // Only display text message if not in GUI mode
        if (guiView == null) {
          view.displayMessage("Context set successfully to calendar: " + this.currentCalendar.getCalendarName());
        }
      } else {
        commandExecutor.executeCommand(cmd, currentCalendar);
      }

    } catch (Exception e) {
      if (view != null) view.displayError(e.getMessage());
      else System.err.println("Error: " + e.getMessage());
    }
  }

  @Override
  public void shutDown() {
    System.exit(0);
  }

  @Override
  public IcalendarView getView() {
    return this.view;
  }

  // ==================== GUI FEATURES IMPLEMENTATION ====================
  // Note: These methods preserve the specific try-catch blocks and logic
  // from your original CalendarGuiController.

  private void createDefaultCalendar() {
    try {
      ZoneId systemZone = ZoneId.systemDefault();
      model.createCalendar("Default", systemZone.getId());
      currentCalendar = model.getCalendar("Default");
    } catch (Exception e) {
      guiView.showError("Failed to create default calendar: " + e.getMessage());
    }
  }

  @Override
  public void createCalendar(String name, String timezone) {
    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_CALENDAR)
          .calendarName(name)
          .calendarTimeZone(timezone)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      updateCalendarList();
      guiView.showSuccess("Calendar '" + name + "' created successfully");
    } catch (Exception e) {
      guiView.showError("Failed to create calendar: " + e.getMessage());
    }
  }

  @Override
  public void selectCalendar(String calendarName) {
    try {
      currentCalendar = model.getCalendar(calendarName);
      guiView.setSelectedCalendar(calendarName);

      String tzId = currentCalendar.getCalendarTimeZone().getId();
      guiView.updateTimezoneDisplay(tzId);

      viewEventsOnDate(LocalDate.now());
    } catch (Exception e) {
      guiView.showError("Failed to switch calendar: " + e.getMessage());
    }
  }

  @Override
  public void editCalendarName(String calendarName, String newName) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_CALENDAR_NAME)
          .calendarName(calendarName)
          .propertyToEdit("name")
          .newPropertyValue(newName)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);

      updateCalendarList();
      currentCalendar = model.getCalendar(newName);
      guiView.showSuccess("Calendar renamed successfully to '" + newName + "'");

    } catch (DuplicateCalendarException e) {
      guiView.showError("Calendar renaming failed: A calendar named '" + newName + "' already exists.");
    } catch (Exception e) {
      guiView.showError("Failed to rename calendar: " + e.getMessage());
    }
  }

  @Override
  public void editCalendarTimezone(String calendarName, String newTimezone) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_CALENDAR_ZONE)
          .calendarName(calendarName)
          .propertyToEdit("timezone")
          .newPropertyValue(newTimezone)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      guiView.showSuccess("Timezone updated successfully");

      guiView.updateTimezoneDisplay(newTimezone);
      LocalDate dateToRefresh = guiView.getSelectedDate();
      viewEventsOnDate(dateToRefresh);

    } catch (Exception e) {
      guiView.showError("Failed to update timezone: " + e.getMessage());
    }
  }

  @Override
  public void createSingleEvent(String subject, LocalDateTime start, LocalDateTime end) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_SINGLE_EVENT)
          .subject(subject)
          .startDateTime(start)
          .endDateTime(end)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(start.toLocalDate());
      guiView.showSuccess("Event created successfully");
    } catch (Exception e) {
      guiView.showError("Failed to create event: " + e.getMessage());
    }
  }

  @Override
  public void createAllDayEvent(String subject, LocalDate date) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new  ParsedCommand.Builder(CommandType.CREATE_ALL_DAY_EVENT)
          .subject(subject)
          .startDateTime(date.atTime(8, 0))
          .endDateTime(date.atTime(17, 0))
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(date);
      guiView.showSuccess("All-day event created successfully");
    } catch (Exception e) {
      guiView.showError("Failed to create all-day event: " + e.getMessage());
    }
  }

  @Override
  public void createRecurringEventWithOccurrences(String subject, LocalDateTime start,
                                                  LocalDateTime end,
                                                  Set<DayOfWeekAlphabet> weekdays,
                                                  int occurrences) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
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

      commandExecutor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(start.toLocalDate());
      guiView.showSuccess("Recurring event created successfully");
    } catch (Exception e) {
      guiView.showError("Failed to create recurring event: " + e.getMessage());
    }
  }

  @Override
  public void createRecurringEventUntilDate(String subject, LocalDateTime start,
                                            LocalDateTime end,
                                            Set<DayOfWeekAlphabet> weekdays,
                                            LocalDate endDate) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
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

      commandExecutor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(start.toLocalDate());
      guiView.showSuccess("Recurring event created successfully");
    } catch (Exception e) {
      guiView.showError("Failed to create recurring event: " + e.getMessage());
    }
  }

  @Override
  public void editSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                              String property, String newValue) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
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

      commandExecutor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(start.toLocalDate());
      guiView.showSuccess("Event updated successfully");
    } catch (Exception e) {
      guiView.showError("Failed to edit event: " + e.getMessage());
    }
  }

  @Override
  public void editEventsFromDate(String subject, LocalDateTime startDateTime,
                                 String property, String newValue) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_EVENTS)
          .subject(subject)
          .startDateTime(startDateTime)
          .propertyToEdit(property)
          .newPropertyValue(newValue)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(startDateTime.toLocalDate());
      guiView.showSuccess("Events updated successfully");
    } catch (Exception e) {
      guiView.showError("Failed to edit events: " + e.getMessage());
    }
  }

  @Override
  public void editAllInSeries(String subject, LocalDateTime anyEventStart,
                              String property, String newValue) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_SERIES)
          .subject(subject)
          .startDateTime(anyEventStart)
          .propertyToEdit(property)
          .newPropertyValue(newValue)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      viewEventsOnDate(anyEventStart.toLocalDate());
      guiView.showSuccess("Series updated successfully");
    } catch (Exception e) {
      guiView.showError("Failed to edit series: " + e.getMessage());
    }
  }

  @Override
  public void viewEventsOnDate(LocalDate date) {
    if (currentCalendar == null) {
      if (guiView != null) guiView.displayEvents(null, date);
      return;
    }

    List<model.Event> events = currentCalendar.getEventsOn(date);
    if (guiView != null) guiView.displayEvents(events, date);
  }

  @Override
  public void viewEventsFromDate(LocalDateTime startDateTime) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.PRINT_EVENTS_FROM)
          .startDateTime(startDateTime)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      guiView.showSuccess("Events displayed in console");
    } catch (Exception e) {
      guiView.showError("Failed to query events: " + e.getMessage());
    }
  }

  @Override
  public void checkStatus(LocalDateTime dateTime) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.SHOW_STATUS)
          .startDateTime(dateTime)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      // Executor prints status to console
    } catch (Exception e) {
      guiView.showError("Failed to check status: " + e.getMessage());
    }
  }

  @Override
  public void exportToCSV(String filePath) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EXPORT_CALENDAR_CSV)
          .fileName(filePath)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      guiView.showSuccess("Calendar exported to CSV successfully");
    } catch (Exception e) {
      guiView.showError("Failed to export to CSV: " + e.getMessage());
    }
  }

  @Override
  public void exportToICal(String filePath) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EXPORT_CALENDAR_ICAL)
          .fileName(filePath)
          .build();

      commandExecutor.executeCommand(cmd, currentCalendar);
      guiView.showSuccess("Calendar exported to iCal successfully");
    } catch (Exception e) {
      guiView.showError("Failed to export to iCal: " + e.getMessage());
    }
  }

  private void updateCalendarList() {
    if (guiView != null) {
      List<String> calendarNames = model.getAllCalendarNames();
      guiView.setCalendarNames(calendarNames);
      if (currentCalendar != null) {
        guiView.setSelectedCalendar(currentCalendar.getCalendarName());
      }
    }
  }
}