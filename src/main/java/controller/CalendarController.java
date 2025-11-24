package controller;

import controller.features.CalendarFeatures;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import model.Icalendar;
import model.IcalendarSystem;
import view.CalendarView;
import view.IcalendarView;
import view.gui.CalendarGuiView;
import view.gui.GuiViewAdapter;
import view.gui.IcalendarGuiView;

/**
 * Unified Controller that handles both Text-based (Headless/Interactive)
 * and GUI-based interactions.
 * This class implements both the generic IcalendarController lifecycle and
 * the specific CalendarFeatures interface for GUI interactions.
 */
public class CalendarController implements IcalendarController, CalendarFeatures {

  private final IcalendarSystem model;
  private final CommandParser commandParser;
  private IcalendarView view;        // The generic interface used by Executor (and for text output)
  private IcalendarGuiView guiView;   // The specific GUI implementation (only for GUI mode)
  private Icalendar currentCalendar;
  private CommandExecutor commandExecutor;

  /**
   * Constructs the CalendarController.
   *
   * @param model The application's data model (IcalendarSystem).
   */
  public CalendarController(IcalendarSystem model) {
    this.model = model;
    this.currentCalendar = null;
    this.commandParser = new CommandParser();
  }

  // ==================== MODE MANAGEMENT ====================

  @Override
  public void run(ApplicationMode mode, String commandFilePath) {
    this.view = new CalendarView();

    if (mode == ApplicationMode.GUI) {
      startGuiMode();
    }

    this.commandExecutor = new CommandExecutor(model, view);
    ModeManager modeManager = new ModeManager(commandFilePath);

    try {
      modeManager.execute(mode, this);

    } catch (IOException e) {
      view.displayError(e.getMessage());
    }
  }

  /**
   * Initializes components specific to the GUI mode and starts the Swing thread.
   */
  private void startGuiMode() {
    // 1. Initialize GUI specific components
    this.guiView = new CalendarGuiView();
    this.guiView.setFeatures(this);

    // 2. Wrap GUI in Adapter so generic Executor works
    this.view = new GuiViewAdapter(this.guiView);

    // 3. Initialize default state
    createDefaultCalendar();
    updateCalendarList();

    SwingUtilities.invokeLater(guiView::display);
  }


  // ==================== TEXT INPUT IMPLEMENTATION ====================

  @Override
  public void processCommand(String command) {
    try {
      ParsedCommand cmd = commandParser.parse(command);

      if (cmd.getCommandType() == CommandType.SET_CONTEXT) {
        this.currentCalendar = commandExecutor.handleContextSetting(cmd);
        view.displayMessage(
            "Context set successfully to calendar: " + this.currentCalendar.getCalendarName());

      } else {
        commandExecutor.executeCommand(cmd, currentCalendar);
      }

    } catch (Exception e) {
      view.displayError(e.getMessage());
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

  /**
   * Creates a default calendar if none exists and sets it as the current context.
   */
  protected void createDefaultCalendar() {
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
    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_CALENDAR)
        .calendarName(name)
        .calendarTimeZone(timezone)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
    updateCalendarList();
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

      // Execution happens, throws exception on duplicate
      commandExecutor.executeCommand(cmd, currentCalendar);

      // --- SUCCESS PATH (Only runs if execution succeeded) ---
      updateCalendarList();
      currentCalendar = model.getCalendar(newName);

    } catch (model.DuplicateCalendarException e) {
      // Catch specific exception and show GUI error
      guiView.showError(
          "Calendar renaming failed: A calendar named '" + newName + "' already exists.");

    } catch (Exception e) {
      // Catch other errors (CalendarNotFoundException, etc.)
      guiView.showError("Failed to rename calendar: " + e.getMessage());
    }
  }

  @Override
  public void editCalendarTimezone(String calendarName, String newTimezone) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_CALENDAR_ZONE)
        .calendarName(calendarName)
        .propertyToEdit("timezone")
        .newPropertyValue(newTimezone)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);

    guiView.updateTimezoneDisplay(newTimezone);
    LocalDate dateToRefresh = guiView.getSelectedDate();
    viewEventsOnDate(dateToRefresh);
  }

  @Override
  public void createSingleEvent(String subject, LocalDateTime start, LocalDateTime end) {
    // The command executor handles the null currentCalendar check via IllegalStateException
    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_SINGLE_EVENT)
        .subject(subject)
        .startDateTime(start)
        .endDateTime(end)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
    viewEventsOnDate(start.toLocalDate());
  }

  @Override
  public void createAllDayEvent(String subject, LocalDate date) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_ALL_DAY_EVENT)
        .subject(subject)
        .startDateTime(date.atTime(8, 0))
        .endDateTime(date.atTime(17, 0))
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
    viewEventsOnDate(date);
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

    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_SERIES_REPEATING_N_TIMES)
        .subject(subject)
        .startDateTime(start)
        .endDateTime(end)
        .weekdays(weekdays)
        .occurrences(occurrences)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
    viewEventsOnDate(start.toLocalDate());
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

    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_SERIES_UNTIL)
        .subject(subject)
        .startDateTime(start)
        .endDateTime(end)
        .weekdays(weekdays)
        .seriesEndDate(endDate)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
    viewEventsOnDate(start.toLocalDate());
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
    } catch (IllegalStateException e) {
      view.displayError(e.getMessage());
    }
    viewEventsOnDate(start.toLocalDate());
  }

  @Override
  public void editEventsFromDate(String subject, LocalDateTime startDateTime,
                                 String property, String newValue) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }
    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_EVENTS)
        .subject(subject)
        .startDateTime(startDateTime)
        .propertyToEdit(property)
        .newPropertyValue(newValue)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
    viewEventsOnDate(startDateTime.toLocalDate());
  }

  @Override
  public void editAllInSeries(String subject, LocalDateTime anyEventStart,
                              String property, String newValue) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }
    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EDIT_SERIES)
        .subject(subject)
        .startDateTime(anyEventStart)
        .propertyToEdit(property)
        .newPropertyValue(newValue)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
    viewEventsOnDate(anyEventStart.toLocalDate());
  }

  @Override
  public void viewEventsOnDate(LocalDate date) {
    if (currentCalendar == null) {
      if (guiView != null) {
        guiView.displayEvents(null, date);
      }
      return;
    }

    List<model.Event> events = currentCalendar.getEventsOn(date);
    if (guiView != null) {
      guiView.displayEvents(events, date);
    }
  }

  @Override
  public void viewEventsFromDate(LocalDateTime startDateTime) {

    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.PRINT_EVENTS_FROM)
        .startDateTime(startDateTime)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
  }

  @Override
  public void checkStatus(LocalDateTime dateTime) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }
    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.SHOW_STATUS)
        .startDateTime(dateTime)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
  }

  @Override
  public void exportToCsv(String filePath) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EXPORT_CALENDAR_CSV)
        .fileName(filePath)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
  }

  @Override
  public void exportToIcal(String filePath) {
    if (currentCalendar == null) {
      guiView.showError("No calendar selected");
      return;
    }

    ParsedCommand cmd = new ParsedCommand.Builder(CommandType.EXPORT_CALENDAR_ICAL)
        .fileName(filePath)
        .build();

    commandExecutor.executeCommand(cmd, currentCalendar);
  }

  /**
   * Updates the list of available calendar names in the GUI's dropdown selector.
   */
  void updateCalendarList() {
    if (guiView != null) {
      List<String> calendarNames = model.getAllCalendarNames();
      guiView.setCalendarNames(calendarNames);
      if (currentCalendar != null) {
        guiView.setSelectedCalendar(currentCalendar.getCalendarName());
      }
    }
  }
}