package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import model.CalendarNotFoundException;
import model.DuplicateCalendarException;
import model.DuplicateEventException;
import model.Event;
import model.EventNotFoundException;
import model.Icalendar;
import model.IcalendarSystem;
import model.InvalidTimezoneException;
import view.IcalendarView;

class CommandExecutor {

  private Icalendar currentCalendar;
  private final IcalendarSystem model;
  private final IcalendarView view;

  public CommandExecutor(IcalendarSystem model, IcalendarView view) {
    this.model = model;
    this.view = view;
  }

  protected void executeCommand(ParsedCommand cmd, Icalendar currentCalendar)
      throws IllegalStateException {
    CommandType type = cmd.getCommandType();
    this.currentCalendar = currentCalendar;

    if (currentCalendar == null && !cmd.isCalendarSystemOperation()) {
      throw new IllegalStateException(
          "Current calendar context has not been set. Command Invalid.");
    }

    switch (type) {
      case CREATE_SINGLE_EVENT:
        handleSingleEventCreation(cmd);
        break;

      case CREATE_ALL_DAY_EVENT:
        handleSingleEventCreation(cmd);
        break;

      case CREATE_SERIES_REPEATING_N_TIMES:
        handleNseriesCreation(cmd);
        break;

      case CREATE_SERIES_UNTIL:
        handleSeriesUntilCreation(cmd);
        break;

      case EDIT_SINGLE_EVENT:
        handleSingleEventEditing(cmd);
        break;

      case EDIT_EVENTS:
        handleEventsEditing(cmd);
        break;

      case EDIT_SERIES:
        handleEventSeriesEditing(cmd);
        break;

      case PRINT_EVENTS_ON:
        handlePrintEventsOnQuery(cmd);
        break;

      case PRINT_EVENTS_FROM:
        handlePrintEventsFromQuery(cmd);
        break;

      case SHOW_STATUS:
        handleStatus(cmd);
        break;

      case EXPORT_CALENDAR_CSV:
        handleExportCsv(cmd);
        break;

      case EXPORT_CALENDAR_ICAL:
        handleExportIcal(cmd);
        break;

      case CREATE_CALENDAR:
        handleCreateCalendar(cmd);
        break;

      case EDIT_CALENDAR_NAME:
        handleEditCalendar(cmd, true);
        break;

      case EDIT_CALENDAR_ZONE:
        handleEditCalendar(cmd, false);
        break;

      case COPY_SINGLE_EVENT:
        handleCopySingleEvent(cmd);
        break;

      case COPY_EVENTS_ON_DATE:
        handleCopyEventsOnDate(cmd);
        break;

      case COPY_EVENTS_BETWEEN_DATES:
        handleCopyEventsBetween(cmd);
        break;

      default:
        view.displayError("Unknown command: ");


    }
  }

  Icalendar handleContextSetting(ParsedCommand cmd) {
    return model.getCalendar(cmd.getContext());
  }

  private void handleCreateCalendar(ParsedCommand cmd) {

    try {
      String name = cmd.getCalendarName();
      String timezone = cmd.getCalendarTimeZone();
      model.createCalendar(name, timezone);
      view.displaySuccess("Calendar:" + name + " added successfully");
    } catch (DuplicateCalendarException | InvalidTimezoneException e) {
      view.displayError(e.getMessage());
    }

  }

  private void handleEditCalendar(ParsedCommand cmd, boolean isNameEdit) {

    try {
      String calName = cmd.getCalendarName();
      String newProperty = cmd.getNewPropertyValue();

      if (isNameEdit) {
        model.renameCalendar(calName, newProperty);
        view.displaySuccess(
            "Name of calendar:" + calName + " edited to " + newProperty + " successfully");
      } else {
        model.changeTimezone(calName, newProperty);
        view.displaySuccess(
            "Timezone of calendar:" + calName + " edited to " + newProperty + " successfully");
      }
    } catch (DuplicateCalendarException | InvalidTimezoneException e) {
      view.displayError(e.getMessage());
    }

  }

  private void handleEventSeriesEditing(ParsedCommand cmd) {

    try {
      String property = cmd.getPropertyToEdit();
      String newValue = cmd.getNewPropertyValue();
      String subject = cmd.getSubject();
      LocalDateTime startTime = cmd.getStartDateTime();
      LocalDateTime endTime = cmd.getEndDateTime();

      currentCalendar.editAllInSeries(subject, startTime, property, newValue);
      view.displaySuccess(
          "All events in series edited successfully. Property: "
              + property + ", New Value: "
              + newValue);
    } catch (EventNotFoundException | DuplicateEventException e) {
      view.displayError(e.getMessage());
    }
  }


  private void handleEventsEditing(ParsedCommand cmd) {
    try {
      String property = cmd.getPropertyToEdit();
      String newValue = cmd.getNewPropertyValue();
      String subject = cmd.getSubject();
      LocalDateTime startTime = cmd.getStartDateTime();
      currentCalendar.editEventsFromDate(subject, startTime, property, newValue);
      view.displaySuccess("Events from date: " + startTime + " edited successfully");
    } catch (EventNotFoundException | DuplicateEventException e) {
      view.displayError(e.getMessage());
    }

  }

  private void handleSingleEventEditing(ParsedCommand cmd) {

    try {
      String property = cmd.getPropertyToEdit();
      String newValue = cmd.getNewPropertyValue();
      String subject = cmd.getSubject();
      LocalDateTime startTime = cmd.getStartDateTime();
      LocalDateTime endTime = cmd.getEndDateTime();

      currentCalendar.editEvent(subject, startTime, endTime, property, newValue);
      view.displaySuccess("Event edited successfully");
    } catch (EventNotFoundException | DuplicateEventException e) {
      view.displayError(e.getMessage());
    }
  }

  private void handleExportCsv(ParsedCommand cmd) {
    try {
      String fileName = cmd.getFileName();
      Set<Event> events = currentCalendar.getAllEvents();
      CsvExporter csvExporter = new CsvExporter();
      String path = csvExporter.exportToCsv(events, fileName);
      view.displaySuccess("Exported CSV successfully. File path: " + path);
    } catch (IOException e) {
      view.displayError("Error while creating CSV file. " + e.getMessage());
    }
  }

  private void handleExportIcal(ParsedCommand cmd) {
    try {
      String fileName = cmd.getFileName();
      Set<Event> events = currentCalendar.getAllEvents();
      IcalExporter icalExporter = new IcalExporter();
      String path = icalExporter.exportToIcal(events, fileName);
      view.displaySuccess("Exported ICAL successfully. File path: " + path);
    } catch (IOException e) {
      view.displayError("Error while creating ICAL file. " + e.getMessage());
    }
  }

  private void handleStatus(ParsedCommand cmd) {
    LocalDateTime date = cmd.getStatusDateTime();
    boolean occupied = currentCalendar.isOccupied(date);
    view.displayStatus(occupied, date);
  }

  private void handlePrintEventsFromQuery(ParsedCommand cmd) {
    try {
      LocalDateTime startDate = cmd.getQueryStartDateTime();
      LocalDateTime endDate = cmd.getQueryEndDateTime();
      List<Event> eventsFrom = currentCalendar.getEventsBetween(startDate, endDate);
      view.displayEventsForRange(eventsFrom, startDate, endDate);
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  private void handlePrintEventsOnQuery(ParsedCommand cmd) {
    try {
      LocalDate date = cmd.getQueryDate();
      List<Event> eventsOn = currentCalendar.getEventsOn(date);
      view.displayEventsForDate(eventsOn, date);
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }

  }

  private void handleSeriesUntilCreation(ParsedCommand cmd) {
    try {
      String subject = cmd.getSubject();
      LocalDateTime startDateTime = cmd.getStartDateTime();
      LocalDateTime endDateTime = cmd.getEndDateTime();
      LocalDate endDate = cmd.getSeriesEndDate();
      Set<DayOfWeekAlphabet> weekdays = cmd.getWeekdays();

      currentCalendar.addEventSeriesUntil(subject, startDateTime, endDateTime, endDate, weekdays);
      view.displaySuccess("Event series until " + endDate + " added successfully");
    } catch (DuplicateEventException e) {
      view.displayError(e.getMessage());
    }

  }

  private void handleNseriesCreation(ParsedCommand cmd) {
    try {
      String subject = cmd.getSubject();
      LocalDateTime startDateTime = cmd.getStartDateTime();
      LocalDateTime endDateTime = cmd.getEndDateTime();
      int occurrences = cmd.getOccurrences();
      Set<DayOfWeekAlphabet> weekdays = cmd.getWeekdays();

      currentCalendar.addEventSeriesOccurrences(subject, startDateTime, endDateTime, occurrences,
          weekdays);
      view.displaySuccess("Event series for " + occurrences + " occurrences added successfully");
    } catch (DuplicateEventException e) {
      view.displayError(e.getMessage());
    }

  }


  private void handleSingleEventCreation(ParsedCommand cmd) {
    try {
      String subject = cmd.getSubject();
      LocalDateTime startDateTime = cmd.getStartDateTime();
      LocalDateTime endDateTime = cmd.getEndDateTime();
      currentCalendar.addEvent(subject, startDateTime, endDateTime);
      view.displaySuccess("Event added successfully");
    } catch (DuplicateEventException e) {
      view.displayError(e.getMessage());
    }

  }

  private void handleCopySingleEvent(ParsedCommand cmd) {
    try {
      String subject = cmd.getSubject();
      LocalDateTime sourceDateTime = cmd.getStartDateTime();
      String targetCalendarName = cmd.getTargetCalendarName();
      LocalDateTime targetDateTime = cmd.getTargetDateTime();

      model.copyEvent(subject, sourceDateTime, targetDateTime, currentCalendar.getCalendarName(),
          targetCalendarName);
      view.displaySuccess("Event copied successfully to " + targetCalendarName);

    } catch (EventNotFoundException | CalendarNotFoundException e) {
      view.displayError(e.getMessage());
    } catch (DuplicateEventException e) {
      view.displayError("Cannot copy. conflict with existing event. " + e.getMessage());
    } catch (Exception e) {
      view.displayError("Error copying event: " + e.getMessage());
    }
  }

  private void handleCopyEventsOnDate(ParsedCommand cmd) {
    try {
      LocalDate sourceDate = cmd.getQueryDate();
      String targetCalendarName = cmd.getTargetCalendarName();
      LocalDate targetDate = cmd.getTargetDate();

      model.copyEventsOn(sourceDate, targetDate, currentCalendar.getCalendarName(),
          targetCalendarName);

      view.displaySuccess("Copied events from " + sourceDate + " to "
          + targetCalendarName + " on " + targetDate);

    } catch (EventNotFoundException | CalendarNotFoundException e) {
      view.displayError(e.getMessage());
    } catch (DuplicateEventException e) {
      view.displayError("Cannot copy. conflict with existing event. " + e.getMessage());
    } catch (Exception e) {
      view.displayError("Error copying events: " + e.getMessage());
    }
  }

  private void handleCopyEventsBetween(ParsedCommand cmd) {
    try {
      LocalDate startDate = cmd.getIntervalStartDate();
      LocalDate endDate = cmd.getIntervalEndDate();
      String targetCalendarName = cmd.getTargetCalendarName();
      LocalDate targetStartDate = cmd.getTargetDate();

      model.copyEventsBetween(startDate, endDate, targetStartDate,
          currentCalendar.getCalendarName(), targetCalendarName);

      view.displaySuccess("Copied " + " events from " + startDate + " to " + endDate
          + " to " + targetCalendarName + " starting " + targetStartDate);

    } catch (DuplicateEventException e) {
      view.displayError("Cannot copy - conflict with existing event: " + e.getMessage());
    } catch (Exception e) {
      view.displayError("Error copying events: " + e.getMessage());
    }
  }

}
