import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import controller.DayOfWeekAlphabet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.CalendarImp;
import model.DuplicateEventException;
import model.Event;
import model.EventNotFoundException;
import model.EventStatus;
import model.Icalendar;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for CalendarImp.
 * Tests event creation, series creation, and duplicate event handling.
 */
public class CalendarImplTests {
  private Icalendar calendar;

  /**
   * Sets up a fresh CalendarImp instance before each test.
   */
  @Before
  public void setUp() {
    ZoneId zone = ZoneId.systemDefault();
    calendar = new CalendarImp("test Calendar", zone);
  }

  /**
   * Tests that adding a duplicate event (same subject, start, and end)
   * throws a DuplicateEventException.
   */
  @Test(expected = DuplicateEventException.class)
  public void duplicateEventAdd() {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);
    calendar.addEvent("Birthday", start, end);
    calendar.addEvent("Birthday", start, end);

  }

  @Test
  public void addSingleEvent() throws DuplicateEventException, EventNotFoundException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDate onDate = LocalDate.of(2025, 3, 5);

    calendar.addEvent("Birthday", start, end);

    List<Event> events = calendar.getEventsOn(onDate);
    assertEquals(1, events.size());
    assertEquals("Birthday", events.get(0).getSubject());

  }

  /**
   * Tests adding an event series with a specific number of occurrences.
   * Creates a series that repeats on Tuesdays and Fridays for 6 occurrences.
   */
  @Test
  public void testAddEventSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 9, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 9, 3, 11, 0);
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.T);
    weekdays.add(DayOfWeekAlphabet.F);
    calendar.addEventSeriesOccurrences("pdp_class", start, end, 6, weekdays);
  }

  /**
   * Tests adding an event series that repeats until a specific end date.
   * Creates a series that repeats on Tuesdays and Fridays until December 30, 2025.
   */
  @Test
  public void testAddEventSeriesUntil() {
    LocalDateTime startDt = LocalDateTime.of(2025, 9, 3, 10, 0);
    LocalDateTime endDt = LocalDateTime.of(2025, 9, 3, 11, 0);
    LocalDate endD = LocalDate.of(2025, 12, 30);
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.T);
    weekdays.add(DayOfWeekAlphabet.F);
    calendar.addEventSeriesUntil("pdp_class", startDt, endDt, endD, weekdays);
  }


  @Test
  public void addMultipleEventsWithSameSubjectDifferentTimes() throws DuplicateEventException {
    LocalDateTime start1 = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 3, 5, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 5, 15, 0);

    calendar.addEvent("Meeting", start1, end1);
    calendar.addEvent("Meeting", start2, end2);

    LocalDate date = LocalDate.of(2025, 3, 5);
    List<Event> events = calendar.getEventsOn(date);
    assertEquals(2, events.size());
  }

  @Test
  public void addEventsWithSameStartDifferentEnd() throws DuplicateEventException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 5, 12, 0);

    calendar.addEvent("Meeting", start, end1);
    calendar.addEvent("Meeting", start, end2);

    LocalDate date = LocalDate.of(2025, 3, 5);
    List<Event> events = calendar.getEventsOn(date);
    assertEquals(2, events.size());
  }

  @Test
  public void addEventSeriesWithOccurrences() throws DuplicateEventException {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.W);
    weekdays.add(DayOfWeekAlphabet.F);

    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 10, 30);

    calendar.addEventSeriesOccurrences("Standup", start, end, 5, weekdays);

    LocalDate startDate = LocalDate.of(2025, 3, 5);
    LocalDate endDate = LocalDate.of(2025, 3, 20);
    List<Event> events = calendar.getEventsBetween(
        startDate.atStartOfDay(),
        endDate.atTime(23, 59)
    );

    assertEquals(5, events.size());

    for (Event e : events) {
      assertEquals("Standup", e.getSubject());
    }
  }

  @Test
  public void addEventSeriesUntilDate() throws DuplicateEventException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 3, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 3, 9, 30);
    LocalDate endDate = LocalDate.of(2025, 3, 17);

    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.W);

    calendar.addEventSeriesUntil("Daily Sync", start, end, endDate, weekdays);

    List<Event> events = calendar.getEventsBetween(
        start,
        endDate.atTime(23, 59)
    );

    assertEquals(5, events.size());
  }

  @Test(expected = DuplicateEventException.class)
  public void addSeriesWithDuplicateInSeries() throws DuplicateEventException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.addEvent("Meeting", start, end);

    LocalDateTime seriesStart = LocalDateTime.of(2025, 3, 5, 10, 0);
    Set<DayOfWeekAlphabet> days = new HashSet<>();
    days.add(DayOfWeekAlphabet.W);

    calendar.addEventSeriesOccurrences("Meeting", seriesStart, end, 2, days);
  }


  @Test
  public void editEventSubject() throws DuplicateEventException, EventNotFoundException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.addEvent("Meeting", start, end);
    calendar.editEvent("Meeting", start, end, "subject", "Important Meeting");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 3, 5));
    assertEquals(1, events.size());
    assertEquals("Important Meeting", events.get(0).getSubject());
  }

  @Test(expected = DuplicateEventException.class)
  public void editEventDuplicateResult() throws DuplicateEventException, EventNotFoundException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.addEvent("Meeting", start, end);
    calendar.addEvent("Meeting 2", start, end);
    calendar.editEvent("Meeting 2", start, end, "subject", "Meeting");
  }

  @Test(expected = DuplicateEventException.class)
  public void editEventSeriesDuplicateResult()
      throws DuplicateEventException, EventNotFoundException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    LocalDate endDate = LocalDate.of(2025, 3, 17);

    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.W);

    calendar.addEventSeriesUntil("Daily Sync", start, end, endDate, weekdays);

    calendar.addEvent("Meeting", start, end);
    calendar.editAllInSeries("Daily Sync", start, "subject", "Meeting");


  }

  @Test
  public void editEventEndTime() throws DuplicateEventException, EventNotFoundException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.addEvent("Meeting", start, end);
    calendar.editEvent("Meeting", start, end, "end", "2025-03-05T12:00");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 3, 5));
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 3, 5, 12, 0), events.get(0).getEnd());
  }

  @Test(expected = EventNotFoundException.class)
  public void editNonExistentEvent() throws EventNotFoundException, DuplicateEventException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.editEvent("NonExistent", start, end, "subject", "New Name");
  }

  @Test
  public void editEventsFromDateSingleEvent()
      throws DuplicateEventException, EventNotFoundException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.addEvent("Meeting", start, end);
    calendar.editEventsFromDate("Meeting", start, "subject", "Important Meeting");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 3, 5));
    assertEquals("Important Meeting", events.get(0).getSubject());
  }

  @Test(expected = EventNotFoundException.class)
  public void editEventsFromDateNoMatch() throws EventNotFoundException, DuplicateEventException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    calendar.editEventsFromDate("NonExistent", start, "subject", "New");
  }

  @Test
  public void editAllInSeriesSingleEvent() throws DuplicateEventException, EventNotFoundException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.addEvent("Meeting", start, end);
    calendar.editAllInSeries("Meeting", start, "subject", "Important Meeting");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 3, 5));
    assertEquals("Important Meeting", events.get(0).getSubject());
  }

  @Test(expected = EventNotFoundException.class)
  public void editAllInSeriesNoEvents() throws DuplicateEventException, EventNotFoundException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.editAllInSeries("Meeting", start, "subject", "Important Meeting");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 3, 5));
    assertEquals("Important Meeting", events.get(0).getSubject());
  }

  /**
   * Tests editing all events in a series.
   */
  @Test
  public void testEditAllInSeries() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);

    calendar.addEventSeriesOccurrences("Weekly Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0),
        3, weekdays);

    calendar.editAllInSeries("Weekly Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        "subject", "Updated Sync");

    Event firstOccurrence = calendar.getEventsOn(LocalDate.of(2025, 11, 3)).get(0);
    Event secondOccurrence = calendar.getEventsOn(LocalDate.of(2025, 11, 10)).get(0);
    Event thirdOccurrence = calendar.getEventsOn(LocalDate.of(2025, 11, 17)).get(0);

    assertEquals("Updated Sync", firstOccurrence.getSubject());
    assertEquals("Updated Sync", secondOccurrence.getSubject());
    assertEquals("Updated Sync", thirdOccurrence.getSubject());
  }


  @Test
  public void testEditAllInSeriesProperties() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);

    calendar.addEventSeriesOccurrences("Weekly Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0),
        3, weekdays);

    calendar.editAllInSeries("Weekly Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        "subject", "Updated Sync");

    calendar.editAllInSeries("Updated Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        "description", "Event details");

    calendar.editAllInSeries("Updated Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        "location", "Boston");

    calendar.editAllInSeries("Updated Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        "status", "Public");

    calendar.editAllInSeries("Updated Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        "start", "2025-11-03T09:00");

    Event firstOccurrence = calendar.getEventsOn(LocalDate.of(2025, 11, 3)).get(0);
    Event secondOccurrence = calendar.getEventsOn(LocalDate.of(2025, 11, 10)).get(0);
    Event thirdOccurrence = calendar.getEventsOn(LocalDate.of(2025, 11, 17)).get(0);

    assertEquals("Updated Sync", firstOccurrence.getSubject());
    assertEquals("Updated Sync", secondOccurrence.getSubject());
    assertEquals("Updated Sync", thirdOccurrence.getSubject());

    assertEquals(LocalDateTime.of(2025, 11, 3, 9, 0), firstOccurrence.getStart());
    assertEquals("Event details", firstOccurrence.getDescription());
    assertEquals("Boston", firstOccurrence.getLocation());
    assertEquals(EventStatus.PUBLIC, firstOccurrence.getStatus());


  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditNonExistentProperty() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);

    calendar.addEventSeriesOccurrences("Weekly Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0),
        3, weekdays);

    calendar.editAllInSeries("Weekly Sync",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        "abc", "Updated Sync");

  }

  /**
   * Tests editing events from a specific date onward.
   */
  @Test
  public void testEditEventsFromDate() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.W);

    calendar.addEventSeriesOccurrences("Class",
        LocalDateTime.of(2025, 11, 5, 14, 0),
        LocalDateTime.of(2025, 11, 5, 16, 0),
        4, weekdays);

    calendar.editEventsFromDate("Class",
        LocalDateTime.of(2025, 11, 12, 14, 0),
        "subject", "Updated Class");

    assertEquals("Class",
        calendar.getEventsOn(LocalDate.of(2025, 11, 5)).get(0).getSubject());

    assertEquals("Updated Class",
        calendar.getEventsOn(LocalDate.of(2025, 11, 12)).get(0).getSubject());
    assertEquals("Updated Class",
        calendar.getEventsOn(LocalDate.of(2025, 11, 19)).get(0).getSubject());
  }


  @Test
  public void getEventsOnDate() throws DuplicateEventException {
    LocalDateTime start1 = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 3, 5, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 5, 15, 0);
    LocalDateTime start3 = LocalDateTime.of(2025, 3, 6, 10, 0);
    LocalDateTime end3 = LocalDateTime.of(2025, 3, 6, 11, 0);

    calendar.addEvent("Morning Meeting", start1, end1);
    calendar.addEvent("Afternoon Meeting", start2, end2);
    calendar.addEvent("Next Day Meeting", start3, end3);

    List<Event> march5 = calendar.getEventsOn(LocalDate.of(2025, 3, 5));
    List<Event> march6 = calendar.getEventsOn(LocalDate.of(2025, 3, 6));

    assertEquals(2, march5.size());
    assertEquals(1, march6.size());
  }

  @Test
  public void getEventsOnDateEmpty() {
    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 3, 5));
    assertNotNull(events);
    assertTrue(events.isEmpty());
  }

  @Test
  public void getEventsBetween() throws DuplicateEventException {
    LocalDateTime event1 = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime event2 = LocalDateTime.of(2025, 3, 10, 10, 0);
    LocalDateTime event3 = LocalDateTime.of(2025, 3, 15, 10, 0);

    calendar.addEvent("Event1", event1, event1.plusHours(1));
    calendar.addEvent("Event2", event2, event2.plusHours(1));
    calendar.addEvent("Event3", event3, event3.plusHours(1));

    List<Event> events = calendar.getEventsBetween(
        LocalDateTime.of(2025, 3, 8, 0, 0),
        LocalDateTime.of(2025, 3, 12, 23, 59)
    );

    assertEquals(1, events.size());
    assertEquals("Event2", events.get(0).getSubject());
  }

  @Test
  public void getEventsBetweenEmpty() {
    List<Event> events = calendar.getEventsBetween(
        LocalDateTime.of(2025, 3, 1, 0, 0),
        LocalDateTime.of(2025, 3, 31, 23, 59)
    );
    assertNotNull(events);
    assertTrue(events.isEmpty());
  }

  @Test
  public void isOccupiedTrue() throws DuplicateEventException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.addEvent("Meeting", start, end);

    assertTrue(calendar.isOccupied(LocalDateTime.of(2025, 3, 5, 10, 0)));
    assertTrue(calendar.isOccupied(LocalDateTime.of(2025, 3, 5, 10, 30)));
    assertTrue(calendar.isOccupied(LocalDateTime.of(2025, 3, 5, 11, 0)));
  }

  @Test
  public void isOccupiedFalse() throws DuplicateEventException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);

    calendar.addEvent("Meeting", start, end);

    assertFalse(calendar.isOccupied(LocalDateTime.of(2025, 3, 5, 9, 59)));
    assertFalse(calendar.isOccupied(LocalDateTime.of(2025, 3, 5, 11, 1)));
    assertFalse(calendar.isOccupied(LocalDateTime.of(2025, 3, 6, 10, 0)));
  }

  @Test
  public void isOccupiedNoEvents() {
    assertFalse(calendar.isOccupied(LocalDateTime.of(2025, 3, 5, 10, 0)));
  }

  @Test
  public void eventSpanningMultipleDays() throws DuplicateEventException {
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 23, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 6, 1, 0);

    calendar.addEvent("Late Night Event", start, end);

    List<Event> march5 = calendar.getEventsOn(LocalDate.of(2025, 3, 5));
    List<Event> march6 = calendar.getEventsOn(LocalDate.of(2025, 3, 6));

    assertFalse(march5.isEmpty());
    assertFalse(march6.isEmpty());
  }

  /**
   * Tests adding event that spans midnight.
   */
  @Test
  public void testEventSpansMidnight() {
    calendar.addEvent("Late Night",
        LocalDateTime.of(2025, 3, 5, 23, 0),
        LocalDateTime.of(2025, 3, 6, 1, 0));

    assertFalse(calendar.getEventsOn(LocalDate.of(2025, 3, 5)).isEmpty());
    assertFalse(calendar.getEventsOn(LocalDate.of(2025, 3, 6)).isEmpty());
  }

  /**
   * Tests series with weekend days.
   */
  @Test
  public void testSeriesWithWeekendDays() {
    Set<DayOfWeekAlphabet> weekend = new HashSet<>();
    weekend.add(DayOfWeekAlphabet.S);
    weekend.add(DayOfWeekAlphabet.U);

    calendar.addEventSeriesOccurrences("Weekend Fun",
        LocalDateTime.of(2025, 11, 1, 10, 0),
        LocalDateTime.of(2025, 11, 1, 12, 0),
        4, weekend);

    assertFalse(calendar.getEventsOn(LocalDate.of(2025, 11, 1)).isEmpty());
    assertFalse(calendar.getEventsOn(LocalDate.of(2025, 11, 2)).isEmpty());

    assertEquals(0, calendar.getEventsOn(LocalDate.of(2025, 11, 3)).size());
  }

  /**
   * Tests complex scenario with multiple series and single events.
   */
  @Test
  public void testComplexScenario() {

    calendar.addEvent("One-time Meeting",
        LocalDateTime.of(2025, 11, 5, 10, 0),
        LocalDateTime.of(2025, 11, 5, 11, 0));

    Set<DayOfWeekAlphabet> mwf = new HashSet<>();
    mwf.add(DayOfWeekAlphabet.M);
    mwf.add(DayOfWeekAlphabet.W);
    mwf.add(DayOfWeekAlphabet.F);
    calendar.addEventSeriesOccurrences("Lecture",
        LocalDateTime.of(2025, 11, 3, 13, 0),
        LocalDateTime.of(2025, 11, 3, 15, 0),
        6, mwf);

    Set<DayOfWeekAlphabet> tr = new HashSet<>();
    tr.add(DayOfWeekAlphabet.T);
    tr.add(DayOfWeekAlphabet.R);
    calendar.addEventSeriesOccurrences("Lab",
        LocalDateTime.of(2025, 11, 4, 16, 0),
        LocalDateTime.of(2025, 11, 4, 18, 0),
        4, tr);

    List<Event> wedEvents = calendar.getEventsOn(LocalDate.of(2025, 11, 5));
    assertTrue(wedEvents.size() >= 2);

    Set<Event> allEvents = calendar.getAllEvents();
    assertTrue(allEvents.size() >= 11);
  }

  /**
   * Tests that calendar handles many events efficiently.
   */
  @Test
  public void testManyEvents() {
    for (int i = 0; i < 100; i++) {
      calendar.addEvent("Event" + i,
          LocalDateTime.of(2025, 3, 5, 8, i % 60),
          LocalDateTime.of(2025, 3, 5, 9, i % 60));
    }

    Set<Event> allEvents = calendar.getAllEvents();
    assertEquals(100, allEvents.size());
  }

  @Test
  public void testEditAllInSeriesMaintainsSeriesId() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);

    calendar.addEventSeriesOccurrences("Weekly Meeting",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0),
        4, weekdays);

    String originalSeriesId = calendar.getEventsOn(LocalDate.of(2025, 11, 3))
        .get(0).getSeriesId();
    assertNotNull(originalSeriesId);

    calendar.editAllInSeries("Weekly Meeting",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        "subject", "Updated Meeting");

    Event event1 = calendar.getEventsOn(LocalDate.of(2025, 11, 3)).get(0);
    Event event2 = calendar.getEventsOn(LocalDate.of(2025, 11, 10)).get(0);

    assertNotNull(event1.getSeriesId());
    assertNotNull(event2.getSeriesId());

    Event event3 = calendar.getEventsOn(LocalDate.of(2025, 11, 17)).get(0);
    Event event4 = calendar.getEventsOn(LocalDate.of(2025, 11, 24)).get(0);

    assertNotNull(event3.getSeriesId());
    assertNotNull(event4.getSeriesId());

    assertEquals(event1.getSeriesId(), event2.getSeriesId());
    assertEquals(event2.getSeriesId(), event3.getSeriesId());
    assertEquals(event3.getSeriesId(), event4.getSeriesId());

    assertEquals("Updated Meeting", event1.getSubject());
    assertEquals("Updated Meeting", event2.getSubject());
    assertEquals("Updated Meeting", event3.getSubject());
    assertEquals("Updated Meeting", event4.getSubject());
  }

  /**
   * Tests that editing all events changes subject but maintains series.
   */
  @Test
  public void testEditAllInSeriesChangesSubjectButKeepsSeries() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.T);
    weekdays.add(DayOfWeekAlphabet.R);

    calendar.addEventSeriesOccurrences("Lab",
        LocalDateTime.of(2025, 11, 4, 14, 0),
        LocalDateTime.of(2025, 11, 4, 16, 0),
        3, weekdays);

    calendar.editAllInSeries("Lab",
        LocalDateTime.of(2025, 11, 4, 14, 0),
        "subject", "Workshop");

    List<Event> tuesday = calendar.getEventsOn(LocalDate.of(2025, 11, 4));
    List<Event> thursday = calendar.getEventsOn(LocalDate.of(2025, 11, 6));

    assertEquals("Workshop", tuesday.get(0).getSubject());
    assertEquals("Workshop", thursday.get(0).getSubject());

    String originalSeriesId = calendar.getEventsOn(LocalDate.of(2025, 11, 4))
        .get(0).getSeriesId();

    assertEquals(originalSeriesId, tuesday.get(0).getSeriesId());
    assertEquals(originalSeriesId, thursday.get(0).getSeriesId());
  }


  @Test
  public void testEditAllInSeriesChangesTimeKeepsSeries() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.W);

    calendar.addEventSeriesOccurrences("Standup",
        LocalDateTime.of(2025, 11, 5, 9, 0),
        LocalDateTime.of(2025, 11, 5, 9, 15),
        4, weekdays);

    String originalSeriesId = calendar.getEventsOn(LocalDate.of(2025, 11, 5))
        .get(0).getSeriesId();

    calendar.editAllInSeries("Standup",
        LocalDateTime.of(2025, 11, 5, 9, 0),
        "start", "2025-11-05T09:10");

    Event event1 = calendar.getEventsOn(LocalDate.of(2025, 11, 5)).get(0);
    Event event2 = calendar.getEventsOn(LocalDate.of(2025, 11, 12)).get(0);

    assertEquals(LocalDateTime.of(2025, 11, 5, 9, 10), event1.getStart());

    assertEquals(event1.getSeriesId(), event2.getSeriesId());
  }

  /**
   * Tests that editing from date splits series correctly.
   * Events before date should keep old seriesId, events from date onward
   * should have new (or same) seriesId.
   */
  @Test
  public void testEditFromDateSplitsSeriesCorrectly() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);

    calendar.addEventSeriesOccurrences("Class",
        LocalDateTime.of(2025, 11, 3, 13, 0),
        LocalDateTime.of(2025, 11, 3, 15, 0),
        5, weekdays);

    String originalSeriesId = calendar.getEventsOn(LocalDate.of(2025, 11, 3))
        .get(0).getSeriesId();

    calendar.editEventsFromDate("Class",
        LocalDateTime.of(2025, 11, 17, 13, 0),
        "subject", "Advanced Class");

    assertEquals("Class",
        calendar.getEventsOn(LocalDate.of(2025, 11, 3)).get(0).getSubject());
    assertEquals("Class",
        calendar.getEventsOn(LocalDate.of(2025, 11, 10)).get(0).getSubject());

    assertEquals("Advanced Class",
        calendar.getEventsOn(LocalDate.of(2025, 11, 17)).get(0).getSubject());
    assertEquals("Advanced Class",
        calendar.getEventsOn(LocalDate.of(2025, 11, 24)).get(0).getSubject());

    assertNotNull(calendar.getEventsOn(LocalDate.of(2025, 11, 3)).get(0).getSeriesId());
    assertNotNull(calendar.getEventsOn(LocalDate.of(2025, 11, 10)).get(0).getSeriesId());
    assertNotNull(calendar.getEventsOn(LocalDate.of(2025, 11, 17)).get(0).getSeriesId());
    assertNotNull(calendar.getEventsOn(LocalDate.of(2025, 11, 24)).get(0).getSeriesId());

    String newSeriesId = calendar.getEventsOn(LocalDate.of(2025, 11, 17))
        .get(0).getSeriesId();
    assertEquals(newSeriesId,
        calendar.getEventsOn(LocalDate.of(2025, 11, 24)).get(0).getSeriesId());
  }

  /**
   * Tests that editing from first occurrence behaves like edit all.
   */
  @Test
  public void testEditFromFirstOccurrenceLikeEditAll() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.F);

    calendar.addEventSeriesOccurrences("Friday Review",
        LocalDateTime.of(2025, 11, 7, 15, 0),
        LocalDateTime.of(2025, 11, 7, 16, 0),
        3, weekdays);

    String originalSeriesId = calendar.getEventsOn(LocalDate.of(2025, 11, 7))
        .get(0).getSeriesId();

    calendar.editEventsFromDate("Friday Review",
        LocalDateTime.of(2025, 11, 7, 15, 0),
        "subject", "Weekly Review");

    assertEquals("Weekly Review",
        calendar.getEventsOn(LocalDate.of(2025, 11, 7)).get(0).getSubject());
    assertEquals("Weekly Review",
        calendar.getEventsOn(LocalDate.of(2025, 11, 14)).get(0).getSubject());
    assertEquals("Weekly Review",
        calendar.getEventsOn(LocalDate.of(2025, 11, 21)).get(0).getSubject());

    String updatedSeriesId = calendar.getEventsOn(LocalDate.of(2025, 11, 7))
        .get(0).getSeriesId();
    assertEquals(updatedSeriesId,
        calendar.getEventsOn(LocalDate.of(2025, 11, 14)).get(0).getSeriesId());
    assertEquals(updatedSeriesId,
        calendar.getEventsOn(LocalDate.of(2025, 11, 21)).get(0).getSeriesId());
  }

  /**
   * Tests editing from middle occurrence maintains series for later events.
   */
  @Test
  public void testEditFromMiddleOccurrenceMaintainsSeries() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.T);

    calendar.addEventSeriesOccurrences("Team Sync",
        LocalDateTime.of(2025, 11, 4, 10, 0),
        LocalDateTime.of(2025, 11, 4, 11, 0),
        6, weekdays);

    calendar.editEventsFromDate("Team Sync",
        LocalDateTime.of(2025, 11, 18, 10, 0),
        "start", "2025-11-18T10:30");

    assertEquals(LocalDateTime.of(2025, 11, 4, 10, 0),
        calendar.getEventsOn(LocalDate.of(2025, 11, 4)).get(0).getStart());
    assertEquals(LocalDateTime.of(2025, 11, 11, 10, 0),
        calendar.getEventsOn(LocalDate.of(2025, 11, 11)).get(0).getStart());

    assertEquals(LocalDateTime.of(2025, 11, 18, 10, 30),
        calendar.getEventsOn(LocalDate.of(2025, 11, 18)).get(0).getStart());

    String laterSeriesId = calendar.getEventsOn(LocalDate.of(2025, 11, 18))
        .get(0).getSeriesId();
    assertNotNull(laterSeriesId);
    assertEquals(laterSeriesId,
        calendar.getEventsOn(LocalDate.of(2025, 11, 25)).get(0).getSeriesId());
  }

  @Test
  public void testEditAllInSeries_ShiftStartTime() throws Exception {

    CalendarImp calendar = new CalendarImp("Test Calendar", ZoneId.of("America/New_York"));
    LocalDateTime seriesStart = LocalDateTime.of(2023, 10, 1, 9, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2023, 10, 1, 11, 0);   // Oct 1st, 11 AM
    LocalDateTime newStart = LocalDateTime.of(2023, 10, 1, 10, 0);

    Set<DayOfWeekAlphabet> days = new HashSet<>();
    days.add(DayOfWeekAlphabet.U);
    days.add(DayOfWeekAlphabet.M);
    days.add(DayOfWeekAlphabet.T);
    days.add(DayOfWeekAlphabet.W);
    days.add(DayOfWeekAlphabet.R);

    calendar.addEventSeriesOccurrences("Meeting", seriesStart, seriesEnd, 5, days);

    calendar.editAllInSeries("Meeting", seriesStart, "start", newStart.toString());

    List<Event> eventsDay1 = calendar.getEventsOn(LocalDate.of(2023, 10, 1));
    assertEquals(10, eventsDay1.get(0).getStart().getHour());
    assertEquals(12, eventsDay1.get(0).getEnd().getHour());

    List<Event> eventsDay3 = calendar.getEventsOn(LocalDate.of(2023, 10, 3));
    assertEquals(10, eventsDay3.get(0).getStart().getHour());
  }

  @Test
  public void testEditAllInSeries_ShiftEndTime() throws Exception {
    CalendarImp calendar = new CalendarImp("Test Calendar", ZoneId.of("America/New_York"));
    LocalDateTime seriesStart = LocalDateTime.of(2023, 10, 1, 9, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2023, 10, 1, 11, 0);

    Set<DayOfWeekAlphabet> days = new HashSet<>();
    days.add(DayOfWeekAlphabet.U);
    days.add(DayOfWeekAlphabet.M);
    days.add(DayOfWeekAlphabet.T);
    days.add(DayOfWeekAlphabet.W);
    days.add(DayOfWeekAlphabet.R);

    calendar.addEventSeriesOccurrences("Meeting", seriesStart, seriesEnd, 5, days);

    LocalDateTime newEnd = LocalDateTime.of(2023, 10, 1, 12, 0);
    calendar.editAllInSeries("Meeting", seriesStart, "end", newEnd.toString());

    List<Event> eventsDay1 = calendar.getEventsOn(LocalDate.of(2023, 10, 1));
    assertEquals(10, eventsDay1.get(0).getStart().getHour());
    assertEquals(12, eventsDay1.get(0).getEnd().getHour());
  }

  @Test
  public void testEditEventsFromDate_ChangeLocation() throws Exception {

    CalendarImp calendar = new CalendarImp("Test Calendar", ZoneId.of("America/New_York"));
    LocalDateTime seriesStart = LocalDateTime.of(2023, 10, 1, 9, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2023, 10, 1, 11, 0);

    Set<DayOfWeekAlphabet> days = new HashSet<>();
    days.add(DayOfWeekAlphabet.U);
    days.add(DayOfWeekAlphabet.M);
    days.add(DayOfWeekAlphabet.T);
    days.add(DayOfWeekAlphabet.W);
    days.add(DayOfWeekAlphabet.R);

    calendar.addEventSeriesOccurrences("Meeting", seriesStart, seriesEnd, 5, days);
    LocalDateTime startFrom = LocalDateTime.of(2023, 10, 3, 9, 0);
    calendar.editEventsFromDate("Meeting", startFrom, "location", "Room B");

    List<Event> eventsDay1 = calendar.getEventsOn(LocalDate.of(2023, 10, 1));
    assertNull(eventsDay1.get(0).getLocation());

    List<Event> eventsDay3 = calendar.getEventsOn(LocalDate.of(2023, 10, 3));
    assertEquals("Room B", eventsDay3.get(0).getLocation());

    List<Event> eventsDay5 = calendar.getEventsOn(LocalDate.of(2023, 10, 5));
    assertEquals("Room B", eventsDay5.get(0).getLocation());
  }

  @Test
  public void testEditEventsFromDate_ShiftSeriesForward() throws Exception {

    CalendarImp calendar = new CalendarImp("Test Calendar", ZoneId.of("America/New_York"));
    LocalDateTime seriesStart = LocalDateTime.of(2023, 10, 1, 9, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2023, 10, 1, 11, 0);

    Set<DayOfWeekAlphabet> days = new HashSet<>();
    days.add(DayOfWeekAlphabet.U);
    days.add(DayOfWeekAlphabet.M);
    days.add(DayOfWeekAlphabet.T);
    days.add(DayOfWeekAlphabet.W);
    days.add(DayOfWeekAlphabet.R);

    calendar.addEventSeriesOccurrences("Meeting", seriesStart, seriesEnd, 5, days);
    LocalDateTime pivotEventStart = LocalDateTime.of(2023, 10, 3, 9, 0);
    LocalDateTime newStart = LocalDateTime.of(2023, 10, 3, 11, 0); // +2 Hours

    calendar.editEventsFromDate("Meeting", pivotEventStart, "start", newStart.toString());

    List<Event> eventsDay2 = calendar.getEventsOn(LocalDate.of(2023, 10, 2));
    assertEquals(9, eventsDay2.get(0).getStart().getHour());

    List<Event> eventsDay3 = calendar.getEventsOn(LocalDate.of(2023, 10, 3));
    assertEquals(11, eventsDay3.get(0).getStart().getHour());
    assertEquals(13, eventsDay3.get(0).getEnd().getHour());

    List<Event> eventsDay4 = calendar.getEventsOn(LocalDate.of(2023, 10, 4));
    assertEquals(11, eventsDay4.get(0).getStart().getHour());
  }


  @Test
  public void testEditEventsFromDate_ShiftSeriesEndDate() throws Exception {

    CalendarImp calendar = new CalendarImp("Test Calendar", ZoneId.of("America/New_York"));
    LocalDateTime seriesStart = LocalDateTime.of(2023, 10, 1, 9, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2023, 10, 1, 11, 0);

    Set<DayOfWeekAlphabet> days = new HashSet<>();
    days.add(DayOfWeekAlphabet.U);
    days.add(DayOfWeekAlphabet.M);
    days.add(DayOfWeekAlphabet.T);
    days.add(DayOfWeekAlphabet.W);
    days.add(DayOfWeekAlphabet.R);

    calendar.addEventSeriesOccurrences("Meeting", seriesStart, seriesEnd, 5, days);

    LocalDateTime pivotEventStart = LocalDateTime.of(2023, 10, 3, 9, 0);
    LocalDateTime newEnd = LocalDateTime.of(2023, 10, 3, 13, 0);

    calendar.editEventsFromDate("Meeting", pivotEventStart, "end", newEnd.toString());

    assertEquals(9, calendar.getEventsOn(LocalDate.of(2023, 10, 2)).get(0).getStart().getHour());
    Event day3 = calendar.getEventsOn(LocalDate.of(2023, 10, 3)).get(0);
    assertEquals(11, day3.getStart().getHour());
    assertEquals(13, day3.getEnd().getHour());
  }


}