package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import mocks.MockIcalendar;
import mocks.MockIcalendarGuiView;
import mocks.MockIcalendarSystem;
import mocks.MockIcalendarView;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarController's GUI features (CalendarFeatures interface).
 * Tests controller business logic with mocked GUI view - NO actual GUI components.
 */
public class CalendarGuiControllerTest {

  private MockIcalendarSystem mockSystem;
  private MockIcalendar mockCalendar;
  private MockIcalendarView mockTextView;
  private MockIcalendarGuiView mockGuiView;
  private StringBuilder systemLog;
  private StringBuilder calendarLog;
  private StringBuilder textViewLog;
  private StringBuilder guiLog;
  private CalendarController controller;

  /**
   * Initial set up.
   */
  @Before
  public void setUp() {
    systemLog = new StringBuilder();
    calendarLog = new StringBuilder();
    textViewLog = new StringBuilder();
    guiLog = new StringBuilder();

    mockCalendar = new MockIcalendar(calendarLog, 12345);
    mockCalendar.setCalendarName("TestCal");
    mockCalendar.setTimezone(ZoneId.of("America/New_York"));

    mockSystem = new MockIcalendarSystem(systemLog, mockCalendar);
    mockTextView = new MockIcalendarView(textViewLog);
    mockGuiView = new MockIcalendarGuiView(guiLog);

    controller = new CalendarController(mockSystem, mockTextView, mockGuiView);
    controller.currentCalendar = mockCalendar;
    controller.createCalendar("TestCal", "America/New_York");
    controller.selectCalendar("TestCal");
    systemLog.setLength(0);
    calendarLog.setLength(0);
    textViewLog.setLength(0);
    guiLog.setLength(0);
  }

  /**
   * Tests that controller calls the calendar to add an event
   * and refreshes the GUI view.
   */
  @Test
  public void testCreateSingleEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    controller.createSingleEvent("Meeting", start, end);
    assertTrue(calendarLog.toString().contains("addEvent: Meeting"));
    assertTrue(guiLog.toString().contains("displayEvents: date=2025-01-15"));
  }

  /**
   * Tests that controller creates a calendar through the model
   * and updates the GUI calendar list.
   */
  @Test
  public void testCreateCalendar() {
    systemLog.setLength(0);
    guiLog.setLength(0);
    controller.createCalendar("NewCal", "UTC");

    assertTrue(systemLog.toString().contains("createCalendar"));
    assertTrue(systemLog.toString().contains("name=NewCal"));
    assertTrue(systemLog.toString().contains("timezone=UTC"));
    assertTrue(guiLog.toString().contains("setCalendarNames"));
  }

  /**
   * Tests that controller retrieves a calendar from the model
   * and updates the GUI view accordingly.
   */
  @Test
  public void testSelectCalendar() {
    systemLog.setLength(0);
    guiLog.setLength(0);
    controller.selectCalendar("TestCal");
    assertTrue(systemLog.toString().contains("getCalendar: TestCal"));
    assertTrue(guiLog.toString().contains("setSelectedCalendar: TestCal"));
    assertTrue(guiLog.toString().contains("updateTimezoneDisplay: America/New_York"));
  }

  /**
   * Tests that controller renames a calendar in the model
   * and refreshes the GUI calendar list.
   */
  @Test
  public void testEditCalendarName() {
    controller.editCalendarName("TestCal", "NewCalName");
    assertTrue(systemLog.toString().contains("renameCalendar"));
    assertTrue(systemLog.toString().contains("oldName=TestCal"));
    assertTrue(systemLog.toString().contains("newName=NewCalName"));
    assertTrue(guiLog.toString().contains("setCalendarNames"));
  }

  /**
   * Tests that controller changes timezone in the model
   * and updates the GUI timezone display.
   */
  @Test
  public void testEditCalendarTimezone() {
    controller.editCalendarTimezone("TestCal", "UTC");
    assertTrue(systemLog.toString().contains("changeTimezone"));
    assertTrue(systemLog.toString().contains("calendar=TestCal"));
    assertTrue(systemLog.toString().contains("timezone=UTC"));
    assertTrue(guiLog.toString().contains("updateTimezoneDisplay: UTC"));
  }

  /**
   * Tests that controller creates a recurring event series
   * and refreshes the view.
   */
  @Test
  public void testCreateRecurringEventWithOccurrences() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.W);
    controller.createRecurringEventWithOccurrences("Weekly Meeting", start, end, weekdays, 5);
    assertTrue(calendarLog.toString().contains("addEventSeriesOccurrences"));
    assertTrue(calendarLog.toString().contains("Weekly Meeting"));
    assertTrue(guiLog.toString().contains("displayEvents"));
  }

  /**
   * Tests that controller creates a recurring event series until a date
   * and refreshes the view.
   */
  @Test
  public void testCreateRecurringEventUntilDate() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.F);
    LocalDate endDate = LocalDate.of(2025, 3, 1);
    controller.createRecurringEventUntilDate("Standup", start, end, weekdays, endDate);
    assertTrue(calendarLog.toString().contains("addEventSeriesUntil"));
    assertTrue(calendarLog.toString().contains("Standup"));
    assertTrue(guiLog.toString().contains("displayEvents"));
  }

  /**
   * Tests that controller edits a single event
   * and refreshes the view.
   */
  @Test
  public void testEditSingleEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    controller.editSingleEvent("Meeting", start, end, "subject", "Updated Meeting");
    assertTrue(calendarLog.toString().contains("editEvent"));
    assertTrue(guiLog.toString().contains("displayEvents"));
  }

  /**
   * Tests that controller queries events from the calendar
   * and displays them in the GUI.
   */
  @Test
  public void testViewEventsOnDate() {
    LocalDate date = LocalDate.of(2025, 1, 15);
    controller.viewEventsOnDate(date);
    assertTrue(calendarLog.toString().contains("getEventsOn: 2025-01-15"));
    assertTrue(guiLog.toString().contains("displayEvents: date=2025-01-15"));
  }

  /**
   * Tests that controller handles calendar not found errors properly.
   */
  @Test
  public void testSelectCalendarNotFound() {
    mockSystem.setShouldThrowNotFound(true);
    guiLog.setLength(0);
    controller.selectCalendar("NonExistent");
    assertTrue(guiLog.toString().contains("showError"));

  }

  @Test
  public void testEditCalendarNameDuplicate() {
    mockSystem.setShouldThrowDuplicate(true);
    guiLog.setLength(0);
    controller.editCalendarName("TestCal", "ExistingName");
    assertTrue(guiLog.toString().contains("showError"));

  }

  /**
   * Tests editing events from a date forward.
   */
  @Test
  public void testEditEventsFromDate() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editEventsFromDate("Meeting", start, "location", "Room B");
    assertTrue(calendarLog.toString().contains("editEventsFromDate"));
    assertTrue(guiLog.toString().contains("displayEvents"));
  }

  /**
   * Tests editing all events in a series.
   */
  @Test
  public void testEditAllInSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editAllInSeries("Weekly Meeting", start, "subject", "Team Standup");
    assertTrue(calendarLog.toString().contains("editAllInSeries"));
    assertTrue(guiLog.toString().contains("displayEvents"));
  }

  /**
   * Tests viewing events from a specific date forward.
   */
  @Test
  public void testViewEventsFromDate() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.viewEventsFromDate(start);
    assertTrue(calendarLog.toString().contains("getEventsBetween"));
  }


  /**
   * Tests exporting calendar to CSV.
   */
  @Test
  public void testExportToCsv() {
    controller.exportToCsv("calendar.csv");
    assertTrue(calendarLog.toString().contains("getAllEvents"));
  }

  /**
   * Tests exporting calendar to iCal format.
   */
  @Test
  public void testExportToIcal() {
    controller.exportToIcal("calendar.ics");
    assertTrue(calendarLog.toString().contains("getAllEvents"));
  }

  /**
   * Tests handling of duplicate event exception.
   */
  @Test
  public void testCreateEventDuplicate() {
    mockCalendar.setShouldThrowDuplicate(true);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    controller.createSingleEvent("Meeting", start, end);
    assertTrue(calendarLog.toString().contains("addEvent"));
  }

  /**
   * Tests handling of event not found exception.
   */
  @Test
  public void testEditEventNotFound() {
    mockCalendar.setShouldThrowNotFound(true);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    controller.editSingleEvent("NonExistent", start, end, "subject", "New");
    assertTrue(calendarLog.toString().contains("editEvent"));
  }

  /**
   * Tests that controller handles duplicate exception when creating recurring event.
   */
  @Test
  public void testCreateRecurringEventDuplicate() {
    mockCalendar.setShouldThrowDuplicate(true);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.W);
    controller.createRecurringEventWithOccurrences("Meeting", start, end, weekdays, 5);
    assertTrue(calendarLog.toString().contains("addEventSeriesOccurrences"));
  }

  /**
   * Tests that controller handles event not found when editing events from date.
   */
  @Test
  public void testEditEventsFromDateNotFound() {
    mockCalendar.setShouldThrowNotFound(true);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editEventsFromDate("NonExistent", start, "location", "Room B");

    assertTrue(calendarLog.toString().contains("editEventsFromDate"));
  }

  /**
   * Tests that controller handles event not found when editing all in series.
   */
  @Test
  public void testEditAllInSeriesNotFound() {
    mockCalendar.setShouldThrowNotFound(true);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editAllInSeries("NonExistent", start, "subject", "New");
    assertTrue(calendarLog.toString().contains("editAllInSeries"));
  }

  /**
   * Tests that controller properly displays empty events list.
   */
  @Test
  public void testViewEventsOnDateEmpty() {
    LocalDate date = LocalDate.of(2025, 12, 25);
    guiLog.setLength(0);

    controller.viewEventsOnDate(date);
    assertTrue(calendarLog.toString().contains("getEventsOn: 2025-12-25"));
    assertTrue(guiLog.toString().contains("displayEvents: date=2025-12-25"));
  }

  /**
   * Tests that selecting a calendar refreshes the event display for today.
   */
  @Test
  public void selectCalendarRefreshEvents() {
    systemLog.setLength(0);
    guiLog.setLength(0);
    controller.selectCalendar("TestCal");
    assertTrue(guiLog.toString().contains("setSelectedCalendar: TestCal"));
    assertTrue(guiLog.toString().contains("updateTimezoneDisplay"));
    assertTrue(guiLog.toString().contains("displayEvents"));
  }

  /**
   * Tests that changing timezone refreshes events display (times may change).
   */
  @Test
  public void testEditCalendarTimezoneRefreshesEvents() {
    guiLog.setLength(0);

    controller.editCalendarTimezone("TestCal", "Europe/London");

    assertTrue(guiLog.toString().contains("updateTimezoneDisplay: Europe/London"));

    assertTrue(guiLog.toString().contains("displayEvents"));
  }

  /**
   * Tests that controller handles duplicate when creating recurring event until date.
   */
  @Test
  public void testCreateRecurringEventUntilDateDuplicate() {
    mockCalendar.setShouldThrowDuplicate(true);

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.T);
    LocalDate endDate = LocalDate.of(2025, 3, 1);

    controller.createRecurringEventUntilDate("Meeting", start, end, weekdays, endDate);

    assertTrue(calendarLog.toString().contains("addEventSeriesUntil"));
  }

  /**
   * Tests that controller handles duplicate when editing causes conflict.
   */
  @Test
  public void testEditSingleEventDuplicate() {
    mockCalendar.setShouldThrowDuplicate(true);

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    controller.editSingleEvent("Meeting", start, end, "subject", "ExistingName");

    assertTrue(calendarLog.toString().contains("editEvent"));
  }

  /**
   * Tests that controller handles duplicate when editing events from date causes conflict.
   */
  @Test
  public void testEditEventsFromDateDuplicate() {
    mockCalendar.setShouldThrowDuplicate(true);

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editEventsFromDate("Meeting", start, "subject", "ExistingName");

    assertTrue(calendarLog.toString().contains("editEventsFromDate"));
  }


  @Test
  public void testEditAllInSeriesDuplicate() {
    mockCalendar.setShouldThrowDuplicate(true);

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editAllInSeries("Meeting", start, "subject", "ExistingName");

    assertTrue(calendarLog.toString().contains("editAllInSeries"));
  }

  /**
   * Tests that performing multiple operations keeps GUI in sync.
   */
  @Test
  public void testMultipleOperationsUpdateGui() {
    guiLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    controller.createSingleEvent("Meeting", start, end);

    controller.editSingleEvent("Meeting", start, end, "location", "Room A");

    controller.viewEventsOnDate(LocalDate.of(2025, 1, 15));

    String log = guiLog.toString();
    int displayCount = log.split("displayEvents").length - 1;
    assertTrue(displayCount >= 3);
  }

  /**
   * Tests that creating multiple calendars updates GUI list progressively.
   */
  @Test
  public void testMultipleCalendarCreationsUpdateList() {
    guiLog.setLength(0);

    controller.createCalendar("Work", "America/Los_Angeles");
    controller.createCalendar("Personal", "Europe/Paris");
    controller.createCalendar("Travel", "Asia/Tokyo");

    String log = guiLog.toString();

    int updateCount = log.split("setCalendarNames").length - 1;
    assertEquals(3, updateCount);
  }

  /**
   *   testcases for improving pit/ jacoco.
   */
  @Test
  public void editCalendarNameNullCalendar() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    controller.editCalendarName("TestCal", "NewName");
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }

  @Test
  public void editCalendarTimezoneNullCalendar() {
    controller.currentCalendar  = null;
    guiLog.setLength(0);
    controller.editCalendarName("TestCal", "NewName");
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }

  @Test
  public void recurringEventWithOccurrencesNull() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    controller.createRecurringEventWithOccurrences("pdp", start, end, weekdays, 5);
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }

  @Test
  public void editCalendarNameNull() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    controller.editCalendarName("TestCal", "NewName");
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }


  @Test
  public void recurringEventUntilDateNull() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    LocalDate endDate = LocalDate.of(2025, 3, 1);
    controller.createRecurringEventUntilDate("Meeting", start, end, weekdays, endDate);
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }

  @Test
  public void editSingleEventNull() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    controller.editSingleEvent("Meeting", start, end, "subject", "New");
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }


  @Test
  public void editEventsFromDateNull() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    controller.editEventsFromDate("pdp", start, "location", "dodge");
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }



  @Test
  public void testEditAllInSeriesNullCalendar() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    controller.editAllInSeries("Meeting", start, "subject", "New");
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }

  @Test
  public void testViewEventsOnDateNullCalendar() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    LocalDate date = LocalDate.of(2025, 1, 15);
    controller.viewEventsOnDate(date);
    assertTrue(guiLog.toString().contains("displayEvents: date=2025-01-15"));
  }

  @Test
  public void testCheckStatusNullCalendar() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 14, 30);
    controller.checkStatus(dateTime);
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }


  @Test
  public void testExportToCsvNullCalendar() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    controller.exportToCsv("calendar.csv");
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }


  @Test
  public void testExportToIcalNullCalendar() {
    controller.currentCalendar = null;
    guiLog.setLength(0);
    controller.exportToIcal("calendar.ics");
    assertTrue(guiLog.toString().contains("showError"));
    assertTrue(guiLog.toString().contains("No calendar selected"));
  }

  @Test
  public void testViewEventsOnDateWithNullReturn() {
    LocalDate date = LocalDate.of(2025, 1, 15);
    guiLog.setLength(0);
    controller.viewEventsOnDate(date);
    assertTrue(guiLog.toString().contains("displayEvents: date=2025-01-15"));
  }

  @Test
  public void testUpdateCalendarListWithCurrentCalendar() {
    controller.createCalendar("NewCal", "UTC");
    assertTrue(guiLog.toString().contains("setSelectedCalendar"));
  }


}