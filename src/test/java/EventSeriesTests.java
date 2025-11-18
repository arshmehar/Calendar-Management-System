import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import controller.DayOfWeekAlphabet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Event;
import model.EventSeries;
import model.EventStatus;
import org.junit.Test;

/**
 * Test class for EventSeries.
 * Tests series creation, validation, event generation, and edge cases
 * for recurring calendar events.
 */
public class EventSeriesTests {

  /**
   * Tests basic event series creation with end date.
   */
  @Test
  public void basicEventSeries() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("Pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests basic event series creation with occurrences.
   */
  @Test
  public void basicEventSeries2() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .occurrences(30)
        .days(days)
        .build();
  }

  /**
   * Tests event series creation with all optional fields set.
   */
  @Test
  public void basicEventSeries3() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);
    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .occurrences(30)
        .days(days)
        .description("module 1 OOPs")
        .location("room 101")
        .status(EventStatus.PRIVATE)
        .build();
  }

  /**
   * Tests that missing subject throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void missingRequired1() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that missing start time throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void missingRequired2() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that missing start date throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void missingRequired3() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that missing end time throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void missingRequired4() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that missing recurring days throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void missingRequired5() {
    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .build();
  }

  /**
   * Tests that missing both end date and occurrences throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void missingRequired6() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .days(days)
        .build();
  }

  /**
   * Tests that providing both occurrences and end date throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void missingRequired7() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("Pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .occurrences(22)
        .build();
  }

  /**
   * Tests that empty days set throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void missingRequired8() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();

    EventSeries series = EventSeries.getBuilder()
        .subject("Pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that null subject throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void nullRequired1() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject(null)
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that null start time throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void nullRequired2() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp_class")
        .startTime(null)
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that null start date throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void nullRequired3() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp_class")
        .startTime(LocalTime.of(9, 50))
        .startDate(null)
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that null end time throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void nullRequired4() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp_class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(null)
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that null days set throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void nullRequired5() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp_class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 30))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(null)
        .build();
  }

  /**
   * Tests that whitespace-only subject throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void invalidSubject() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("    ")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .occurrences(30)
        .days(days)
        .build();
  }

  /**
   * Tests that empty string subject throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void invalidSubject2() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .occurrences(30)
        .days(days).build();
  }

  /**
   * Tests that end time equal to start time throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void endEqualsStart() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("Pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(9, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that end date before start date throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void endBeforeStart() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("Pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 9, 1))
        .days(days)
        .build();
  }

  /**
   * Tests that end time before start time throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void endBeforeStart2() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("Pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(9, 49))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests that zero occurrences throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void invalidOccurrences() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .occurrences(0)
        .days(days)
        .build();
  }

  /**
   * Tests that negative occurrences throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void invalidOccurrences2() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .occurrences(-20)
        .days(days)
        .build();
  }

  /**
   * Tests event series with single recurring day.
   */
  @Test
  public void daysSingular() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("PdpLAB")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests event series recurring all 7 days of the week.
   */
  @Test
  public void days1() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.THURSDAY);
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.SATURDAY);
    days.add(DayOfWeek.SUNDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("PdpLAB")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();
  }

  /**
   * Tests event generation with occurrences where start date is not a recurring day.
   */
  @Test
  public void generateEvents() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .occurrences(3).days(days).build();

    List<Event> generated = series.generateEvents();

    assertEquals(3, generated.size());

    for (Event singleEvent : generated) {
      assertEquals("pdp class", singleEvent.getSubject());
      assertEquals(LocalTime.of(9, 50), singleEvent.getStart().toLocalTime());
      assertEquals(LocalTime.of(11, 50), singleEvent.getEnd().toLocalTime());

      DayOfWeek day = singleEvent.getStart().getDayOfWeek();
      assertTrue(day == DayOfWeek.TUESDAY || day == DayOfWeek.FRIDAY);

      assertNotEquals(LocalDate.of(2025, 9, 16),
          singleEvent.getStart().toLocalDate());

      assertNotEquals(LocalDate.of(2025, 9, 2),
          singleEvent.getStart().toLocalDate());

      assertNotEquals(LocalDate.of(2025, 9, 2),
          singleEvent.getStart().toLocalDate());

      assertNotEquals(LocalDate.of(2025, 9, 16),
          singleEvent.getStart().toLocalDate());

      assertEquals(LocalDate.of(2025, 9, 5),
          generated.get(0).getStart().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 9),
          generated.get(1).getStart().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 12),
          generated.get(2).getStart().toLocalDate());

      assertEquals(LocalDate.of(2025, 9, 5),
          generated.get(0).getEnd().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 9),
          generated.get(1).getEnd().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 12),
          generated.get(2).getEnd().toLocalDate());
    }
  }

  /**
   * Tests event generation with end date where start and end are on recurring days.
   */
  @Test
  public void generateEvents2() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 5))
        .endDate(LocalDate.of(2025, 9, 12))
        .days(days).build();

    List<Event> generated = series.generateEvents();
    for (Event singleEvent : generated) {
      assertEquals("pdp class", singleEvent.getSubject());
      assertEquals(LocalTime.of(9, 50), singleEvent.getStart().toLocalTime());
      assertEquals(LocalTime.of(11, 50), singleEvent.getEnd().toLocalTime());

      DayOfWeek day = singleEvent.getStart().getDayOfWeek();
      assertFalse(day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY
          || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
          || day == DayOfWeek.THURSDAY);
      assertTrue(day == DayOfWeek.TUESDAY || day == DayOfWeek.FRIDAY);

      assertEquals(LocalDate.of(2025, 9, 5),
          generated.get(0).getStart().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 9),
          generated.get(1).getStart().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 12),
          generated.get(2).getStart().toLocalDate());

      assertEquals(LocalDate.of(2025, 9, 5),
          generated.get(0).getEnd().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 9),
          generated.get(1).getEnd().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 12),
          generated.get(2).getEnd().toLocalDate());
    }
  }

  /**
   * Tests event generation with edge case times and dates.
   */
  @Test
  public void generateEvents3() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(12, 0))
        .startDate(LocalDate.of(2025, 8, 29))
        .occurrences(2).days(days).build();

    List<Event> generated = series.generateEvents();

    for (Event singleEvent : generated) {
      assertNotEquals(LocalTime.of(8, 49), singleEvent.getStart().toLocalTime());
      assertNotEquals(LocalTime.of(12, 1), singleEvent.getEnd().toLocalTime());

      assertNotEquals(LocalDate.of(2025, 8, 26),
          singleEvent.getStart().toLocalDate());

      assertNotEquals(LocalDate.of(2025, 9, 3),
          singleEvent.getStart().toLocalDate());

      assertEquals(LocalDate.of(2025, 8, 29),
          generated.get(0).getStart().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 2),
          generated.get(1).getStart().toLocalDate());

      assertEquals(LocalDate.of(2025, 8, 29),
          generated.get(0).getEnd().toLocalDate());
      assertEquals(LocalDate.of(2025, 9, 2),
          generated.get(1).getEnd().toLocalDate());
    }
  }

  /**
   * Tests that when end date equals start date and day is not in recurring list,
   * an empty list is returned.
   */
  @Test
  public void generateEvents4() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endDate(LocalDate.of(2025, 9, 3))
        .days(days).build();

    List<Event> generated = series.generateEvents();

    assertEquals(0, generated.size());
    assertTrue(generated.isEmpty());
  }

  /**
   * Tests event generation with end date on non-matching day and optional fields.
   */
  @Test
  public void generateEvents5() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 2))
        .endDate(LocalDate.of(2025, 9, 8))
        .days(days)
        .description("fill survey before class")
        .location("room 101, churchill hall")
        .status(EventStatus.PRIVATE)
        .build();

    List<Event> generated = series.generateEvents();
    for (Event singleEvent : generated) {
      assertEquals("fill survey before class", singleEvent.getDescription());
      assertEquals("room 101, churchill hall", singleEvent.getLocation());
      assertEquals(EventStatus.PRIVATE, singleEvent.getStatus());

      assertEquals(LocalDate.of(2025, 9, 2),
          generated.get(0).getStart().toLocalDate());

      assertNotEquals(LocalDate.of(2025, 9, 8),
          generated.get(1).getStart().toLocalDate());
    }
  }

  /**
   * Tests all getter methods return correct values.
   */
  @Test
  public void gettersTest() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);
    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .occurrences(30)
        .days(days)
        .description("module 1 OOPs")
        .location("room 101")
        .status(EventStatus.PRIVATE)
        .build();

    assertEquals("pdp class", series.subject());
    assertEquals(LocalTime.of(9, 50), series.getStartTime());
    assertEquals(LocalTime.of(11, 50), series.getEndTime());
    assertEquals(LocalDate.of(2025, 9, 3), series.startDate());
    assertEquals(30, (int) series.getOccurrences());
    assertEquals("module 1 OOPs", series.getDescription());
    assertEquals("room 101", series.getLocation());
    assertEquals(EventStatus.PRIVATE, series.getStatus());
    assertNull(series.getEndDate());
  }

  /**
   * Tests that optional getters return null when fields not set.
   */
  @Test
  public void optionalgettersNull() {
    Set<DayOfWeek> days = new HashSet<DayOfWeek>();
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("Pdp class")
        .startTime(LocalTime.of(9, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .endTime(LocalTime.of(11, 50))
        .endDate(LocalDate.of(2025, 12, 22))
        .days(days)
        .build();

    assertEquals(LocalDate.of(2025, 12, 22), series.getEndDate());
    assertNull(series.getDescription());
    assertNull(series.getLocation());
    assertNull(series.getStatus());
    assertNull(series.getOccurrences());
  }

  /**
   * Tests that the returned days set is immutable.
   */
  @Test
  public void daysSetImmutable() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("pdp class")
        .startTime(LocalTime.of(9, 50))
        .endTime(LocalTime.of(11, 50))
        .startDate(LocalDate.of(2025, 9, 3))
        .occurrences(3).days(days).build();

    Set<DayOfWeek> returnedDays = series.days();

    int originalSize = returnedDays.size();
    try {
      returnedDays.add(DayOfWeek.MONDAY);
      List<Event> events = series.generateEvents();
      for (Event event : events) {
        DayOfWeek day = event.getStart().getDayOfWeek();
        assertNotEquals(DayOfWeek.MONDAY, day);
      }
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    }
  }

  /**
   * Tests event generation when end date equals start and is a matching day.
   */
  @Test
  public void generateEventsSameDateMatchingDay() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("one time event")
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(11, 0))
        .startDate(LocalDate.of(2025, 9, 5))
        .endDate(LocalDate.of(2025, 9, 5))
        .days(days)
        .build();

    List<Event> generated = series.generateEvents();

    assertEquals(1, generated.size());

    Event event = generated.get(0);
    assertEquals("one time event", event.getSubject());
    assertEquals(LocalDate.of(2025, 9, 5), event.getStart().toLocalDate());
    assertEquals(DayOfWeek.FRIDAY, event.getStart().getDayOfWeek());
  }

  /**
   * Tests event generation for very long series with 150 occurrences.
   */
  @Test
  public void generateEventsVeryLongSeries() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("daily standup")
        .startTime(LocalTime.of(9, 0))
        .endTime(LocalTime.of(9, 15))
        .startDate(LocalDate.of(2025, 1, 1))
        .occurrences(150)
        .days(days)
        .build();

    List<Event> generated = series.generateEvents();

    assertEquals(150, generated.size());

    for (Event event : generated) {
      assertEquals("daily standup", event.getSubject());
      assertEquals(LocalTime.of(9, 0), event.getStart().toLocalTime());
      assertEquals(LocalTime.of(9, 15), event.getEnd().toLocalTime());

      DayOfWeek day = event.getStart().getDayOfWeek();
      assertTrue(day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY
          || day == DayOfWeek.FRIDAY);
    }

    assertEquals(LocalDate.of(2025, 1, 1),
        generated.get(0).getStart().toLocalDate());

    LocalDate lastDate = generated.get(149).getStart().toLocalDate();
    assertTrue(lastDate.isAfter(LocalDate.of(2025, 1, 1)));
  }

  /**
   * Tests event series with single occurrence.
   */
  @Test
  public void singleOccurrence() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.THURSDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("one time meeting")
        .startTime(LocalTime.of(14, 0))
        .endTime(LocalTime.of(15, 0))
        .startDate(LocalDate.of(2025, 9, 4))
        .occurrences(1)
        .days(days)
        .build();

    List<Event> generated = series.generateEvents();

    assertEquals(1, generated.size());

    Event event = generated.get(0);
    assertEquals("one time meeting", event.getSubject());
    assertEquals(LocalTime.of(14, 0), event.getStart().toLocalTime());
    assertEquals(LocalTime.of(15, 0), event.getEnd().toLocalTime());
    assertEquals(LocalDate.of(2025, 9, 4), event.getStart().toLocalDate());
    assertEquals(DayOfWeek.THURSDAY, event.getStart().getDayOfWeek());
  }

  /**
   * Tests event generation at midnight (00:00 to 01:00).
   */
  @Test
  public void generateEventsAtMidnight() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.SATURDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("midnight event")
        .startTime(LocalTime.of(0, 0))
        .endTime(LocalTime.of(1, 0))
        .startDate(LocalDate.of(2025, 9, 6))
        .occurrences(2)
        .days(days)
        .build();

    List<Event> generated = series.generateEvents();

    assertEquals(2, generated.size());

    for (Event event : generated) {
      assertEquals(LocalTime.of(0, 0), event.getStart().toLocalTime());
      assertEquals(LocalTime.of(1, 0), event.getEnd().toLocalTime());
    }
  }

  /**
   * Tests event generation late at night (23:00 to 23:59).
   */
  @Test
  public void generateEventsLateNight() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.SUNDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("late night event")
        .startTime(LocalTime.of(23, 0))
        .endTime(LocalTime.of(23, 59))
        .startDate(LocalDate.of(2025, 9, 7))
        .occurrences(2)
        .days(days)
        .build();

    List<Event> generated = series.generateEvents();

    assertEquals(2, generated.size());

    for (Event event : generated) {
      assertEquals(LocalTime.of(23, 0), event.getStart().toLocalTime());
      assertEquals(LocalTime.of(23, 59), event.getEnd().toLocalTime());
      assertEquals(DayOfWeek.SUNDAY, event.getStart().getDayOfWeek());
    }
  }

  /**
   * Tests that event series cannot span midnight (end time before start time fails).
   */
  @Test(expected = IllegalStateException.class)
  public void eventSeriesCannotSpanMidnight() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = EventSeries.getBuilder()
        .subject("late night event")
        .startTime(LocalTime.of(23, 30))
        .endTime(LocalTime.of(1, 30))
        .startDate(LocalDate.of(2025, 9, 5))
        .occurrences(2)
        .days(days)
        .build();
  }

  @Test
  public void testEventSeriesDaysGetter() {
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);
    weekdays.add(DayOfWeek.TUESDAY);
    weekdays.add(DayOfWeek.FRIDAY);


    EventSeries series = EventSeries.getBuilder()
        .subject("Lecture")
        .startTime(LocalTime.of(21, 30))
        .endTime(LocalTime.of(22, 30))
        .startDate(LocalDate.of(2025, 9, 5))
        .occurrences(2)
        .days(weekdays)
        .build();

    Set<DayOfWeek> returnedDays = series.days();

    assertNotNull(returnedDays);
    assertEquals(3, returnedDays.size());
    assertTrue(returnedDays.contains(DayOfWeek.MONDAY));
    assertTrue(returnedDays.contains(DayOfWeek.FRIDAY));
    assertTrue(returnedDays.contains(DayOfWeek.TUESDAY));
  }
}