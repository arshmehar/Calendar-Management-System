import static model.EventStatus.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import model.Event;
import model.EventKey;
import model.EventStatus;
import org.junit.Test;

/**
 * Test class for Event.
 * Tests event creation, validation, builder pattern, equality,
 * all-day events, and edge cases.
 */
public class EventTests {

  /**
   * Tests basic event creation with required fields only.
   */
  @Test
  public void testBasicEventCreation() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2025, 3, 5, 20, 0);
    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .end(en)
        .build();
    assertEquals(subj, event.getSubject());
    assertEquals(st, event.getStart());
    assertEquals(en, event.getEnd());
    assertEquals(null, event.getDescription());
    assertEquals(null, event.getLocation());
    assertEquals(null, event.getStatus());
  }

  /**
   * Tests event spanning multiple days.
   */
  @Test
  public void eventSpanningMultipleDays() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2025, 3, 7, 20, 0);
    String description = "Description";
    String location = null;
    Event event = Event.getBuilder()
        .start(st)
        .end(en)
        .subject(subj)
        .build();
  }

  /**
   * Tests that missing subject throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void incompleteEvent() {
    String subj = "birthday";
    Event event = Event.getBuilder().subject(subj).build();
  }

  /**
   * Tests that missing end time throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void incompleteEvent2() {
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);

    Event event = Event.getBuilder().start(st).build();
  }

  /**
   * Tests that only providing end time throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void incompleteEvent3() {
    LocalDateTime en = LocalDateTime.of(2025, 3, 5, 20, 0);
    Event event = Event.getBuilder().end(en).build();
  }

  /**
   * Tests that null start time throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void nullStart() {
    String subj = "birthday";
    LocalDateTime st = null;
    Event event = Event.getBuilder().subject(subj).start(st).build();
  }

  /**
   * Tests that null end time creates an all-day event.
   */
  @Test
  public void nullEnd() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = null;
    Event event = Event.getBuilder().subject(subj).start(st).end(en).build();
  }

  /**
   * Tests that end time before start time on same day throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void endBeforeStart() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2025, 3, 5, 5, 0);
    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .end(en)
        .build();
  }

  /**
   * Tests that end one year prior to start throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void endBeforeStart2() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2024, 1, 5, 20, 0);
    Event event = Event.getBuilder().subject(subj).start(st).end(en).build();
  }

  /**
   * Tests that end one month prior to start throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void endBeforeStart3() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2025, 1, 5, 20, 0);
    Event event = Event.getBuilder().subject(subj).start(st).end(en).build();
  }

  /**
   * Tests that end one minute prior to start throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void endBeforeStart4() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2025, 3, 5, 10, 59);
    Event event = Event.getBuilder().subject(subj).start(st).end(en).build();
  }

  /**
   * Tests that start and end exactly equal throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void endequalsStart5() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2025, 3, 5, 11, 0);
    Event event = Event.getBuilder().subject(subj).start(st).end(en).build();
  }

  /**
   * Tests all-day event creation (8 AM to 5 PM when no end provided).
   */
  @Test
  public void allDayEventCreation() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .build();
    assertEquals(subj, event.getSubject());
    assertEquals(LocalTime.of(8, 0), event.getStart().toLocalTime());
    assertEquals(LocalTime.of(17, 0), event.getEnd().toLocalTime());
    assertEquals(LocalDate.of(2025, 3, 5), event.getStart().toLocalDate());
  }

  /**
   * Tests equals and hashCode methods.
   */
  @Test
  public void equalAndHashcode() {
    String subj = "birthday";
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2025, 3, 5, 20, 0);


    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .end(en)
        .build();

    String subj2 = "birthday";
    LocalDateTime st2 = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en2 = LocalDateTime.of(2025, 3, 5, 20, 0);
    Event event2 = Event.getBuilder()
        .subject(subj2)
        .start(st2)
        .end(en2)
        .build();

    assertEquals(event, event2);
    assertEquals(event.hashCode(), event2.hashCode());
    assertEquals(event, event);

    String subj3 = "birthdays";
    LocalDateTime st3 = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en3 = LocalDateTime.of(2025, 3, 5, 20, 0);
    Event event3 = Event.getBuilder()
        .subject(subj3)
        .start(st3)
        .end(en3)
        .build();

    assertNotEquals(event, event3);
    assertNotEquals(event.hashCode(), event3.hashCode());
    assertNotEquals("string, not equals", event);
    assertNotEquals(null, event);
  }

  /**
   * Tests that events with different optional fields are still equal.
   */
  @Test
  public void equalsDifferentOptionalFields() {
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2025, 3, 5, 20, 0);

    Event event1 = Event.getBuilder()
        .subject("Meeting")
        .start(st)
        .end(en)
        .location("Room A")
        .status(EventStatus.PRIVATE)
        .build();

    Event event2 = Event.getBuilder()
        .subject("Meeting")
        .start(st)
        .end(en)
        .location("Room B")
        .build();

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  /**
   * Tests that empty subject throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void emptySubject() {
    Event.getBuilder()
        .subject("")
        .start(LocalDateTime.of(2025, 3, 5, 11, 0))
        .end(LocalDateTime.of(2025, 3, 5, 20, 0))
        .build();
  }

  /**
   * Tests that whitespace-only subject throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void whitespaceSubject() {
    Event.getBuilder()
        .subject("   ")
        .start(LocalDateTime.of(2025, 3, 5, 11, 0))
        .end(LocalDateTime.of(2025, 3, 5, 20, 25))
        .build();
  }

  /**
   * Tests that null subject throws IllegalStateException.
   */
  @Test(expected = IllegalStateException.class)
  public void nullSubject() {
    Event.getBuilder()
        .subject(null)
        .start(LocalDateTime.of(2025, 3, 5, 11, 0))
        .end(LocalDateTime.of(2025, 3, 5, 20, 25))
        .build();
  }

  /**
   * Tests all getter methods return correct values.
   */
  @Test
  public void testGetters() {
    LocalDateTime st = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime en = LocalDateTime.of(2025, 3, 5, 20, 35);

    Event event = Event.getBuilder()
        .subject("birthday").start(st).end(en)
        .description("party!party!party!").location("home")
        .status(PUBLIC).build();

    assertEquals("birthday", event.getSubject());
    assertEquals(LocalTime.of(11, 0), event.getStart().toLocalTime());
    assertEquals(LocalTime.of(20, 35), event.getEnd().toLocalTime());
    assertEquals(LocalDate.of(2025, 3, 5), event.getStart().toLocalDate());
    assertEquals(LocalDate.of(2025, 3, 5), event.getEnd().toLocalDate());
    assertEquals(LocalDateTime.of(2025, 3, 5, 11, 0), event.getStart());
    assertEquals("party!party!party!", event.getDescription());
    assertEquals("home", event.getLocation());
    assertEquals(EventStatus.PUBLIC, event.getStatus());
  }

  /**
   * Tests event spanning year boundary (Dec 31 to Jan 1).
   */
  @Test
  public void eventSpanningYearBoundary() {
    String subj = "new years";
    LocalDateTime st = LocalDateTime.of(2025, 12, 31, 22, 0);
    LocalDateTime en = LocalDateTime.of(2026, 1, 1, 2, 0);

    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .end(en)
        .build();

    assertEquals(subj, event.getSubject());
    assertEquals(LocalDate.of(2025, 12, 31), event.getStart().toLocalDate());
    assertEquals(LocalDate.of(2026, 1, 1), event.getEnd().toLocalDate());
    assertEquals(LocalTime.of(22, 0), event.getStart().toLocalTime());
    assertEquals(LocalTime.of(2, 0), event.getEnd().toLocalTime());
  }

  /**
   * Tests event at midnight (00:00 start time).
   */
  @Test
  public void eventAtMidnight() {
    String subj = "Midnight Event";
    LocalDateTime st = LocalDateTime.of(2025, 5, 5, 0, 0);
    LocalDateTime en = LocalDateTime.of(2025, 5, 5, 1, 0);

    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .end(en)
        .build();

    assertEquals(LocalTime.of(0, 0), event.getStart().toLocalTime());
    assertEquals(LocalTime.of(1, 0), event.getEnd().toLocalTime());
  }

  /**
   * Tests event ending at 23:59.
   */
  @Test
  public void eventEndingAtLastMinute() {
    String subj = "Late Night Event";
    LocalDateTime st = LocalDateTime.of(2025, 5, 5, 22, 0);
    LocalDateTime en = LocalDateTime.of(2025, 5, 5, 23, 59);

    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .end(en)
        .build();

    assertEquals(LocalTime.of(22, 0), event.getStart().toLocalTime());
    assertEquals(LocalTime.of(23, 59), event.getEnd().toLocalTime());
  }

  /**
   * Tests event spanning multiple weeks (month-long event).
   */
  @Test
  public void longDurationEvent() {
    String subj = "Month-long Conference";
    LocalDateTime st = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime en = LocalDateTime.of(2025, 1, 31, 17, 0);

    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .end(en)
        .build();

    assertEquals(subj, event.getSubject());
    assertEquals(LocalDate.of(2025, 1, 1), event.getStart().toLocalDate());
    assertEquals(LocalDate.of(2025, 1, 31), event.getEnd().toLocalDate());
  }

  /**
   * Tests that getBuilder creates new builder instance each time.
   */
  @Test
  public void getBuilderCreatesNewInstance() {
    Event.EventBuilder builder1 = Event.getBuilder();
    Event.EventBuilder builder2 = Event.getBuilder();

    assertNotEquals(builder1, builder2);
  }

  /**
   * Tests that Event is immutable to input LocalDateTime changes.
   */
  @Test
  public void eventIsImmutableToInputChanges() {
    String subj = "Meeting";
    LocalDateTime st = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime en = LocalDateTime.of(2025, 5, 5, 11, 0);

    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .end(en)
        .build();

    LocalDateTime originalStart = event.getStart();

    st = st.plusHours(5);
    en = en.plusHours(5);

    LocalDateTime originalEnd = event.getEnd();
    assertEquals(originalStart, event.getStart());
    assertEquals(originalEnd, event.getEnd());
    assertEquals(LocalTime.of(10, 0), event.getStart().toLocalTime());
    assertEquals(LocalTime.of(11, 0), event.getEnd().toLocalTime());
  }

  /**
   * Tests event spanning exactly 24 hours.
   */
  @Test
  public void eventSpanningExactly24Hours() {
    String subj = "24 Hour Hackathon";
    LocalDateTime st = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime en = LocalDateTime.of(2025, 5, 6, 9, 0);

    Event event = Event.getBuilder()
        .subject(subj)
        .start(st)
        .end(en)
        .build();

    assertEquals(LocalDate.of(2025, 5, 5), event.getStart().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 6), event.getEnd().toLocalDate());
    assertEquals(LocalTime.of(9, 0), event.getStart().toLocalTime());
    assertEquals(LocalTime.of(9, 0), event.getEnd().toLocalTime());
  }

  @Test
  public void testEventEqualsReflexive() {
    Event event = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    assertEquals(event, event);
  }

  /**
   * Tests that two events with same subject, start, and end are equal.
   */
  @Test
  public void testEventEqualsSymmetric() {
    Event event1 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    Event event2 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    assertEquals(event1, event2);
    assertEquals(event2, event1); // Symmetric
  }

  /**
   * Tests transitivity of equals (if a=b and b=c, then a=c).
   */
  @Test
  public void testEventEqualsTransitive() {
    Event event1 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    Event event2 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    Event event3 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    assertEquals(event1, event2);
    assertEquals(event2, event3);
    assertEquals(event1, event3); // Transitive
  }

  /**
   * Tests that event is not equal to null.
   */
  @Test
  public void testEventNotEqualsNull() {
    Event event = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    assertNotEquals(event, null);
  }

  /**
   * Tests that event is not equal to object of different class.
   */
  @Test
  public void testEventNotEqualsDifferentClass() {
    Event event = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    assertNotEquals(event, "Meeting");
    assertNotEquals(event, new Object());
  }

  @Test
  public void testEventDifferentEndTime() {
    Event event1 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    Event event2 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 12, 0))
        .build();

    assertNotEquals(event1, event2);
  }

  @Test
  public void testEventDifferentStartTime() {
    Event event1 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 10, 0))
        .end(LocalDateTime.of(2025, 3, 5, 11, 0))
        .build();

    Event event2 = Event.getBuilder()
        .subject("Meeting")
        .start(LocalDateTime.of(2025, 3, 5, 14, 0))
        .end(LocalDateTime.of(2025, 3, 5, 16, 0))
        .build();

    assertNotEquals(event1, event2);
  }

  @Test
  public void testEventKeyEqualsReflexive() {
    EventKey key = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    assertEquals(key, key);
  }

  /**
   * Tests that two EventKeys with same values are equal (symmetric).
   */
  @Test
  public void testEventKeyEqualsSymmetric() {
    EventKey key1 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    EventKey key2 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    assertEquals(key1, key2);
    assertEquals(key2, key1);
  }

  /**
   * Tests transitivity of EventKey equals.
   */
  @Test
  public void testEventKeyEqualsTransitive() {
    EventKey key1 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    EventKey key2 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    EventKey key3 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    assertEquals(key1, key2);
    assertEquals(key2, key3);
    assertEquals(key1, key3);
  }

  /**
   * Tests that EventKey is not equal to null.
   */
  @Test
  public void testEventKeyNotEqualsNull() {
    EventKey key = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    assertNotEquals(key, null);
  }

  /**
   * Tests that EventKey is not equal to different class.
   */
  @Test
  public void testEventKeyNotEqualsDifferentClass() {
    EventKey key = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    assertNotEquals(key, "Meeting");
    assertNotEquals(key, new Object());
  }

  /**
   * Tests that EventKeys with different subjects are not equal.
   */
  @Test
  public void testEventKeyDifferentSubject() {
    EventKey key1 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    EventKey key2 = new EventKey("Conference",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    assertNotEquals(key1, key2);
  }

  /**
   * Tests that EventKeys with different start times are not equal.
   */
  @Test
  public void testEventKeyDifferentStartTime() {
    EventKey key1 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    EventKey key2 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 14, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    assertNotEquals(key1, key2);
  }

  /**
   * Tests that EventKeys with different end times are not equal.
   */
  @Test
  public void testEventKeyDifferentEndTime() {
    EventKey key1 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 11, 0));

    EventKey key2 = new EventKey("Meeting",
        LocalDateTime.of(2025, 3, 5, 10, 0),
        LocalDateTime.of(2025, 3, 5, 12, 0));

    assertNotEquals(key1, key2);
  }


}