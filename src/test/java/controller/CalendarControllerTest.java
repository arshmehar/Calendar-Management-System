package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import mocks.MockIcalendar;
import mocks.MockIcalendarSystem;
import mocks.MockIcalendarView;
import model.Event;
import model.EventStatus;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for CalendarController using manual mocks.
 * Tests command processing with the calendar system .
 */
public class CalendarControllerTest {

  private MockIcalendarSystem mockSystem;
  private MockIcalendar mockCalendar;
  private MockIcalendarView mockView;
  private StringBuilder systemLog;
  private StringBuilder calendarLog;
  private StringBuilder viewLog;
  private CalendarController controller;

  /**
   * setup requirements before each test which is to be carried out.
   */
  @Before
  public void setUp() {
    systemLog = new StringBuilder();
    calendarLog = new StringBuilder();
    viewLog = new StringBuilder();

    mockCalendar = new MockIcalendar(calendarLog, 12345);
    mockSystem = new MockIcalendarSystem(systemLog, mockCalendar);
    mockView = new MockIcalendarView(viewLog);

    controller = new CalendarController(mockSystem, mockView);

    controller.processCommand("create calendar --name TestCal --timezone America/New_York");
    controller.processCommand("use calendar --name TestCal");

    systemLog.setLength(0);
    calendarLog.setLength(0);
    viewLog.setLength(0);
  }

  @Test
  public void getView() {
    assertSame(mockView, controller.getView());
  }

  @Test
  public void createSingleEvent() {
    controller.processCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");

    assertEquals(
        "addEvent: Meeting 2025-01-15T10:00 2025-01-15T11:00" + System.lineSeparator(),
        calendarLog.toString());
    assertTrue(viewLog.toString().contains("displaySuccess: Event added successfully"));
  }

  @Test
  public void printEventsOn() {
    controller.processCommand("print events on 2025-01-15");

    assertEquals(
        "getEventsOn: 2025-01-15" + System.lineSeparator(), calendarLog.toString());
    assertTrue(viewLog.toString().contains("displayEventsForDate: date=2025-01-15"));
  }

  @Test
  public void printEventsRange() {
    controller.processCommand(
        "print events from 2025-01-15T08:00 to 2025-01-20T17:00");

    String log = calendarLog.toString();
    assertTrue(log.contains("getEventsBetween"));
    assertTrue(log.contains("2025-01-15T08:00"));
    assertTrue(viewLog.toString().contains("displayEventsForRange"));
  }

  /**
   * Recreate with uniqueCode 1000 (even = busy).
   * for testing purpose, in MockIcalendar we keep all evens busy.
   */
  @Test
  public void showStatusBusy() {

    mockCalendar = new MockIcalendar(calendarLog, 1000);
    mockSystem = new MockIcalendarSystem(systemLog, mockCalendar);
    controller = new CalendarController(mockSystem, mockView);


    controller.processCommand("use calendar --name TestCal");

    calendarLog.setLength(0);
    viewLog.setLength(0);

    controller.processCommand("show status on 2025-01-15T14:30");

    assertEquals("isOccupied: 2025-01-15T14:30" + System.lineSeparator(),
        calendarLog.toString());
    assertTrue(viewLog.toString().contains("busy=true"));
  }

  /**
   * Recreate with uniqueCode 999 (odd = notbusy).
   * for testing purpose, in MockIcalendar we write so as to keep all odds free.
   */
  @Test
  public void showStatusFree() {
    mockCalendar = new MockIcalendar(calendarLog, 999);
    mockSystem = new MockIcalendarSystem(systemLog, mockCalendar);
    controller = new CalendarController(mockSystem, mockView);
    controller.processCommand("use calendar --name TestCal");

    calendarLog.setLength(0);
    viewLog.setLength(0);

    controller.processCommand("show status on 2025-01-15T14:30");

    assertEquals("isOccupied: 2025-01-15T14:30" + System.lineSeparator(),
        calendarLog.toString());
    assertTrue(viewLog.toString().contains("busy=false"));
  }

  @Test
  public void invalidCommand() {
    controller.processCommand("invalid command format");

    assertEquals("", calendarLog.toString());
    assertTrue(viewLog.toString().contains("displayError"));
  }


  /**
   * Testing invalid date format.
   */
  @Test
  public void invalidDate() {
    controller.processCommand("create event Meeting from 2025-01-15 to 2025-01-16");

    assertEquals("", calendarLog.toString());
    assertTrue(viewLog.toString().contains("displayError"));
  }

  /**
   * Testing duplicate event exception.
   */
  @Test
  public void duplicateEvent() {
    mockCalendar.setShouldThrowDuplicate(true);
    controller.processCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");

    assertTrue(calendarLog.toString().contains("addEvent"));
    assertTrue(viewLog.toString().contains("displayError"));
    assertFalse(viewLog.toString().contains("displaySuccess"));
  }

  /**
   * Testing event not found exception.
   */
  @Test
  public void eventNotFound() {
    mockCalendar.setShouldThrowNotFound(true);
    controller.processCommand(
        "edit event subject NonExistent from 2025-01-15T10:00 to 2025-01-15T11:00 "
            + "with NewSubject");

    assertTrue(calendarLog.toString().contains("editEvent"));
    assertTrue(viewLog.toString().contains("displayError"));
  }

  /**
   * Testing duplicate series exception.
   */
  @Test
  public void duplicateSeries() {
    mockCalendar.setShouldThrowDuplicate(true);
    controller.processCommand(
        "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 "
            + "repeats MW for 5 times");

    assertTrue(calendarLog.toString().contains("addEventSeriesOccurrences"));
    assertTrue(viewLog.toString().contains("displayError"));
  }


  /**
   * Testing multiple commands in sequence processing.
   */
  @Test
  public void testMultipleCommands() {
    controller.processCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");
    controller.processCommand("print events on 2025-01-15");
    controller.processCommand("show status on 2025-01-15T10:30");

    String log = calendarLog.toString();
    int addIdx = log.indexOf("addEvent");
    int getIdx = log.indexOf("getEventsOn");
    int statusIdx = log.indexOf("isOccupied");

    assertTrue(addIdx != -1 && getIdx != -1 && statusIdx != -1);
    assertTrue(addIdx < getIdx && getIdx < statusIdx);
  }

  /**
   * Tests transmission of time values.
   */
  @Test
  public void testTimeTransmission() {
    controller.processCommand("create event Test from 2025-12-25T23:59 to 2025-12-26T00:59");

    String log = calendarLog.toString();
    assertTrue(log.contains("2025-12-25T23:59"));
    assertTrue(log.contains("2025-12-26T00:59"));
  }

  /**
   * Tests transmission of weekdays.
   */
  @Test
  public void testWeekdaysTransmit() {
    controller.processCommand(
        "create event Test from 2025-01-15T10:00 to 2025-01-15T11:00 "
            + "repeats MTWRFSU for 3 times");

    assertTrue(calendarLog.toString().contains("weekdays="));
  }

  /**
   * Tests handling empty command.
   */
  @Test
  public void testEmpty() {
    controller.processCommand("");
    assertEquals("", calendarLog.toString());
  }

  /**
   * Tests handling whitespace-only command.
   */
  @Test
  public void whiteSpace() {
    controller.processCommand("   ");
    assertEquals("", calendarLog.toString());
  }

  /**
   * Tests processing multiple edit commands.
   */
  @Test
  public void multipleEdits() {
    controller.processCommand(
        "edit event subject Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 with NewMeeting");
    controller.processCommand(
        "edit events location Standup from 2025-01-20T09:00 with RoomB");
    controller.processCommand(
        "edit series subject Conference from 2025-01-25T14:00 with Summit");

    String log = calendarLog.toString();
    assertTrue(log.contains("editEvent"));
    assertTrue(log.contains("editEventsFromDate"));
    assertTrue(log.contains("editAllInSeries"));
  }


  /**
   * Tests CSV export command.
   */
  @Test
  public void testExportCsv() {
    controller.processCommand("export cal my_cal.csv");

    assertTrue(calendarLog.toString().contains("getAllEvents"));
    assertTrue(viewLog.toString().contains("displaySuccess"));
  }


  /**
   * Tests for copying event(S) to another calendar.
   */
  @Test
  public void testCopySingleEvent() {
    controller.processCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");
    controller.processCommand("create calendar --name TargetCal --timezone UTC");

    systemLog.setLength(0);
    calendarLog.setLength(0);
    viewLog.setLength(0);

    controller.processCommand(
        "copy event Meeting on 2025-01-15T10:00 --target TargetCal to 2025-01-16T14:00");

    assertTrue(systemLog.toString().contains("copyEvent"));
    assertTrue(systemLog.toString().contains("Meeting"));
    assertTrue(systemLog.toString().contains("TargetCal"));
  }


  @Test
  public void copySingleEventQuotedName() {
    controller.processCommand(
        "create event \"Team Meeting\" from 2025-01-15T10:00 to 2025-01-15T11:00");
    controller.processCommand("create calendar --name TargetCal --timezone UTC");

    systemLog.setLength(0);
    controller.processCommand(
        "copy event \"Team Meeting\" on 2025-01-15T10:00 --target TargetCal to 2025-01-16T14:00");

    assertTrue(systemLog.toString().contains("copyEvent"));
    assertTrue(systemLog.toString().contains("Team Meeting"));
  }

  /**
   * Tests copying all events on a specific date.
   */
  @Test
  public void testCopyEventsOnDate() {
    controller.processCommand("create event Meeting1 from 2025-01-15T13:00 to 2025-01-15T16:00");
    controller.processCommand("create event Meeting2 from 2025-01-15T17:00 to 2025-01-15T17:00");
    controller.processCommand("create calendar --name TargetCal --timezone UTC");

    systemLog.setLength(0);

    controller.processCommand("copy events on 2025-01-15 --target TargetCal to 2025-01-20");

    assertTrue(systemLog.toString().contains("copyEventsOn"));
    assertTrue(systemLog.toString().contains("sourceDate=2025-01-15"));
    assertTrue(systemLog.toString().contains("targetDate=2025-01-20"));
    assertTrue(systemLog.toString().contains("TargetCal"));
  }


  /**
   * Tests all copy operation exception handling.
   * 1. copySingleEvent - EventNotFoundException
   * 2. copySingleEvent - DuplicateEventException
   * 3. copyEventsOn - DuplicateEventException
   * 4. copyEventsBetween - DuplicateEventException
   *
   */
  @Test
  public void testAllCopyExceptions() {
    controller.processCommand("create calendar --name TargetCal --timezone UTC");

    mockSystem.setShouldThrowNotFoundOnCopy(true);
    viewLog.setLength(0);
    controller.processCommand(
        "copy event Meeting on 2025-01-15T10:00 --target TargetCal to 2025-01-16T10:00");
    assertTrue(viewLog.toString().contains("displayError"));
    mockSystem.setShouldThrowNotFoundOnCopy(false);


    mockSystem.setShouldThrowDuplicateOnCopy(true);
    viewLog.setLength(0);
    controller.processCommand(
        "copy event Meeting on 2025-01-15T10:00 --target TargetCal to 2025-01-16T10:00");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Cannot copy. conflict with existing event"));
    mockSystem.setShouldThrowDuplicateOnCopy(false);

    mockSystem.setShouldThrowDuplicateOnCopy(true);
    viewLog.setLength(0);
    controller.processCommand("copy events on 2025-01-15 --target TargetCal to 2025-01-20");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Cannot copy. conflict with existing event"));
    mockSystem.setShouldThrowDuplicateOnCopy(false);

    mockSystem.setShouldThrowDuplicateOnCopy(true);
    viewLog.setLength(0);
    controller.processCommand(
        "copy events between 2025-01-15 and 2025-01-20 --target TargetCal to 2025-02-01");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Cannot copy - conflict with existing event"));
    mockSystem.setShouldThrowDuplicateOnCopy(false);
  }

  /**
   * Tests copying events within a date range.
   */
  @Test
  public void testCopyEventsBetween() {
    controller.processCommand("create event Meeting1 from 2025-01-15T10:00 to 2025-01-15T11:00");
    controller.processCommand("create event Meeting2 from 2025-01-17T14:00 to 2025-01-17T15:00");
    controller.processCommand("create event Meeting3 from 2025-01-20T09:00 to 2025-01-20T10:00");
    controller.processCommand("create calendar --name TargetCal --timezone UTC");

    systemLog.setLength(0);

    controller.processCommand(
        "copy events between 2025-01-15 and 2025-01-20 --target TargetCal to 2025-02-01");

    assertTrue(systemLog.toString().contains("copyEventsBetween"));
    assertTrue(systemLog.toString().contains("sourceStart=2025-01-15"));
    assertTrue(systemLog.toString().contains("sourceEnd=2025-01-20"));
    assertTrue(systemLog.toString().contains("targetStart=2025-02-01"));
    assertTrue(systemLog.toString().contains("TargetCal"));
  }

  /**
   * Tests error when exceptions.
   */
  @Test
  public void copyEventMissingTarget() {
    controller.processCommand("copy event Meeting on 2025-01-15T10:00 to 2025-01-16T14:00");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("missing '--target <calendarName>'"));
  }

  @Test
  public void copyEventMissingTo() {
    controller.processCommand(
        "copy event Meeting on 2025-01-15T10:00 --target TargetCal 2025-01-16T14:00");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("missing 'to <date/dateTime>'"));
  }

  @Test
  public void copyUnknownFormat() {
    controller.processCommand("copy something --target TargetCal to 2025-01-15");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Unknown copy command format"));
  }

  @Test
  public void testCopyEventMissingOn() {
    controller.processCommand(
        "copy event Meeting 2025-01-15T10:00 --target TargetCal to 2025-01-16T14:00");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("expected 'on <dateTime>' after event name"));
  }


  @Test
  public void extraWordsBeforeTarget() {
    controller.processCommand(
        "copy event Meeting on "
            + "2025-01-15T10:00 extra words --target TargetCal to 2025-01-16T14:00");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid source date-time"));
  }


  @Test
  public void copyEventMissingToAfterCalendar() {
    controller.processCommand(
        "copy event Meeting on 2025-01-15T10:00 to early --target TargetCal 2025-01-16T14:00");
    assertTrue(viewLog.toString().contains("displayError"));
  }


  @Test
  public void extraWordsInTargetDateTime() {
    controller.processCommand(
        "copy event Meeting on 2025-01-15T10:00 --target TargetCal to 2025-01-16T14:00 extra");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid target date-time"));
  }

  @Test
  public void eventsOnExtraWords() {
    controller.processCommand("copy events on 2025-01-15 extra --target TargetCal to 2025-01-20");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid source date"));
  }

  @Test
  public void copyEventsOnTargetExtraWords() {
    controller.processCommand("copy events on 2025-01-15 --target TargetCal to 2025-01-20 extra");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid target date"));
  }


  @Test
  public void copyBetweenMissingAnd() {
    controller.processCommand(
        "copy events between 2025-01-15 2025-01-20 --target TargetCal to 2025-02-01");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Expected '<date> and <date>"));
  }


  @Test
  public void copyBetweenEndBeforeStart() {
    controller.processCommand(
        "copy events between 2025-01-20 and 2025-01-15 --target TargetCal to 2025-02-01");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("End date cannot be before start date"));
  }


  @Test
  public void copyMismatchedQuotesInCalendarName() {
    controller.processCommand(
        "copy event Meeting on 2025-01-15T10:00 --target \"TargetCal to 2025-01-16T14:00");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Mismatched quotes"));
  }


  @Test
  public void testCopyEmptyCalendarName() {
    controller.processCommand(
        "copy event Meeting on 2025-01-15T10:00 --target \"\" to 2025-01-16T14:00");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Calendar name cannot be empty"));
  }


  @Test
  public void testCopyWithQuotedTargetCalendar() {
    controller.processCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");
    controller.processCommand("create calendar --name \"My Target Calendar\" --timezone UTC");

    systemLog.setLength(0);

    controller.processCommand(
        "copy event Meeting on "
            + "2025-01-15T10:00 --target \"My Target Calendar\" to 2025-01-16T14:00");

    assertTrue(systemLog.toString().contains("copyEvent"));
    assertTrue(systemLog.toString().contains("My Target Calendar"));
  }


  /**
   * MODE MANAGER TESTS.
   * Tests that controller runs in HEADLESS mode.
   */
  @Test
  public void testHeadlessMode() throws Exception {
    String commands = "create calendar --name TestCal --timezone UTC" + System.lineSeparator()
        + "use calendar --name TestCal" + System.lineSeparator()
        + "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00" + System.lineSeparator()
        + "exit" + System.lineSeparator();

    java.nio.file.Files.write(java.nio.file.Paths.get("test_commands.txt"),
        commands.getBytes());

    calendarLog.setLength(0);
    viewLog.setLength(0);

    controller.run(ApplicationMode.HEADLESS, "test_commands.txt");

    assertTrue(calendarLog.toString().contains("addEvent"));

    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("test_commands.txt"));
  }


  /**
   * Tests exporting events to both CSV and iCal formats.
   */
  @Test
  public void testExportToMultipleFormats() throws Exception {
    Set<Event> events = new HashSet<>();

    Event event1 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 1, 15, 10, 0))
        .end(LocalDateTime.of(2025, 1, 15, 11, 0))
        .description("Discuss project updates")
        .location("Room A")
        .status(EventStatus.PUBLIC)
        .build();

    Event event2 = Event.getBuilder()
        .subject("Lunch")
        .start(LocalDateTime.of(2025, 1, 15, 12, 0))
        .end(LocalDateTime.of(2025, 1, 15, 13, 0))
        .build();

    events.add(event1);
    events.add(event2);

    CsvExporter csvExporter = new CsvExporter();
    String csvPath = csvExporter.exportToCsv(events, "test_calendar.csv");

    assertTrue(Files.exists(Paths.get(csvPath)));
    String csvContent = Files.readString(Paths.get(csvPath));
    assertTrue(csvContent.contains("Subject,Start Date,Start Time"));
    assertTrue(csvContent.contains("Meeting"));
    assertTrue(csvContent.contains("Lunch"));

    IcalExporter icalExporter = new IcalExporter();
    String icalPath = icalExporter.exportToIcal(events, "test_calendar.ics");

    assertTrue(Files.exists(Paths.get(icalPath)));
    String icalContent = Files.readString(Paths.get(icalPath));
    assertTrue(icalContent.contains("BEGIN:VCALENDAR"));
    assertTrue(icalContent.contains("VERSION:2.0"));
    assertTrue(icalContent.contains("SUMMARY:Meeting"));
    assertTrue(icalContent.contains("SUMMARY:Lunch"));

    Files.deleteIfExists(Paths.get(csvPath));
    Files.deleteIfExists(Paths.get(icalPath));
  }


  /**
   * CalendarOperationsCommandParser tests.
   */
  @Test
  public void editCalendarInvalidProperty() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("edit calendar --name TestCal --property invalid NewValue");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid property"));
    assertTrue(viewLog.toString().contains("Can only edit 'name' or 'timezone'"));
  }

  @Test
  public void editCalendarInvalidProperty2() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("edit calendar --name TestCal --property invalid NewValue");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid property"));
    assertTrue(viewLog.toString().contains("Can only edit 'name' or 'timezone'"));
  }

  /**
   * Tests error when multi-word value is not quoted.
   */
  @Test
  public void testEditCalendarUnquotedMultiWord() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("edit calendar --name TestCal --property name New Calendar Name");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("multi-word values must be in quotes"));
  }

  /**
   * Tests error when value has mismatched quotes.
   */
  @Test
  public void testEditCalendarMismatchedQuotes() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("edit calendar --name TestCal --property name \"NewName");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Mismatched quotes"));
  }

  /**
   * Tests error when extra text after closing quote in value.
   */
  @Test
  public void testEditCalendarExtraTextAfterQuote() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("edit calendar --name TestCal --property name \"NewName\" extra");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Extra text after closing quote"));
  }

  /**
   * Tests error when timezone value is invalid.
   */
  @Test
  public void testEditCalendarInvalidTimezone() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("edit calendar --name TestCal --property timezone InvalidZone");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Unsupported timezone"));
  }

  /**
   * Tests error when --property flag is missing.
   */
  @Test
  public void testEditCalendarMissingPropertyFlag() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("edit calendar --name TestCal name NewName");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Expected --property flag"));
  }

  /**
   * Tests error when property name or value is missing.
   */
  @Test
  public void testEditCalendarMissingPropertyValue() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("edit calendar --name TestCal --property name");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Missing property name or value"));
  }

  /**
   * Tests error when timezone has extra words.
   */
  @Test
  public void testCreateCalendarTimezoneExtraWords() {
    controller.processCommand("create calendar --name TestCal --timezone America/New York");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Extra words"));
  }

  /**
   * Tests error when --name flag is missing in create.
   */
  @Test
  public void testCreateCalendarMissingNameFlag() {
    controller.processCommand("create calendar TestCal --timezone UTC");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Expected '--name' flag"));
  }

  /**
   * Tests error when calendar name is empty.
   */
  @Test
  public void testCreateCalendarEmptyName() {
    controller.processCommand("create calendar --name \"\" --timezone UTC");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Calendar name cannot be empty"));
  }

  /**
   * Tests error when --timezone flag is missing.
   */
  @Test
  public void testCreateCalendarMissingTimezoneFlag() {
    controller.processCommand("create calendar --name TestCal UTC");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Expected --timezone flag"));
  }

  /**
   * Tests error when timezone is empty.
   */
  @Test
  public void testCreateCalendarEmptyTimezone() {
    controller.processCommand("create calendar --name TestCal --timezone");

    assertTrue(viewLog.toString().contains("displayError"));
  }

  /**
   * Tests use calendar with quoted name.
   */
  @Test
  public void testUseCalendarQuotedName() {
    controller.processCommand("create calendar --name \"My Work Calendar\" --timezone UTC");

    systemLog.setLength(0);
    viewLog.setLength(0);

    controller.processCommand("use calendar --name \"My Work Calendar\"");

    assertTrue(systemLog.toString().contains("getCalendar: My Work Calendar"));
    assertTrue(viewLog.toString().contains("displayMessage"));
  }

  /**
   * Tests error when extra text after calendar name in use command.
   */
  @Test
  public void testUseCalendarExtraText() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("use calendar --name TestCal extra text");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Extra text after calendar name"));
  }

  /**
   * Tests error when calendar name has mismatched quotes in use command.
   */
  @Test
  public void testUseCalendarMismatchedQuotes() {
    controller.processCommand("use calendar --name \"TestCal");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Mismatched quotes"));
  }

  /**
   * Tests various edit calendar error cases.
   * 1. Invalid property
   * 2. Multi-word without quotes
   * 3. Mismatched quotes
   * 4. Extra text after quote
   * 5. Invalid timezone
   * 6. Missing property flag
   * 7. Missing property value.
   */
  @Test
  public void testEditCalendarErrors() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");

    viewLog.setLength(0);
    controller.processCommand("edit calendar --name TestCal --property invalid NewValue");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Can only edit 'name' or 'timezone'"));


    viewLog.setLength(0);
    controller.processCommand("edit calendar --name TestCal --property name New Name");
    assertTrue(viewLog.toString().contains("multi-word values must be in quotes"));


    viewLog.setLength(0);
    controller.processCommand("edit calendar --name TestCal --property name \"NewName");
    assertTrue(viewLog.toString().contains("Mismatched quotes"));

    viewLog.setLength(0);
    controller.processCommand("edit calendar --name TestCal --property name \"NewName\" extra");
    assertTrue(viewLog.toString().contains("Extra text after closing quote"));

    viewLog.setLength(0);
    controller.processCommand("edit calendar --name TestCal --property timezone BadZone");
    assertTrue(viewLog.toString().contains("Unsupported timezone"));


    viewLog.setLength(0);
    controller.processCommand("edit calendar --name TestCal name NewName");
    assertTrue(viewLog.toString().contains("Expected --property flag"));


    viewLog.setLength(0);
    controller.processCommand("edit calendar --name TestCal --property name");
    assertTrue(viewLog.toString().contains("Missing property name or value"));
  }

  /**
   * Tests various create calendar error cases.
   * 1.  Timezone with extra words
   * 2. Missing name flag
   * 3. Empty name
   * 4. Missing timezone flag
   */
  @Test
  public void testCreateCalendarErrors() {

    viewLog.setLength(0);
    controller.processCommand("create calendar --name Test --timezone America/New York");
    assertTrue(viewLog.toString().contains("Extra words"));

    viewLog.setLength(0);
    controller.processCommand("create calendar Test --timezone UTC");
    assertTrue(viewLog.toString().contains("Expected '--name' flag"));

    viewLog.setLength(0);
    controller.processCommand("create calendar --name \"\" --timezone UTC");
    assertTrue(viewLog.toString().contains("Calendar name cannot be empty"));


    viewLog.setLength(0);
    controller.processCommand("create calendar --name Test UTC");
    assertTrue(viewLog.toString().contains("Expected --timezone flag"));
  }

  /**
   * Tests various use calendar cases.
   * 1. Extra text error
   * 2. Mismatched quotes
   */
  @Test
  public void testUseCalendarVariousCases() {
    controller.processCommand("create calendar --name \"My Calendar\" --timezone UTC");

    systemLog.setLength(0);
    controller.processCommand("use calendar --name \"My Calendar\"");
    assertTrue(systemLog.toString().contains("getCalendar: My Calendar"));

    viewLog.setLength(0);
    controller.processCommand("use calendar --name TestCal extra");
    assertTrue(viewLog.toString().contains("Extra text after calendar name"));

    viewLog.setLength(0);
    controller.processCommand("use calendar --name \"TestCal");
    assertTrue(viewLog.toString().contains("Mismatched quotes"));
  }


  /**
   * Tests various create command parsing errors.
   * 1.Generic invalid format
   * 2. Invalid day character in weekdays
   */
  @Test
  public void testCreateCommandParsingErrors() {

    viewLog.setLength(0);
    controller.processCommand("create event Meeting 2025-01-15T10:00");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid 'create event' format"));

    viewLog.setLength(0);
    controller.processCommand(
        "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 repeats MXF for 5 times");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid day"));
  }

  /**
   * Tests export command parsing errors.
   * 1. missing filename
   * 2.Filename with spaces
   * 3. Not .csv extension
   */
  @Test
  public void testExportCommandErrors2() {
    viewLog.setLength(0);
    controller.processCommand("export cal");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid command"));

    viewLog.setLength(0);
    controller.processCommand("export cal my calendar.csv");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("cannot contain spaces"));

    viewLog.setLength(0);
    controller.processCommand("export cal mycalendar.txt");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("must end with .csv"));
  }

  /**
   * Tests creating series with until date through command executor.
   */
  @Test
  public void testSeriesUntilCreationExecutor() {
    controller.processCommand(
        "create event Meeting from "
            + "2025-01-15T10:00 to 2025-01-15T11:00 repeats MWF until 2025-02-01");

    assertTrue(calendarLog.toString().contains("addEventSeriesUntil"));
    assertTrue(calendarLog.toString().contains("Meeting"));
    assertTrue(calendarLog.toString().contains("endDate=2025-02-01"));
    assertTrue(viewLog.toString().contains("displaySuccess"));
    assertTrue(viewLog.toString().contains("until 2025-02-01"));
  }

  /**
   * Tests renaming a calendar through executor.
   */
  @Test
  public void testRenameCalendarExecutor() {
    viewLog.setLength(0);
    systemLog.setLength(0);

    controller.processCommand("edit calendar --name TestCal --property name NewCalName");

    assertTrue(systemLog.toString().contains("renameCalendar"));
    assertTrue(systemLog.toString().contains("oldName=TestCal"));
    assertTrue(systemLog.toString().contains("newName=NewCalName"));
    assertTrue(viewLog.toString().contains("displaySuccess"));
  }

  /**
   * Tests changing calendar timezone through executor.
   */
  @Test
  public void testChangeTimezoneExecutor() {
    viewLog.setLength(0);
    systemLog.setLength(0);

    controller.processCommand("edit calendar --name TestCal --property timezone UTC");

    assertTrue(systemLog.toString().contains("changeTimezone"));
    assertTrue(systemLog.toString().contains("calendar=TestCal"));
    assertTrue(systemLog.toString().contains("timezone=UTC"));
    assertTrue(viewLog.toString().contains("displaySuccess"));
  }

  /**
   * Tests creating all-day series through executor.
   */
  @Test
  public void testAllDaySeriesCreation() {
    controller.processCommand("create event Conference on 2025-01-15 repeats MWF for 5 times");

    assertTrue(calendarLog.toString().contains("addEventSeriesOccurrences"));
    assertTrue(calendarLog.toString().contains("Conference"));
    assertTrue(viewLog.toString().contains("displaySuccess"));
  }


  /**
   * Tests more create command error cases.
   * 1. All-day event with extra words after date
   * 2. All-day series with wrong format
   * 3. Timed event with extra words before 'to'
   * 4. Timed event with extra words after 'to'
   * 5. Series with neither 'for' nor 'until'
   */
  @Test
  public void testMoreCreateErrors() {

    viewLog.setLength(0);
    controller.processCommand("create event Meeting on 2025-01-15 extra");
    System.out.println("Test 1: " + viewLog.toString());
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid date"));

    viewLog.setLength(0);
    controller.processCommand("create event Meeting on 2025-01-15 extra repeats MWF for 5 times");
    System.out.println("Test 1: " + viewLog.toString());
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid date"));

    viewLog.setLength(0);
    controller.processCommand(
        "create event Meeting from 2025-01-15T10:00 extra to 2025-01-15T11:00");
    System.out.println("Test 1: " + viewLog.toString());
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid start time"));


    viewLog.setLength(0);
    controller.processCommand(
        "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 extra");
    System.out.println("Test 1: " + viewLog.toString());
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid end time"));

    viewLog.setLength(0);
    controller.processCommand(
        "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 repeats MWF");
    System.out.println("Test 1: " + viewLog.toString());
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Expected 'for <N> times' or 'until <date>'"));
  }

  /**
   * Tests iCal export command.
   */
  @Test
  public void testExportIcal() {
    controller.processCommand("export cal my_cal.ical");

    assertTrue(calendarLog.toString().contains("getAllEvents"));
    assertTrue(viewLog.toString().contains("displaySuccess"));
    assertTrue(viewLog.toString().contains("ICAL"));
  }


  /**
   * Tests default case in switch statement.
   */
  @Test
  public void testUnknownCommandType() {
    viewLog.setLength(0);
    controller.processCommand("unknown command");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid command"));
  }


  /**
   * Tests generic Exception catch in handlePrintEventsFromQuery.
   */
  @Test
  public void testPrintEventsFromGenericException() {
    controller.processCommand("print events from 2025-01-15T10:00 to 2025-01-15T12:00");
    assertTrue(viewLog.toString().contains("displayEventsForRange"));
  }

  /**
   * Tests generic Exception catch in handlePrintEventsOnQuery.
   */
  @Test
  public void testPrintEventsOnGenericException() {
    controller.processCommand("print events on 2025-01-15");
    assertTrue(viewLog.toString().contains("displayEventsForDate"));
  }

  /**
   * Tests generic Exception catch in handleCopyEventsBetween.
   */
  @Test
  public void testCopyEventsBetweenGenericException() {
    controller.processCommand("create calendar --name TargetCal --timezone UTC");
    systemLog.setLength(0);

    controller.processCommand(
        "copy events between 2025-01-15 and 2025-01-20 --target TargetCal to 2025-02-01");
    assertTrue(systemLog.toString().contains("copyEventsBetween"));
  }


  /**
   * Tests creating a single all-day event.
   */
  @Test
  public void testCreateSingleAllDayEvent() {
    controller.processCommand("create event Conference on 2025-01-20");

    assertTrue(calendarLog.toString().contains("addEvent: Conference"));
    assertTrue(calendarLog.toString().contains("2025-01-20T08:00"));
    assertTrue(calendarLog.toString().contains("2025-01-20T17:00"));
    assertTrue(viewLog.toString().contains("displaySuccess"));
  }


  /**
   * Tests iCal export with long description (line folding).
   */
  @Test
  public void testIcalExportLineFolding() throws Exception {
    Set<Event> events = new HashSet<>();

    String longDesc =
        "This is an extremely long description that will definitely exceed "
           +  "the seventy-five character limit for iCal lines and should trigger the line "
           +  "folding mechanism in the exporter";

    Event event = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 1, 15, 10, 0))
        .end(LocalDateTime.of(2025, 1, 15, 11, 0))
        .description(longDesc)
        .build();

    events.add(event);

    IcalExporter exporter = new IcalExporter();
    String path = exporter.exportToIcal(events, "test_long.ics");

    String content = Files.readString(Paths.get(path));
    assertTrue(content.contains("DESCRIPTION"));

    Files.deleteIfExists(Paths.get(path));
  }

  /**
   * Tests iCal export with series events (tests seriesId UID generation).
   */
  @Test
  public void testIcalExportWithSeriesId() throws Exception {
    Set<Event> events = new HashSet<>();

    Event event = Event.getBuilder()
        .subject("Weekly Meeting")
        .start(LocalDateTime.of(2025, 1, 15, 10, 0))
        .end(LocalDateTime.of(2025, 1, 15, 11, 0))
        .seriesId("test-series-123")
        .build();

    events.add(event);

    IcalExporter exporter = new IcalExporter();
    String path = exporter.exportToIcal(events, "test_series.ics");

    String content = Files.readString(Paths.get(path));
    assertTrue(content.contains("UID:test-series-123"));

    Files.deleteIfExists(Paths.get(path));
  }

  /**
   * Tests iCal export creating parent directories.
   */
  @Test
  public void testIcalExportWithDirectories() throws Exception {
    Set<Event> events = new HashSet<>();

    Event event = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 1, 15, 10, 0))
        .end(LocalDateTime.of(2025, 1, 15, 11, 0))
        .build();

    events.add(event);

    IcalExporter exporter = new IcalExporter();
    String path = exporter.exportToIcal(events, "test_output/subfolder/test.ics");

    assertTrue(Files.exists(Paths.get(path)));

    Files.deleteIfExists(Paths.get(path));
    Files.deleteIfExists(Paths.get("test_output/subfolder"));
    Files.deleteIfExists(Paths.get("test_output"));
  }

  /**
   * Tests edit event command with quoted values and error cases.
   * 1.Mismatched quotes
   * 2.Extra text after closing quote
   */
  @Test
  public void testEditEventQuotedValues() {
    controller.processCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");

    viewLog.setLength(0);
    controller.processCommand(
        "edit event subject Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 with \"NewSubject");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Mismatched quotes in 'with' value"));

    viewLog.setLength(0);
    controller.processCommand(
        "edit event subject Meeting from 2025-01-15T10:00 to"
            + " 2025-01-15T11:00 with \"NewSubject\" extra");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("extra text after closing quote"));
  }

  /**
   * Tests EventNotFoundException in copyEventsOn.
   */
  @Test
  public void testCopyEventsOnEventNotFound() {
    controller.processCommand("create calendar --name TargetCal --timezone UTC");

    mockSystem.setShouldThrowNotFoundOnCopy(true);
    viewLog.setLength(0);

    controller.processCommand("copy events on 2025-01-15 --target TargetCal to 2025-01-20");

    assertTrue(viewLog.toString().contains("displayError"));
    mockSystem.setShouldThrowNotFoundOnCopy(false);
  }

  @Test
  public void testCopyEventsOnCalendarNotFound() {
    mockSystem.setShouldThrowCalendarNotFoundOnCopy(true);
    viewLog.setLength(0);

    controller.processCommand("copy events on 2025-01-15 --target NonExistent to 2025-01-20");

    assertTrue(viewLog.toString().contains("displayError"));
    mockSystem.setShouldThrowCalendarNotFoundOnCopy(false);
  }

  /**
   * Tests series creation error cases for number parsing.
   * 1. empty weekdays before 'for'
   * 2. Missing 'times' keyword
   * 3. Invalid number format
   */
  @Test
  public void testSeriesNumberErrors() {
    viewLog.setLength(0);
    controller.processCommand(
        "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 repeats  for 5 times");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid weekdays"));


    viewLog.setLength(0);
    controller.processCommand(
        "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 repeats MWF for 5");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Must end with '<N> times'"));

    viewLog.setLength(0);
    controller.processCommand(
        "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 repeats MWF for abc times");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid number"));
  }

  /**
   * Tests exception handling in handleCreateCalendar.
   */
  @Test
  public void testCreateCalendarExceptions() {
    mockSystem.setShouldThrowDuplicate(true);
    viewLog.setLength(0);
    controller.processCommand("create calendar --name DupCal --timezone UTC");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Mock duplicate calendar"));
    mockSystem.setShouldThrowDuplicate(false);

  }

  /**
   * Tests exception handling in handleEditCalendar.
   */
  @Test
  public void testEditCalendarExceptions() {
    mockSystem.setShouldThrowDuplicate(true);
    viewLog.setLength(0);
    controller.processCommand("edit calendar --name TestCal --property name ExistingName");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Mock duplicate calendar"));
    mockSystem.setShouldThrowDuplicate(false);
  }

  /**
   * Tests exception handling in handleEventSeriesEditing.
   */
  @Test
  public void testEditSeriesExceptions() {
    mockCalendar.setShouldThrowNotFound(true);
    viewLog.setLength(0);
    controller.processCommand(
        "edit series subject NonExistent from 2025-01-15T10:00 with NewSubject");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Mock series not found"));
    mockCalendar.setShouldThrowNotFound(false);

    mockCalendar.setShouldThrowDuplicate(true);
    viewLog.setLength(0);
    controller.processCommand("edit series subject Meeting from 2025-01-15T10:00 with Duplicate");
    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Mock duplicate after series edit"));
    mockCalendar.setShouldThrowDuplicate(false);
  }

  /**
   * Tests remaining DuplicateEventException catch blocks.
   */
  @Test
  public void testRemainingDuplicateExceptions() {
    mockCalendar.setShouldThrowDuplicate(true);
    viewLog.setLength(0);
    controller.processCommand(
        "create event Series from 2025-01-15T10:00 "
            + "to 2025-01-15T11:00 repeats MWF until 2025-03-01");
    assertTrue(viewLog.toString().contains("displayError"));
    mockCalendar.setShouldThrowDuplicate(false);

    mockCalendar.setShouldThrowDuplicate(true);
    viewLog.setLength(0);
    controller.processCommand("edit events subject Meeting from 2025-01-15T10:00 with Dup");
    assertTrue(viewLog.toString().contains("displayError"));
    mockCalendar.setShouldThrowDuplicate(false);

    mockCalendar.setShouldThrowDuplicate(true);
    viewLog.setLength(0);
    controller.processCommand("edit series subject Meeting from 2025-01-15T10:00 with Dup");
    assertTrue(viewLog.toString().contains("displayError"));
    mockCalendar.setShouldThrowDuplicate(false);
  }

  /**
   * Tests CSV export edge cases for branch coverage.
   * 1. All-day event (exactly 8AM-5PM same day) - triggers isAllDayEvent true
   * 2. Event with comma, quote, newline (triggers escapeCsvField wrapping)
   * 3. Event with PRIVATE status (triggers privateEventStatus return "True")
   * 4.  Event with null status (triggers privateEventStatus return null)
   */
  @Test
  public void testCsvExportEdgeCases() throws Exception {
    Set<Event> events = new HashSet<>();


    Event allDay = Event.getBuilder()
        .subject("AllDay")
        .start(LocalDateTime.of(2025, 1, 15, 8, 0))
        .end(LocalDateTime.of(2025, 1, 15, 17, 0))
        .build();
    events.add(allDay);

    Event needsEscape = Event.getBuilder()
        .subject("Meeting, Planning")
        .start(LocalDateTime.of(2025, 1, 16, 10, 0))
        .end(LocalDateTime.of(2025, 1, 16, 11, 0))
        .description("Line1\nLine2")
        .location("Room \"A\"")
        .build();
    events.add(needsEscape);


    Event privateEvent = Event.getBuilder()
        .subject("Private")
        .start(LocalDateTime.of(2025, 1, 17, 10, 0))
        .end(LocalDateTime.of(2025, 1, 17, 11, 0))
        .status(EventStatus.PRIVATE)
        .build();
    events.add(privateEvent);

    Event nullStatus = Event.getBuilder()
        .subject("NoStatus")
        .start(LocalDateTime.of(2025, 1, 18, 10, 0))
        .end(LocalDateTime.of(2025, 1, 18, 11, 0))
        .build();
    events.add(nullStatus);

    CsvExporter exporter = new CsvExporter();
    String path = exporter.exportToCsv(events, "test_edge.csv");

    String content = Files.readString(Paths.get(path));
    assertTrue(content.contains("AllDay"));
    assertTrue(content.contains("\"Meeting, Planning\""));

    Files.deleteIfExists(Paths.get(path));
  }


  /**
   * Tests that headless mode reports error for invalid command in file.
   */
  @Test
  public void testHeadlessModeInvalidCommand() throws Exception {
    String commands = "create calendar --name TestCal --timezone UTC" + System.lineSeparator()
        + "use calendar --name TestCal" + System.lineSeparator()
        + "invalid command here" + System.lineSeparator()
        + "exit" + System.lineSeparator();

    Files.write(Paths.get("test_invalid.txt"), commands.getBytes());

    viewLog.setLength(0);
    controller.run(ApplicationMode.HEADLESS, "test_invalid.txt");

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid command"));

    Files.deleteIfExists(Paths.get("test_invalid.txt"));
  }

}

