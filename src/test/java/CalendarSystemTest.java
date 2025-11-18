import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import controller.DayOfWeekAlphabet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.CalendarNotFoundException;
import model.CalendarSystem;
import model.DuplicateCalendarException;
import model.DuplicateEventException;
import model.Event;
import model.EventNotFoundException;
import model.Icalendar;
import model.IcalendarSystem;
import model.InvalidTimezoneException;
import org.junit.Before;
import org.junit.Test;

/**
 * tests for calendar system.
 */
public class CalendarSystemTest {
  private IcalendarSystem calendarSystem;

  /**
   * setup to be carried out before each test.
   */
  @Before
  public void setUp() {
    calendarSystem = new CalendarSystem();
  }

  @Test
  public void testCalendarSystem() {
    calendarSystem.createCalendar("Study", "Europe/London");
    Icalendar calendar = calendarSystem.getCalendar("Study");
    LocalDateTime start = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDate onDate = LocalDate.of(2025, 3, 5);

    calendar.addEvent("Birthday", start, end);
    List<Event> events = calendar.getEventsOn(onDate);
    assertEquals(1, events.size());
    assertEquals("Birthday", events.get(0).getSubject());
  }

  @Test
  public void testCreateCalendar() {
    calendarSystem.createCalendar("Study", "Europe/London");
    Icalendar calendar = calendarSystem.getCalendar("Study");
    assertNotNull(calendar);
    assertEquals("Study", calendar.getCalendarName());
    assertEquals("Europe/London", calendar.getCalendarTimeZone().getId());
  }


  @Test
  public void testChangeTimezoneConvertsEventTimes() {
    calendarSystem.createCalendar("Work", "America/New_York");
    Icalendar calendar = calendarSystem.getCalendar("Work");

    calendar.addEvent("Meeting",
        LocalDateTime.of(2025, 11, 4, 11, 0),
        LocalDateTime.of(2025, 11, 4, 12, 0));

    calendarSystem.changeTimezone("Work", "America/Los_Angeles");

    Event meeting = calendar.getEventsOn(LocalDate.of(2025, 11, 4)).get(0);
    assertEquals(LocalDateTime.of(2025, 11, 4, 8, 0), meeting.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 4, 9, 0), meeting.getEnd());

    assertEquals(ZoneId.of("America/Los_Angeles"), calendar.getCalendarTimeZone());
  }

  @Test
  public void testChangeTimezoneConvertsMultipleEvents() {
    calendarSystem.createCalendar("Schedule", "America/New_York");
    Icalendar calendar = calendarSystem.getCalendar("Schedule");

    calendar.addEvent("Morning Meeting",
        LocalDateTime.of(2025, 11, 4, 9, 0),
        LocalDateTime.of(2025, 11, 4, 10, 0));
    calendar.addEvent("Lunch",
        LocalDateTime.of(2025, 11, 4, 12, 0),
        LocalDateTime.of(2025, 11, 4, 13, 0));
    calendar.addEvent("Afternoon Call",
        LocalDateTime.of(2025, 11, 4, 15, 0),
        LocalDateTime.of(2025, 11, 4, 16, 0));

    calendarSystem.changeTimezone("Schedule", "America/Los_Angeles");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 11, 4));
    assertEquals(3, events.size());

    Event morning = events.stream()
        .filter(e -> e.getSubject().equals("Morning Meeting"))
        .findFirst().get();
    assertEquals(LocalDateTime.of(2025, 11, 4, 6, 0), morning.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 4, 7, 0), morning.getEnd());

    Event lunch = events.stream()
        .filter(e -> e.getSubject().equals("Lunch"))
        .findFirst().get();
    assertEquals(LocalDateTime.of(2025, 11, 4, 9, 0), lunch.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 4, 10, 0), lunch.getEnd());

    Event afternoon = events.stream()
        .filter(e -> e.getSubject().equals("Afternoon Call"))
        .findFirst().get();
    assertEquals(LocalDateTime.of(2025, 11, 4, 12, 0), afternoon.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 4, 13, 0), afternoon.getEnd());
  }


  @Test
  public void testChangeTimezoneForEventCrossingMidnight() {
    calendarSystem.createCalendar("Night", "America/New_York");
    Icalendar calendar = calendarSystem.getCalendar("Night");

    calendar.addEvent("Late Meeting",
        LocalDateTime.of(2025, 11, 4, 23, 0),
        LocalDateTime.of(2025, 11, 5, 1, 0));

    calendarSystem.changeTimezone("Night", "America/Los_Angeles");

    Event meeting = calendar.getEventsOn(LocalDate.of(2025, 11, 4)).get(0);
    assertEquals(LocalDateTime.of(2025, 11, 4, 20, 0), meeting.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 4, 22, 0), meeting.getEnd());
  }

  @Test
  public void testCreateMultipleCalendars() {
    calendarSystem.createCalendar("Work", "America/New_York");
    calendarSystem.createCalendar("Personal", "America/Los_Angeles");
    calendarSystem.createCalendar("Travel", "Asia/Tokyo");

    assertEquals("Work", calendarSystem.getCalendar("Work").getCalendarName());
    assertEquals("Personal", calendarSystem.getCalendar("Personal").getCalendarName());
    assertEquals("Travel", calendarSystem.getCalendar("Travel").getCalendarName());
  }

  @Test(expected = DuplicateCalendarException.class)
  public void testCreateDuplicateCalendar() {
    calendarSystem.createCalendar("Study", "Europe/London");
    calendarSystem.createCalendar("Study", "America/New_York");
  }

  @Test(expected = DuplicateCalendarException.class)
  public void testCreateCalendarCaseInsensitive() {
    calendarSystem.createCalendar("Study", "Europe/London");
    calendarSystem.createCalendar("study", "America/New_York");
  }

  @Test(expected = InvalidTimezoneException.class)
  public void testCreateCalendarInvalidTimezone() {
    calendarSystem.createCalendar("Test", "Invalid/Timezone");
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testGetNonExistentCalendar() {
    calendarSystem.getCalendar("NonExistent");
  }

  @Test
  public void testRenameCalendar() {
    calendarSystem.createCalendar("OldName", "America/New_York");
    Icalendar calendar = calendarSystem.getCalendar("OldName");
    LocalDateTime start = LocalDateTime.of(2025, 11, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 5, 11, 0);
    calendar.addEvent("Meeting", start, end);

    calendarSystem.renameCalendar("OldName", "NewName");

    Icalendar renamedCalendar = calendarSystem.getCalendar("NewName");
    assertEquals("NewName", renamedCalendar.getCalendarName());
    assertEquals(1, renamedCalendar.getAllEvents().size());
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testRenameCalendarOldNameNotFound() {
    calendarSystem.createCalendar("OldName", "America/New_York");
    calendarSystem.renameCalendar("OldName", "NewName");
    calendarSystem.getCalendar("OldName");
  }

  @Test(expected = DuplicateCalendarException.class)
  public void testRenameCalendarToExistingName() {
    calendarSystem.createCalendar("Calendar1", "America/New_York");
    calendarSystem.createCalendar("Calendar2", "America/New_York");
    calendarSystem.renameCalendar("Calendar1", "Calendar2");
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testRenameNonExistentCalendar() {
    calendarSystem.renameCalendar("NonExistent", "NewName");
  }


  @Test
  public void testChangeTimezone() {
    calendarSystem.createCalendar("Test", "America/New_York");
    calendarSystem.changeTimezone("Test", "Europe/Paris");

    Icalendar calendar = calendarSystem.getCalendar("Test");
    assertEquals("Europe/Paris", calendar.getCalendarTimeZone().getId());
  }

  @Test(expected = InvalidTimezoneException.class)
  public void testChangeToInvalidTimezone() {
    calendarSystem.createCalendar("Test", "America/New_York");
    calendarSystem.changeTimezone("Test", "Invalid/Zone");
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testChangeTimezoneNonExistentCalendar() {
    calendarSystem.changeTimezone("NonExistent", "Europe/London");
  }

  @Test
  public void testCopyEventSameCalendarSameTimezone() {
    calendarSystem.createCalendar("Cal1", "America/New_York");
    Icalendar calendar = calendarSystem.getCalendar("Cal1");

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 16, 0);
    calendar.addEvent("Meeting", start, end);

    LocalDateTime targetDateTime = LocalDateTime.of(2025, 11, 5, 14, 0);
    calendarSystem.copyEvent("Meeting", start, targetDateTime, "Cal1", "Cal1");

    List<Event> eventsOnTarget = calendar.getEventsOn(LocalDate.of(2025, 11, 5));
    assertEquals(1, eventsOnTarget.size());
    assertEquals("Meeting", eventsOnTarget.get(0).getSubject());
    assertEquals(targetDateTime, eventsOnTarget.get(0).getStart());
  }

  @Test
  public void testCopyEventDifferentCalendarsSameTimezone() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 16, 0);
    source.addEvent("Meeting", start, end);

    LocalDateTime targetDateTime = LocalDateTime.of(2025, 11, 5, 10, 0);
    calendarSystem.copyEvent("Meeting", start, targetDateTime, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    List<Event> eventsOnTarget = target.getEventsOn(LocalDate.of(2025, 11, 5));
    assertEquals(1, eventsOnTarget.size());
    assertEquals("Meeting", eventsOnTarget.get(0).getSubject());
    assertEquals(LocalDateTime.of(2025, 11, 5, 10, 0), eventsOnTarget.get(0).getStart());
  }

  @Test
  public void testCopyEventWithTargetTime() {
    calendarSystem.createCalendar("EST", "America/New_York");
    calendarSystem.createCalendar("PST", "America/Los_Angeles");

    Icalendar est = calendarSystem.getCalendar("EST");
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 16, 0);
    est.addEvent("Meeting", start, end);

    LocalDateTime targetDateTime = LocalDateTime.of(2025, 12, 5, 18, 0);
    calendarSystem.copyEvent("Meeting", start, targetDateTime, "EST", "PST");

    Icalendar pst = calendarSystem.getCalendar("PST");
    List<Event> events = pst.getEventsOn(LocalDate.of(2025, 12, 5));
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 12, 5, 18, 0), events.get(0).getStart());
    assertEquals(LocalDateTime.of(2025, 12, 5, 20, 0), events.get(0).getEnd());
  }

  @Test
  public void testCopyEventsWithNewTimezoneConversion() {
    calendarSystem.createCalendar("EST", "America/New_York");
    calendarSystem.createCalendar("PST", "America/Los_Angeles");

    Icalendar est = calendarSystem.getCalendar("EST");
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 16, 0);
    est.addEvent("Meeting", start, end);

    LocalDate sourceDate = LocalDate.of(2025, 11, 3);
    LocalDate targetDate = LocalDate.of(2025, 11, 3);
    calendarSystem.copyEventsOn(sourceDate, targetDate, "EST", "PST");

    Icalendar pst = calendarSystem.getCalendar("PST");
    List<Event> events = pst.getEventsOn(LocalDate.of(2025, 11, 3));
    assertEquals(LocalDateTime.of(2025, 11, 3, 11, 0), events.get(0).getStart());
    assertEquals(LocalDateTime.of(2025, 11, 3, 13, 0), events.get(0).getEnd());
  }

  @Test
  public void testCopyEventPreservesDuration() {
    calendarSystem.createCalendar("Cal1", "America/New_York");
    Icalendar calendar = calendarSystem.getCalendar("Cal1");

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 12, 30);
    calendar.addEvent("Long Meeting", start, end);

    LocalDateTime targetDateTime = LocalDateTime.of(2025, 11, 10, 15, 0);
    calendarSystem.copyEvent("Long Meeting", start, targetDateTime, "Cal1", "Cal1");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 11, 10));
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 11, 10, 15, 0), events.get(0).getStart());
    assertEquals(LocalDateTime.of(2025, 11, 10, 17, 30), events.get(0).getEnd());
  }

  @Test
  public void testCopyEventPreservesMetadata() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);

    source.addEvent("Meeting", start, end);
    source.editEvent("Meeting", start, end, "location", "boston");
    source.editEvent("Meeting", start, end, "description", "My meeting description");

    LocalDateTime targetDateTime = LocalDateTime.of(2025, 11, 5, 10, 0);
    calendarSystem.copyEvent("Meeting", start, targetDateTime, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    List<Event> events = target.getEventsOn(LocalDate.of(2025, 11, 5));
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
    assertEquals("My meeting description", events.get(0).getDescription());
    assertEquals("boston", events.get(0).getLocation());

  }

  @Test(expected = EventNotFoundException.class)
  public void testCopyNonExistentEvent() {
    calendarSystem.createCalendar("Cal1", "America/New_York");
    calendarSystem.createCalendar("Cal2", "America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime targetDateTime = LocalDateTime.of(2025, 11, 5, 10, 0);
    calendarSystem.copyEvent("NonExistent", start, targetDateTime, "Cal1", "Cal2");
  }


  @Test
  public void testCopyEventsOnDateSameTimezone() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");

    source.addEvent("Morning Meeting",
        LocalDateTime.of(2025, 11, 3, 9, 0),
        LocalDateTime.of(2025, 11, 3, 10, 0));
    source.addEvent("Lunch",
        LocalDateTime.of(2025, 11, 3, 12, 0),
        LocalDateTime.of(2025, 11, 3, 13, 0));
    source.addEvent("Evening Call",
        LocalDateTime.of(2025, 11, 3, 17, 0),
        LocalDateTime.of(2025, 11, 3, 18, 0));

    LocalDate sourceDate = LocalDate.of(2025, 11, 3);
    LocalDate targetDate = LocalDate.of(2025, 11, 10);
    calendarSystem.copyEventsOn(sourceDate, targetDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    List<Event> events = target.getEventsOn(targetDate);
    assertEquals(3, events.size());
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testCopyEventsOnNullTarget() {
    calendarSystem.createCalendar("Source", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");

    source.addEvent("Morning Meeting",
        LocalDateTime.of(2025, 11, 3, 9, 0),
        LocalDateTime.of(2025, 11, 3, 10, 0));

    LocalDate sourceDate = LocalDate.of(2025, 11, 3);
    LocalDate targetDate = LocalDate.of(2025, 11, 10);
    calendarSystem.copyEventsOn(sourceDate, targetDate, "Source", "Target");

  }

  @Test(expected = CalendarNotFoundException.class)
  public void testCopyEventsOnNullSource() {
    calendarSystem.createCalendar("Target", "America/New_York");

    LocalDate sourceDate = LocalDate.of(2025, 11, 3);
    LocalDate targetDate = LocalDate.of(2025, 11, 10);
    calendarSystem.copyEventsOn(sourceDate, targetDate, "Source", "Target");

  }

  @Test(expected = CalendarNotFoundException.class)
  public void testCopyEventsBetweenNullSource() {
    calendarSystem.createCalendar("Target", "America/New_York");

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 3);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "Source", "Target");
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testCopyEventsBetweenNullTarget() {
    calendarSystem.createCalendar("Source", "America/New_York");

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 3);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "Source", "Target");
  }

  @Test
  public void testCopyEventsOnDateWithTimezoneConversion() {
    calendarSystem.createCalendar("EST", "America/New_York");
    calendarSystem.createCalendar("PST", "America/Los_Angeles");

    Icalendar est = calendarSystem.getCalendar("EST");
    LocalDate sourceDate = LocalDate.of(2025, 11, 3);

    est.addEvent("Morning Meeting",
        LocalDateTime.of(2025, 11, 3, 9, 0),
        LocalDateTime.of(2025, 11, 3, 10, 0));
    est.addEvent("Afternoon Call",
        LocalDateTime.of(2025, 11, 3, 14, 0),
        LocalDateTime.of(2025, 11, 3, 15, 0));

    LocalDate targetDate = LocalDate.of(2025, 11, 10);
    calendarSystem.copyEventsOn(sourceDate, targetDate, "EST", "PST");

    Icalendar pst = calendarSystem.getCalendar("PST");
    List<Event> events = pst.getEventsOn(targetDate);
    assertEquals(2, events.size());

    Event morning = events.stream()
        .filter(e -> e.getSubject().equals("Morning Meeting"))
        .findFirst().get();
    assertEquals(LocalDateTime.of(2025, 11, 10, 6, 0), morning.getStart());

    Event afternoon = events.stream()
        .filter(e -> e.getSubject().equals("Afternoon Call"))
        .findFirst().get();
    assertEquals(LocalDateTime.of(2025, 11, 10, 11, 0), afternoon.getStart());
  }

  @Test
  public void testCopyEventsOnDateEmpty() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    LocalDate sourceDate = LocalDate.of(2025, 11, 3);
    LocalDate targetDate = LocalDate.of(2025, 11, 10);

    calendarSystem.copyEventsOn(sourceDate, targetDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    List<Event> events = target.getEventsOn(targetDate);
    assertEquals(0, events.size());
  }

  @Test
  public void testCopyEventsOnDateToSameCalendar() {
    calendarSystem.createCalendar("Cal", "America/New_York");
    Icalendar cal = calendarSystem.getCalendar("Cal");

    LocalDate sourceDate = LocalDate.of(2025, 11, 3);
    cal.addEvent("Event1",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0));

    LocalDate targetDate = LocalDate.of(2025, 11, 10);
    calendarSystem.copyEventsOn(sourceDate, targetDate, "Cal", "Cal");

    List<Event> sourceEvents = cal.getEventsOn(sourceDate);
    List<Event> targetEvents = cal.getEventsOn(targetDate);
    assertEquals(1, sourceEvents.size());
    assertEquals(1, targetEvents.size());
  }

  @Test
  public void testCopyEventsOnDateToSameCalendar2() {
    calendarSystem.createCalendar("Cal", "America/New_York");
    Icalendar cal = calendarSystem.getCalendar("Cal");

    LocalDate sourceDate = LocalDate.of(2025, 11, 3);

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 12, 0);

    cal.addEvent("Event1", start, end);

    LocalDateTime targetDate = LocalDateTime.of(2025, 11, 3, 17, 0);
    calendarSystem.copyEvent("Event1", start, targetDate, "Cal", "Cal");

    List<Event> sourceEvents = cal.getEventsOn(sourceDate);
    assertEquals(2, sourceEvents.size());
  }

  @Test
  public void testCopyEventsBetweenSingleDay() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEvent("Event1",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0));

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 3);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    List<Event> events = target.getEventsOn(targetStartDate);
    assertEquals(1, events.size());
  }

  @Test
  public void testCopyEventsBetweenMultipleDays() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEvent("Day1",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0));
    source.addEvent("Day2",
        LocalDateTime.of(2025, 11, 4, 14, 0),
        LocalDateTime.of(2025, 11, 4, 15, 0));
    source.addEvent("Day2.5",
        LocalDateTime.of(2025, 11, 4, 16, 0),
        LocalDateTime.of(2025, 11, 4, 17, 0));
    source.addEvent("Day3",
        LocalDateTime.of(2025, 11, 5, 9, 0),
        LocalDateTime.of(2025, 11, 5, 10, 0));

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 5);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    assertEquals(1, target.getEventsOn(LocalDate.of(2025, 12, 1)).size());
    assertEquals(2, target.getEventsOn(LocalDate.of(2025, 12, 2)).size());
    assertEquals(LocalDateTime.of(2025, 12, 3, 9, 0),
        target.getEventsOn(LocalDate.of(2025, 12, 3)).get(0).getStart());
    assertEquals(1, target.getEventsOn(LocalDate.of(2025, 12, 3)).size());
  }


  @Test
  public void testCopyEventsBetweenMultipleDaysNewTimeZone() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/Los_Angeles");

    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEvent("Day1",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0));
    source.addEvent("Day2",
        LocalDateTime.of(2025, 11, 4, 14, 0),
        LocalDateTime.of(2025, 11, 4, 15, 0));
    source.addEvent("Day2.5",
        LocalDateTime.of(2025, 11, 4, 16, 0),
        LocalDateTime.of(2025, 11, 4, 17, 0));
    source.addEvent("Day3",
        LocalDateTime.of(2025, 11, 5, 9, 0),
        LocalDateTime.of(2025, 11, 5, 10, 0));

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 5);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    assertEquals(1, target.getEventsOn(LocalDate.of(2025, 12, 1)).size());
    assertEquals(2, target.getEventsOn(LocalDate.of(2025, 12, 2)).size());
    assertEquals(LocalDateTime.of(2025, 12, 3, 6, 0),
        target.getEventsOn(LocalDate.of(2025, 12, 3)).get(0).getStart());
    assertEquals(1, target.getEventsOn(LocalDate.of(2025, 12, 3)).size());
  }

  @Test
  public void testCopyEventsOnWithNewTimeZone() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/Los_Angeles");

    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEvent("Day2",
        LocalDateTime.of(2025, 11, 4, 14, 0),
        LocalDateTime.of(2025, 11, 4, 15, 0));
    source.addEvent("Day2.5",
        LocalDateTime.of(2025, 11, 4, 16, 0),
        LocalDateTime.of(2025, 11, 4, 17, 0));

    LocalDate startDate = LocalDate.of(2025, 11, 4);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsOn(startDate, targetStartDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    assertEquals(2, target.getEventsOn(LocalDate.of(2025, 12, 1)).size());
    assertEquals(LocalDateTime.of(2025, 12, 1, 11, 0),
        target.getEventsOn(LocalDate.of(2025, 12, 1)).get(0).getStart());
  }


  @Test
  public void testCopyEventsWithLargeTimezoneShiftToPreviousDay() {
    calendarSystem.createCalendar("USA", "America/New_York");
    calendarSystem.createCalendar("India", "Asia/Kolkata");

    Icalendar usa = calendarSystem.getCalendar("USA");
    usa.addEvent("Late Night Meeting",
        LocalDateTime.of(2025, 11, 4, 23, 0),
        LocalDateTime.of(2025, 11, 5, 0, 30));

    LocalDate startDate = LocalDate.of(2025, 11, 4);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsOn(startDate, targetStartDate, "USA", "India");

    Icalendar india = calendarSystem.getCalendar("India");

    List<Event> events = india.getEventsOn(LocalDate.of(2025, 12, 1));
    assertEquals(1, events.size());

    Event copiedEvent = events.get(0);
    assertEquals("Late Night Meeting", copiedEvent.getSubject());
    assertEquals(LocalDateTime.of(2025, 12, 1, 9, 30), copiedEvent.getStart());
    assertEquals(LocalDateTime.of(2025, 12, 1, 11, 0), copiedEvent.getEnd());
  }

  @Test
  public void testCopyEventsCrossingMidnightWithTimezone() {
    calendarSystem.createCalendar("London", "Europe/London");
    calendarSystem.createCalendar("Auckland", "Pacific/Auckland");

    Icalendar london = calendarSystem.getCalendar("London");
    london.addEvent("Late Meeting",
        LocalDateTime.of(2025, 11, 4, 23, 0),
        LocalDateTime.of(2025, 11, 5, 1, 0)); // crosses midnight

    LocalDate startDate = LocalDate.of(2025, 11, 4);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsOn(startDate, targetStartDate, "London", "Auckland");

    Icalendar auckland = calendarSystem.getCalendar("Auckland");

    List<Event> events = auckland.getEventsOn(LocalDate.of(2025, 12, 1));
    assertEquals(1, events.size());

    Event copiedEvent = events.get(0);
    assertEquals("Late Meeting", copiedEvent.getSubject());
    assertEquals(12, copiedEvent.getStart().getHour());
  }

  @Test
  public void testCopyEventsBetweenPreservesRelativePositions() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEvent("Start",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0));
    source.addEvent("End",
        LocalDateTime.of(2025, 11, 5, 10, 0),
        LocalDateTime.of(2025, 11, 5, 11, 0));

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 5);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 10);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    assertEquals(1, target.getEventsOn(LocalDate.of(2025, 12, 10)).size());
    assertEquals(0, target.getEventsOn(LocalDate.of(2025, 12, 11)).size());
    assertEquals(1, target.getEventsOn(LocalDate.of(2025, 12, 12)).size());
  }

  @Test
  public void testCopyEventsBetweenWithTimezoneConversion() {
    calendarSystem.createCalendar("India", "Asia/Kolkata");
    calendarSystem.createCalendar("USA", "America/New_York");

    Icalendar india = calendarSystem.getCalendar("India");
    india.addEvent("Morning",
        LocalDateTime.of(2025, 11, 3, 9, 0),
        LocalDateTime.of(2025, 11, 3, 10, 0));
    india.addEvent("Evening",
        LocalDateTime.of(2025, 11, 4, 20, 0),
        LocalDateTime.of(2025, 11, 4, 21, 0));

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 4);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "India", "USA");

    Icalendar usa = calendarSystem.getCalendar("USA");
    List<Event> dec1 = usa.getEventsOn(LocalDate.of(2025, 12, 1));
    List<Event> dec2 = usa.getEventsOn(LocalDate.of(2025, 12, 2));

    assertEquals(1, dec1.size());
    assertEquals(1, dec2.size());
  }

  @Test
  public void testCopyEventsBetweenInclusiveEndpoints() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEvent("StartDay",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0));
    source.addEvent("EndDay",
        LocalDateTime.of(2025, 11, 5, 10, 0),
        LocalDateTime.of(2025, 11, 5, 11, 0));

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 5);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    assertFalse(target.getEventsOn(LocalDate.of(2025, 12, 1)).isEmpty());
    assertFalse(target.getEventsOn(LocalDate.of(2025, 12, 3)).isEmpty());
  }


  @Test
  public void testCopyRecurringEventSingleOccurrence() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.W);
    weekdays.add(DayOfWeekAlphabet.F);

    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEventSeriesOccurrences("Standup",
        LocalDateTime.of(2025, 11, 3, 9, 0),
        LocalDateTime.of(2025, 11, 3, 9, 30),
        10, weekdays);

    LocalDateTime firstOccurrence = LocalDateTime.of(2025, 11, 3, 9, 0);
    LocalDateTime targetDateTime = LocalDateTime.of(2025, 12, 1, 10, 0);
    calendarSystem.copyEvent("Standup", firstOccurrence, targetDateTime, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    List<Event> events = target.getEventsOn(LocalDate.of(2025, 12, 1));
    assertEquals(1, events.size());
    assertNotNull(events.get(0).getSeriesId());
  }

  @Test
  public void testCopyEventsOnDateWithRecurringSeries() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);

    source.addEventSeriesOccurrences("Weekly Meeting",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0),
        4, weekdays);

    LocalDate sourceDate = LocalDate.of(2025, 11, 3);
    LocalDate targetDate = LocalDate.of(2025, 12, 15);
    calendarSystem.copyEventsOn(sourceDate, targetDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    List<Event> events = target.getEventsOn(targetDate);
    assertEquals(1, events.size());
    assertNotNull(events.get(0).getSeriesId());
  }

  @Test
  public void testCopyEventsBetweenPartialRecurringSeries() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.W);
    weekdays.add(DayOfWeekAlphabet.F);

    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEventSeriesOccurrences("Class",
        LocalDateTime.of(2025, 11, 3, 13, 0),
        LocalDateTime.of(2025, 11, 3, 15, 0),
        10, weekdays);

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 7);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    assertEquals(1, target.getEventsOn(LocalDate.of(2025, 12, 1)).size());
    assertEquals(0, target.getEventsOn(LocalDate.of(2025, 12, 2)).size());
    assertEquals(1, target.getEventsOn(LocalDate.of(2025, 12, 3)).size());
    assertEquals(0, target.getEventsOn(LocalDate.of(2025, 12, 4)).size());
    assertEquals(1, target.getEventsOn(LocalDate.of(2025, 12, 5)).size());

    String seriesId = target.getEventsOn(LocalDate.of(2025, 12, 1)).get(0).getSeriesId();
    assertNotNull(seriesId);
    assertEquals(seriesId, target.getEventsOn(LocalDate.of(2025, 12, 3)).get(0).getSeriesId());
    assertEquals(seriesId, target.getEventsOn(LocalDate.of(2025, 12, 5)).get(0).getSeriesId());
  }

  @Test
  public void testCopyEventsBetweenMultipleRecurringSeries() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");


    Set<DayOfWeekAlphabet> mwf = new HashSet<>();
    mwf.add(DayOfWeekAlphabet.M);
    mwf.add(DayOfWeekAlphabet.W);
    mwf.add(DayOfWeekAlphabet.F);

    Set<DayOfWeekAlphabet> tuesday = new HashSet<>();
    tuesday.add(DayOfWeekAlphabet.T);

    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEventSeriesOccurrences("Lecture",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 30),
        8, mwf);

    source.addEventSeriesOccurrences("Lab",
        LocalDateTime.of(2025, 11, 4, 14, 0),
        LocalDateTime.of(2025, 11, 4, 16, 0),
        6, tuesday);

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 10);
    LocalDate targetStartDate = LocalDate.of(2025, 12, 1);

    calendarSystem.copyEventsBetween(startDate, endDate, targetStartDate, "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");

    List<Event> allEvents = target.getEventsBetween(
        LocalDateTime.of(2025, 12, 1, 0, 0),
        LocalDateTime.of(2025, 12, 10, 23, 59));

    long lectureCount = allEvents.stream()
        .filter(e -> e.getSubject().equals("Lecture"))
        .count();
    long labCount = allEvents.stream()
        .filter(e -> e.getSubject().equals("Lab"))
        .count();

    assertTrue(lectureCount > 0);
    assertTrue(labCount > 0);
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testCopyEventSourceCalendarNotFound() {
    calendarSystem.createCalendar("Target", "America/New_York");
    calendarSystem.copyEvent("Event",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 5, 10, 0),
        "NonExistent", "Target");
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testCopyEventTargetCalendarNotFound() {
    calendarSystem.createCalendar("Source", "America/New_York");
    Icalendar source = calendarSystem.getCalendar("Source");
    source.addEvent("Event",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0));

    calendarSystem.copyEvent("Event",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 5, 10, 0),
        "Source", "NonExistent");
  }

  @Test(expected = DuplicateEventException.class)
  public void testCopyEventsHandlesDuplicates() {
    calendarSystem.createCalendar("Cal", "America/New_York");
    Icalendar cal = calendarSystem.getCalendar("Cal");

    cal.addEvent("Event",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0));
    cal.addEvent("Event",
        LocalDateTime.of(2025, 11, 5, 10, 0),
        LocalDateTime.of(2025, 11, 5, 11, 0));


    calendarSystem.copyEvent("Event",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 5, 10, 0),
        "Cal", "Cal");

  }

  @Test
  public void testIntegrationCompleteWorkflow() {

    calendarSystem.createCalendar("Personal", "America/Los_Angeles");
    calendarSystem.createCalendar("Work", "America/New_York");
    calendarSystem.createCalendar("Archive", "Europe/London");

    Icalendar personal = calendarSystem.getCalendar("Personal");
    personal.addEvent("Gym",
        LocalDateTime.of(2025, 11, 3, 6, 0),
        LocalDateTime.of(2025, 11, 3, 7, 0));
    personal.addEvent("Dinner",
        LocalDateTime.of(2025, 11, 3, 18, 0),
        LocalDateTime.of(2025, 11, 3, 19, 30));

    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.T);
    weekdays.add(DayOfWeekAlphabet.W);
    weekdays.add(DayOfWeekAlphabet.R);
    weekdays.add(DayOfWeekAlphabet.F);

    Icalendar work = calendarSystem.getCalendar("Work");
    work.addEventSeriesOccurrences("Standup",
        LocalDateTime.of(2025, 11, 3, 9, 0),
        LocalDateTime.of(2025, 11, 3, 9, 15),
        20, weekdays);

    calendarSystem.copyEventsOn(LocalDate.of(2025, 11, 3),
        LocalDate.of(2025, 11, 10),
        "Personal", "Work");

    calendarSystem.copyEventsBetween(
        LocalDate.of(2025, 11, 3),
        LocalDate.of(2025, 11, 7),
        LocalDate.of(2025, 12, 1),
        "Work", "Archive");

    calendarSystem.renameCalendar("Personal", "Personal-PST");

    calendarSystem.changeTimezone("Archive", "Asia/Tokyo");

    assertNotNull(calendarSystem.getCalendar("Personal-PST"));
    assertNotNull(calendarSystem.getCalendar("Work"));
    assertNotNull(calendarSystem.getCalendar("Archive"));

    Icalendar workCal = calendarSystem.getCalendar("Work");
    assertTrue(workCal.getEventsOn(LocalDate.of(2025, 11, 10)).size() >= 2);

    Icalendar archiveCal = calendarSystem.getCalendar("Archive");
    assertEquals("Asia/Tokyo", archiveCal.getCalendarTimeZone().getId());
  }

  @Test
  public void testCopySingleEventFromSeriesPreservesSeriesId() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);

    source.addEventSeriesOccurrences("Meeting",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 0),
        3, weekdays);

    String originalSeriesId = source.getEventsOn(LocalDate.of(2025, 11, 3))
        .get(0).getSeriesId();

    calendarSystem.copyEvent("Meeting",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 12, 1, 10, 0),
        "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    Event copiedEvent = target.getEventsOn(LocalDate.of(2025, 12, 1)).get(0);

    assertNotNull(copiedEvent.getSeriesId());

    assertEquals(originalSeriesId, copiedEvent.getSeriesId());
  }

  /**
   * Tests copying single event maintains series even across timezones.
   */
  @Test
  public void testCopySingleEventAcrossTimezonesPreservesSeries() {
    calendarSystem.createCalendar("EST", "America/New_York");
    calendarSystem.createCalendar("PST", "America/Los_Angeles");

    Icalendar est = calendarSystem.getCalendar("EST");
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.W);

    est.addEventSeriesOccurrences("Standup",
        LocalDateTime.of(2025, 11, 5, 9, 0),
        LocalDateTime.of(2025, 11, 5, 9, 30),
        4, weekdays);

    String originalSeriesId = est.getEventsOn(LocalDate.of(2025, 11, 5))
        .get(0).getSeriesId();

    calendarSystem.copyEvent("Standup",
        LocalDateTime.of(2025, 11, 5, 9, 0),
        LocalDateTime.of(2025, 12, 10, 6, 0),
        "EST", "PST");

    Icalendar pst = calendarSystem.getCalendar("PST");
    Event copied = pst.getEventsOn(LocalDate.of(2025, 12, 10)).get(0);

    assertNotNull(copied.getSeriesId());
    assertEquals(originalSeriesId, copied.getSeriesId());
  }

  @Test
  public void testCopyEventsOnDatePreservesSeriesId() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.W);
    weekdays.add(DayOfWeekAlphabet.F);
    Icalendar source = calendarSystem.getCalendar("Source");

    source.addEventSeriesOccurrences("Lecture",
        LocalDateTime.of(2025, 11, 3, 13, 0),
        LocalDateTime.of(2025, 11, 3, 15, 0),
        8, weekdays);

    String originalSeriesId = source.getEventsOn(LocalDate.of(2025, 11, 3))
        .get(0).getSeriesId();

    calendarSystem.copyEventsOn(
        LocalDate.of(2025, 11, 3),
        LocalDate.of(2025, 12, 1),
        "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    Event copied = target.getEventsOn(LocalDate.of(2025, 12, 1)).get(0);

    assertNotNull(copied.getSeriesId());
    assertEquals(originalSeriesId, copied.getSeriesId());
  }

  /**
   * Tests copying multiple series events on same day preserves all series.
   */
  @Test
  public void testCopyMultipleSeriesEventsOnDate() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");

    Set<DayOfWeekAlphabet> mondays = new HashSet<>();
    mondays.add(DayOfWeekAlphabet.M);

    source.addEventSeriesOccurrences("Morning Meeting",
        LocalDateTime.of(2025, 11, 3, 9, 0),
        LocalDateTime.of(2025, 11, 3, 10, 0),
        3, mondays);

    source.addEventSeriesOccurrences("Afternoon Sync",
        LocalDateTime.of(2025, 11, 3, 14, 0),
        LocalDateTime.of(2025, 11, 3, 15, 0),
        3, mondays);

    List<Event> sourceEvents = source.getEventsOn(LocalDate.of(2025, 11, 3));
    assertEquals(2, sourceEvents.size());

    String series1Id = sourceEvents.get(0).getSeriesId();
    String series2Id = sourceEvents.get(1).getSeriesId();
    assertNotEquals(series1Id, series2Id);

    calendarSystem.copyEventsOn(
        LocalDate.of(2025, 11, 3),
        LocalDate.of(2025, 12, 15),
        "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    List<Event> targetEvents = target.getEventsOn(LocalDate.of(2025, 12, 15));
    assertEquals(2, targetEvents.size());

    assertNotNull(targetEvents.get(0).getSeriesId());
    assertNotNull(targetEvents.get(1).getSeriesId());

    Set<String> targetSeriesIds = new HashSet<>();
    targetSeriesIds.add(targetEvents.get(0).getSeriesId());
    targetSeriesIds.add(targetEvents.get(1).getSeriesId());

    assertTrue(targetSeriesIds.contains(series1Id));
    assertTrue(targetSeriesIds.contains(series2Id));
  }


  /**
   * Tests that copying partial series preserves series membership.
   */
  @Test
  public void testCopyPartialSeriesPreservesSeriesId() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.T);
    weekdays.add(DayOfWeekAlphabet.R);

    source.addEventSeriesOccurrences("Lab",
        LocalDateTime.of(2025, 11, 4, 14, 0),
        LocalDateTime.of(2025, 11, 4, 16, 0),
        8, weekdays);

    calendarSystem.copyEventsBetween(
        LocalDate.of(2025, 11, 4),
        LocalDate.of(2025, 11, 7),
        LocalDate.of(2025, 12, 1),
        "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");

    Event tuesday = target.getEventsOn(LocalDate.of(2025, 12, 1)).get(0);
    Event thursday = target.getEventsOn(LocalDate.of(2025, 12, 3)).get(0);

    assertNotNull(tuesday.getSeriesId());
    assertNotNull(thursday.getSeriesId());

    assertEquals(tuesday.getSeriesId(), thursday.getSeriesId());

    String originalSeriesId = source.getEventsOn(LocalDate.of(2025, 11, 4))
        .get(0).getSeriesId();

    assertEquals(originalSeriesId, tuesday.getSeriesId());
  }

  /**
   * Tests copying full series maintains all series memberships.
   */
  @Test
  public void testCopyFullSeriesMaintainsAllMemberships() {
    calendarSystem.createCalendar("Fall2025", "America/New_York");
    calendarSystem.createCalendar("Spring2026", "America/New_York");

    Set<DayOfWeekAlphabet> mwf = new HashSet<>();
    mwf.add(DayOfWeekAlphabet.M);
    mwf.add(DayOfWeekAlphabet.W);
    mwf.add(DayOfWeekAlphabet.F);

    Icalendar fall = calendarSystem.getCalendar("Fall2025");

    fall.addEventSeriesOccurrences("CS5004 Lecture",
        LocalDateTime.of(2025, 11, 3, 10, 0),
        LocalDateTime.of(2025, 11, 3, 11, 30),
        10, mwf);

    calendarSystem.copyEventsBetween(
        LocalDate.of(2025, 11, 3),
        LocalDate.of(2025, 11, 30),
        LocalDate.of(2026, 1, 5),
        "Fall2025", "Spring2026");

    Icalendar spring = calendarSystem.getCalendar("Spring2026");

    List<Event> allCopied = spring.getEventsBetween(
        LocalDateTime.of(2026, 1, 5, 0, 0),
        LocalDateTime.of(2026, 2, 5, 23, 59));

    assertTrue(allCopied.size() >= 5); // At least 5 occurrences

    String copiedSeriesId = allCopied.get(0).getSeriesId();
    assertNotNull(copiedSeriesId);

    for (Event event : allCopied) {
      assertNotNull(event.getSeriesId());
      assertEquals(copiedSeriesId, event.getSeriesId());
    }

    String originalSeriesId = fall.getEventsOn(LocalDate.of(2025, 11, 3))
        .get(0).getSeriesId();

    assertEquals(originalSeriesId, copiedSeriesId);
  }

  /**
   * Tests copying series across timezones preserves series membership.
   */
  @Test
  public void testCopySeriesAcrossTimezonesPreservesSeries() {
    calendarSystem.createCalendar("India", "Asia/Kolkata");
    calendarSystem.createCalendar("USA", "America/New_York");

    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    weekdays.add(DayOfWeekAlphabet.M);
    weekdays.add(DayOfWeekAlphabet.W);
    weekdays.add(DayOfWeekAlphabet.F);

    Icalendar india = calendarSystem.getCalendar("India");
    india.addEventSeriesOccurrences("Daily Standup",
        LocalDateTime.of(2025, 11, 3, 9, 0),
        LocalDateTime.of(2025, 11, 3, 9, 30),
        6, weekdays);

    calendarSystem.copyEventsBetween(
        LocalDate.of(2025, 11, 3),
        LocalDate.of(2025, 11, 14),
        LocalDate.of(2025, 12, 1),
        "India", "USA");

    Icalendar usa = calendarSystem.getCalendar("USA");
    List<Event> copiedEvents = usa.getEventsBetween(
        LocalDateTime.of(2025, 12, 1, 0, 0),
        LocalDateTime.of(2025, 12, 15, 23, 59));

    assertTrue(copiedEvents.size() >= 3);

    String copiedSeriesId = copiedEvents.get(0).getSeriesId();
    for (Event event : copiedEvents) {
      assertNotNull(event.getSeriesId());
      assertEquals(copiedSeriesId, event.getSeriesId());
    }

    String originalSeriesId = india.getEventsOn(LocalDate.of(2025, 11, 3))
        .get(0).getSeriesId();
    assertEquals(originalSeriesId, copiedSeriesId);
  }

  /**
   * Tests copying overlapping series events maintains distinct series.
   */
  @Test
  public void testCopyOverlappingSeriesMaintainsDistinctSeries() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");

    Set<DayOfWeekAlphabet> tuesdays = new HashSet<>();
    tuesdays.add(DayOfWeekAlphabet.T);

    Set<DayOfWeekAlphabet> thursdays = new HashSet<>();
    thursdays.add(DayOfWeekAlphabet.R);

    source.addEventSeriesOccurrences("Series A",
        LocalDateTime.of(2025, 11, 4, 10, 0),
        LocalDateTime.of(2025, 11, 4, 11, 0),
        4, tuesdays);

    source.addEventSeriesOccurrences("Series B",
        LocalDateTime.of(2025, 11, 6, 14, 0),
        LocalDateTime.of(2025, 11, 6, 15, 0),
        4, thursdays);

    String seriesOneId = source.getEventsOn(LocalDate.of(2025, 11, 4))
        .get(0).getSeriesId();
    String seriesTwoId = source.getEventsOn(LocalDate.of(2025, 11, 6))
        .get(0).getSeriesId();

    assertNotEquals(seriesOneId, seriesTwoId);

    calendarSystem.copyEventsBetween(
        LocalDate.of(2025, 11, 4),
        LocalDate.of(2025, 11, 20),
        LocalDate.of(2025, 12, 1),
        "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");

    List<Event> copiedA = target.getEventsBetween(
        LocalDateTime.of(2025, 12, 1, 9, 0),
        LocalDateTime.of(2025, 12, 1, 12, 0));

    List<Event> copiedB = target.getEventsBetween(
        LocalDateTime.of(2025, 12, 3, 13, 0),
        LocalDateTime.of(2025, 12, 3, 16, 0));

    assertFalse(copiedA.isEmpty());
    assertFalse(copiedB.isEmpty());

    String copiedOneId = copiedA.get(0).getSeriesId();
    String copiedTwoId = copiedB.get(0).getSeriesId();

    assertNotNull(copiedOneId);
    assertNotNull(copiedTwoId);
    assertNotEquals(copiedOneId, copiedTwoId);

    assertEquals(seriesOneId, copiedOneId);
    assertEquals(seriesTwoId, copiedTwoId);
  }

  /**
   * Tests that non-series events don't get seriesId after copy.
   */
  @Test
  public void testCopyNonSeriesEventDoesNotAddSeriesId() {
    calendarSystem.createCalendar("Source", "America/New_York");
    calendarSystem.createCalendar("Target", "America/New_York");

    Icalendar source = calendarSystem.getCalendar("Source");

    source.addEvent("One-time Meeting",
        LocalDateTime.of(2025, 11, 5, 10, 0),
        LocalDateTime.of(2025, 11, 5, 11, 0));

    Event original = source.getEventsOn(LocalDate.of(2025, 11, 5)).get(0);
    assertNull(original.getSeriesId());

    calendarSystem.copyEvent("One-time Meeting",
        LocalDateTime.of(2025, 11, 5, 10, 0),
        LocalDateTime.of(2025, 12, 1, 10, 0),
        "Source", "Target");

    Icalendar target = calendarSystem.getCalendar("Target");
    Event copied = target.getEventsOn(LocalDate.of(2025, 12, 1)).get(0);

    assertNull(copied.getSeriesId());
  }

}
