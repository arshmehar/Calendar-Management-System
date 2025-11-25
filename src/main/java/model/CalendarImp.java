package model;

import controller.DateTimeParsing;
import controller.DayOfWeekAlphabet;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the ICalendar interface that manages events and event series.
 * Uses multiple internal indexes for efficient event lookups and queries where:
 * Events are indexed by unique key (subject, start, end).
 * Events are indexed by subject and start time for series operations.
 * Events are indexed by series ID for tracking recurring events.
 * Events are indexed by date for date-based queries.
 * no two events can have the same subject, start time, and end time.
 */
public class CalendarImp implements Icalendar {

  private String name;
  private ZoneId timezone;

  private final Map<EventKey, Event> eventsByKey;
  private final Map<String, Set<EventKey>> eventsBySubjectStart;
  private final Map<String, List<EventKey>> eventsBySeries;
  private final Map<LocalDate, Set<EventKey>> eventsByDate;


  /**
   * Constructs an empty calendar with initialized internal indexes.
   * All index maps are created empty to store events.
   */
  public CalendarImp(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
    this.eventsByKey = new HashMap<>();
    this.eventsBySubjectStart = new HashMap<>();
    this.eventsBySeries = new HashMap<>();
    this.eventsByDate = new HashMap<>();
  }

  /**
   * Creates a composite key for identifying events by subject and start time.
   *
   * @param subject the event subject
   * @param start   the event start date and time
   * @return composite key string in format "subject|startDateTime"
   */
  private String makeSubjectStartKey(String subject, LocalDateTime start) {
    return subject.toLowerCase() + "|" + start.toString();
  }

  /**
   * Adds an event to all internal indexes for efficient lookups.
   *
   * @param e the event to add to indexes
   * @throws DuplicateEventException if event with same key already exists
   */
  protected void addToIndexes(Event e) throws DuplicateEventException {
    EventKey key = new EventKey(e.getSubject(), e.getStart(), e.getEnd());
    String subjectStartKey = makeSubjectStartKey(e.getSubject(), e.getStart());

    if (eventsByKey.containsKey(key)) {
      throw new DuplicateEventException(
          "Event already exists: " + e.getSubject() + " at " + e.getStart());
    }

    eventsByKey.put(key, e);
    eventsBySubjectStart.computeIfAbsent(subjectStartKey, k -> new HashSet<>()).add(key);

    if (e.getSeriesId() != null) {
      eventsBySeries.computeIfAbsent(e.getSeriesId(), k -> new ArrayList<>()).add(key);

    }

    LocalDate startDate = e.getStart().toLocalDate();
    eventsByDate.computeIfAbsent(startDate, d -> new HashSet<>()).add(key);
    LocalDate endDate = e.getEnd().toLocalDate();
    LocalDate date = startDate.plusDays(1);
    while (!date.isAfter(endDate)) {
      eventsByDate.computeIfAbsent(date, d -> new HashSet<>()).add(key);
      date = date.plusDays(1);
    }

  }

  /**
   * Removes an event from all internal indexes.
   *
   * @param e the event to remove from indexes
   */
  protected void removeFromIndexes(Event e) {
    EventKey key = new EventKey(e.getSubject(), e.getStart(), e.getEnd());
    String subjectStartKey = makeSubjectStartKey(e.getSubject(), e.getStart());

    eventsByKey.remove(key);

    Set<EventKey> subjectSet = eventsBySubjectStart.get(subjectStartKey);
    if (subjectSet != null) {
      subjectSet.remove(key);
      if (subjectSet.isEmpty()) {
        eventsBySubjectStart.remove(subjectStartKey);
      }
    }

    if (e.getSeriesId() != null) {
      List<EventKey> seriesEvents = eventsBySeries.get(e.getSeriesId());
      if (seriesEvents != null) {
        seriesEvents.remove(key);
        if (seriesEvents.isEmpty()) {
          eventsBySeries.remove(e.getSeriesId());
        }
      }
    }

    LocalDate startDate = e.getStart().toLocalDate();
    LocalDate endDate = e.getEnd().toLocalDate();
    LocalDate date = startDate;

    while (!date.isAfter(endDate)) {
      Set<EventKey> set = eventsByDate.get(date);
      if (set != null) {
        set.remove(key);
        if (set.isEmpty()) {
          eventsByDate.remove(date);
        }
      }
      date = date.plusDays(1);
    }
  }

  /**
   * Helper method to update the indexes.
   *
   * @param oldEvent old event to be removed.
   * @param newEvent to be added.
   * @throws DuplicateEventException duplicate event exception thrown if duplicate.
   */
  private void updateIndexes(Event oldEvent, Event newEvent) throws DuplicateEventException {
    removeFromIndexes(oldEvent);
    addToIndexes(newEvent);
  }

  /**
   * Implementation of adding the passed event to this calendar.
   *
   * @param event passed event object.
   * @param start start date time.
   * @param end   end date time.
   */
  void copyEventToCalendar(Event event, LocalDateTime start, LocalDateTime end) {

    Event copiedEvent = Event.getBuilderFrom(event)
        .start(start)
        .end(end)
        .build();

    addToIndexes(copiedEvent);

  }


  /**
   * Adds a single event to the calendar.
   *
   * @param subject string with subject name
   * @param start   of type LocalDateTime start of event
   * @param end     of type LocalDateTime end of event
   * @throws DuplicateEventException if an event with the same subject,
   *                                 start time, and end time already exists
   */
  @Override
  public void addEvent(String subject, LocalDateTime start, LocalDateTime end)
      throws DuplicateEventException {
    Event event = Event.getBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .build();

    addToIndexes(event);

  }


  /**
   * Adds an event series to the calendar. All events generated by the
   * series are added individually.
   *
   * @param subject     the subject of event
   * @param startTime   start time
   * @param endTime     end time of event
   * @param startDate   date on which event starts
   * @param weekdays    days of the week (monday tuesday... sunday)
   * @param occurrences no. of time an event has to recur
   * @throws DuplicateEventException  if any generated event conflicts
   *                                  with an existing event
   * @throws IllegalArgumentException if series is null
   */
  private void addEventSeries(String subject, LocalTime startTime,
                              LocalTime endTime, LocalDate startDate,
                              Set<DayOfWeekAlphabet> weekdays, Integer occurrences,
                              LocalDate endDate) throws DuplicateEventException {

    Set<DayOfWeek> weekdaysSet =
        weekdays.stream().map(DayOfWeekAlphabet::toJavaDay).collect(Collectors.toSet());

    EventSeries series = EventSeries.getBuilder()
        .subject(subject)
        .startTime(startTime)
        .endTime(endTime)
        .startDate(startDate)
        .days(weekdaysSet)
        .occurrences(occurrences)
        .endDate(endDate)
        .build();

    List<Event> generatedEvents = series.generateEvents();

    for (Event event : generatedEvents) {

      addToIndexes(event);

    }

  }

  /**
   * Adds an event series to the calendar. All events generated by the
   * series are added individually.
   *
   * @param subject       the subject of event
   * @param startDateTime start date and time
   * @param endDateTime   end date and time of event
   * @param occurrences   no. of time an event has to recur
   * @param weekdays      days of the week (monday tuesday... sunday)
   * @throws DuplicateEventException  if any generated event conflicts
   *                                  with an existing event
   * @throws IllegalArgumentException if series is null
   */
  @Override
  public void addEventSeriesOccurrences(String subject, LocalDateTime startDateTime,
                                        LocalDateTime endDateTime, int occurrences,
                                        Set<DayOfWeekAlphabet> weekdays)
      throws DuplicateEventException {


    LocalTime startTime = startDateTime.toLocalTime();
    LocalDate startDate = startDateTime.toLocalDate();
    LocalTime endTime = endDateTime.toLocalTime();
    addEventSeries(subject, startTime, endTime, startDate, weekdays, occurrences, null);
  }


  /**
   * Adds an event series to the calendar. All events generated by the
   * series are added individually.
   *
   * @param subject       the subject of event
   * @param startDateTime start date and time
   * @param endDateTime   end date and time of event in the series
   * @param endDate       ending date of the series
   * @param weekdays      days of the week (monday tuesday... sunday)
   * @throws DuplicateEventException  if any generated event conflicts
   *                                  with an existing event
   * @throws IllegalArgumentException if series is null
   */
  @Override
  public void addEventSeriesUntil(String subject, LocalDateTime startDateTime,
                                  LocalDateTime endDateTime, LocalDate endDate,
                                  Set<DayOfWeekAlphabet> weekdays) {
    LocalTime startTime = startDateTime.toLocalTime();
    LocalDate startDate = startDateTime.toLocalDate();
    LocalTime endTime = endDateTime.toLocalTime();

    addEventSeries(subject, startTime, endTime, startDate, weekdays, null, endDate);

  }


  /**
   * Edits a single event by replacing it with an updated version.
   * This is for editing a single instance only, even if part of a series.
   *
   * @param subject  subject name of the original event
   * @param start    start date and time
   * @param end      end date and time
   * @param property the property which needs to be edited
   * @param newValue replaced with this new property
   * @throws EventNotFoundException  if original event doesn't exist
   * @throws DuplicateEventException if updated event conflicts with
   *                                 another existing event
   */
  @Override
  public void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                        String property, String newValue)
      throws EventNotFoundException, DuplicateEventException {


    EventKey originalKey = new EventKey(subject, start, end);
    Event original = eventsByKey.get(originalKey);


    if (original == null) {
      throw new EventNotFoundException("Event not found ");
    }

    String oldSeriesId = original.getSeriesId();
    String newSeriesId;

    newSeriesId = property.equals("start") ? null : oldSeriesId;

    Event updated = updateEvent(original, property, newValue, newSeriesId);
    removeFromIndexes(original);

    EventKey key = new EventKey(updated.getSubject(), updated.getStart(), updated.getEnd());

    if (eventsByKey.containsKey(key)) {
      addToIndexes(original);
      throw new DuplicateEventException(
          "Event created as a result of editing already existed previously.");
    } else {
      addToIndexes(updated);
    }

  }

  /**
   * Edits events in a series starting from a specific date/time.
   * Finds the series containing the specified event and modifies all
   * events in that series.
   *
   * @param subject  the subject of an event in the series
   * @param start    the date/time to start editing from (inclusive)
   * @param property the property to edit (subject, start, end, etc.)
   * @param newValue the new value for the property
   * @throws EventNotFoundException   if no matching event found
   * @throws IllegalArgumentException if property or value is invalid
   */

  @Override
  public void editAllInSeries(String subject, LocalDateTime start, String property,
                              String newValue)
      throws EventNotFoundException, DuplicateEventException {
    editMultipleEvents(subject, start, property, newValue, true);

  }


  /**
   * Edits multiple events matching the given subject and start time.
   * Handles both single events and events in series based on their series ID.
   *
   * @param subject         the subject of events to edit
   * @param start           the start datetime to match
   * @param property        the property to edit
   * @param newValue        the new value for the property
   * @param isSeriesEditing true to edit all in series, false to edit from date forward
   * @throws EventNotFoundException  if no matching events found
   * @throws DuplicateEventException if editing creates duplicate event
   */

  private void editMultipleEvents(String subject, LocalDateTime start, String property,
                                  String newValue, boolean isSeriesEditing)
      throws EventNotFoundException, DuplicateEventException {

    Set<EventKey> matchedEvents = eventsBySubjectStart.get(makeSubjectStartKey(subject, start));

    if (matchedEvents == null || matchedEvents.isEmpty()) {
      throw new EventNotFoundException(
          "No event(s) found with subject '" + subject + "' starting at " + start);
    }

    Map<String, List<Event>> eventsBySeriesId = new HashMap<>();

    for (EventKey ek : matchedEvents) {
      Event e = eventsByKey.get(ek);
      String seriesId = e.getSeriesId();
      eventsBySeriesId.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(e);
    }

    for (Map.Entry<String, List<Event>> entry : eventsBySeriesId.entrySet()) {
      String seriesId = entry.getKey();
      List<Event> events = entry.getValue();

      if (seriesId == null) {
        editSingleEvents(events, property, newValue);
      } else {
        editSeriesBulk(seriesId, start, property, newValue, isSeriesEditing);
      }
    }
  }


  /**
   * Edits events in a series starting from a specific date/time.
   * Modifies all events in the series from the specified date onward.
   *
   * @param subject  the subject of events in the series
   * @param start    the date/time to start editing from (inclusive)
   * @param property the property to edit
   * @param newValue the new value for the property
   * @throws EventNotFoundException  if no matching events found
   * @throws DuplicateEventException if editing creates duplicate event
   */
  public void editEventsFromDate(String subject, LocalDateTime start, String property,
                                 String newValue)
      throws EventNotFoundException, DuplicateEventException {

    editMultipleEvents(subject, start, property, newValue, false);
  }

  /**
   * Edit multiple single events (not part of any series).
   */
  private void editSingleEvents(List<Event> events, String property, String newValue)
      throws DuplicateEventException {

    for (Event e : events) {
      Event edited = updateEvent(e, property, newValue, null);
      updateIndexes(e, edited);
    }
  }

  /**
   * Edit all events in a series.
   */
  private void editSeriesBulk(String seriesId, LocalDateTime start,
                              String property, String newValue, boolean series)
      throws DuplicateEventException {

    List<EventKey> seriesEvents = eventsBySeries.get(seriesId);

    Event lastEvent = seriesEvents.stream()
        .map(eventsByKey::get)
        .max(Comparator.comparing(Event::getStart))
        .orElse(null);


    String newSeriesId = (property.equalsIgnoreCase("start") || property.equalsIgnoreCase("end"))
        ? UUID.randomUUID().toString() : seriesId;

    List<EventKey> snapshot = new ArrayList<>(seriesEvents);
    List<EventKey> toEdit = new ArrayList<>();
    List<EventKey> toKeep = new ArrayList<>();

    if (series) {
      toEdit.addAll(snapshot);
    } else {
      for (EventKey ek : snapshot) {

        Event e = eventsByKey.get(ek);
        if (!e.getStart().isBefore(start)) {
          toEdit.add(ek);
        } else {
          toKeep.add(ek);
        }
      }
    }

    List<Event> updatedEvents = new ArrayList<>();

    Duration shiftAmount = null;
    if (property.equalsIgnoreCase("start")) {
      LocalDateTime newStart = DateTimeParsing.parseDateTime(newValue);
      shiftAmount = Duration.between(start, newStart);
    } else if (property.equalsIgnoreCase("end")) {
      LocalDateTime newEnd = DateTimeParsing.parseDateTime(newValue);
      shiftAmount = Duration.between(lastEvent.getEnd(), newEnd);
    }

    for (EventKey ek : toEdit) {
      Event e = eventsByKey.get(ek);
      Event updated;


      if ((property.equalsIgnoreCase("start") || property.equalsIgnoreCase("end"))
          && shiftAmount != null) {
        LocalDateTime shiftedStart = e.getStart().plus(shiftAmount);
        LocalDateTime shiftedEnd = e.getEnd().plus(shiftAmount);

        updated = Event.getBuilderFrom(e)
            .start(shiftedStart)
            .end(shiftedEnd)
            .seriesId(newSeriesId)
            .build();
      } else {
        updated = updateEvent(e, property, newValue, newSeriesId);
      }

      EventKey key = new EventKey(updated.getSubject(), updated.getStart(), updated.getEnd());
      if (eventsByKey.containsKey(key) && eventsByKey.get(key) != e) {
        throw new DuplicateEventException(
            "Event created as a result of editing already existed previously.");
      }

      updatedEvents.add(updated);
    }

    for (EventKey ek : toEdit) {
      removeFromIndexes(eventsByKey.get(ek));
    }

    for (Event updated : updatedEvents) {
      addToIndexes(updated);
    }

    if (property.equals("start")) {
      if (toKeep.isEmpty()) {
        eventsBySeries.remove(seriesId);
      } else {
        eventsBySeries.put(seriesId, toKeep);
      }
    }
  }


  /**
   * Gets all events scheduled on a specific date.
   * Returns events that start on, end on, or span across the given date.
   *
   * @param date the date to query
   * @return list of events on that date (maybe empty, never null)
   */
  @Override
  public List<Event> getEventsOn(LocalDate date) {
    Set<EventKey> eventKeys = eventsByDate.get(date);
    if (eventKeys == null || eventKeys.isEmpty()) {
      return Collections.emptyList();
    }

    Set<Event> events = eventKeys.stream().map(eventsByKey::get).collect(Collectors.toSet());
    return new ArrayList<>(events);
  }


  /**
   * Gets all events that partly or completely lie within a time range.
   *
   * @param start the start of the time range
   * @param end   the end of the time range
   * @return list of events in the range (maybe empty, never null)
   */
  @Override
  public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    List<Event> result = new ArrayList<>();
    LocalDate current = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();

    while (!current.isAfter(endDate)) {
      Set<EventKey> dayEventsKeys = eventsByDate.get(current);
      if (dayEventsKeys != null) {

        Set<Event> dayEvents =
            dayEventsKeys.stream().map(eventsByKey::get).collect(Collectors.toSet());
        result.addAll(dayEvents);
      }
      current = current.plusDays(1);
    }
    return result;
  }

  /**
   * Checks if the user is busy at a specific date and time.
   * Returns true if there is at least one event scheduled at that time.
   * [start, end) - inclusive start, exclusive end.
   *
   * @param dateTime the date and time to check
   * @return true if busy, false if available
   */
  @Override
  public boolean isOccupied(LocalDateTime dateTime) {
    Set<EventKey> eventKeys = eventsByDate.get(dateTime.toLocalDate());

    if (eventKeys != null) {
      Set<Event> events = eventKeys.stream().map(eventsByKey::get).collect(Collectors.toSet());

      for (Event e : events) {
        if (!dateTime.isBefore(e.getStart()) && !dateTime.isAfter(e.getEnd())) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Gets all events in the calendar across all dates.
   *
   * @return set of all events in the calendar
   */
  public Set<Event> getAllEvents() {

    return new HashSet<>(eventsByKey.values());

  }

  /**
   * Getter for Calendar Name.
   *
   * @return name of calendar.
   */
  public String getCalendarName() {
    return name;
  }

  /**
   * getter for Timezone of this calendar.
   *
   * @return the calendar's timezone.
   */
  @Override
  public ZoneId getCalendarTimeZone() {
    return timezone;
  }

  /**
   * Finds all events with the subject and start time.
   * This is a protected method used internally for event operations like copying.
   *
   * @param subject the subject of the events to find
   * @param start   the start date and time to match
   * @return list of events matching the subject and start time
   * @throws EventNotFoundException if no events match the criteria
   */
  protected List<Event> findEventsBySubjectStart(String subject, LocalDateTime start)
      throws EventNotFoundException {
    Set<EventKey> matchedEvents = eventsBySubjectStart.get(makeSubjectStartKey(subject, start));

    if (matchedEvents == null || matchedEvents.isEmpty()) {
      throw new EventNotFoundException(
          "No event(s) found with subject '" + subject + "' starting at " + start);
    }
    return matchedEvents.stream()
        .map(eventsByKey::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

  }

  /**
   * Sets the calendar name.
   * used by the calendar system when renaming calendars.
   *
   * @param newName the new name for this calendar
   */
  protected void setCalendarName(String newName) {
    this.name = newName;
  }

  /**
   * Sets the calendar timezone.
   * used by the calendar system when changing timezones.
   * Does not modify existing event times, only updates the calendar's timezone setting.
   *
   * @param timezone the new timezone for this calendar
   */
  protected void setCalendarTimezone(ZoneId timezone) {
    this.timezone = timezone;
  }

  /**
   * Helper method to build an updated event with one property changed.
   *
   * @param original the original event
   * @param property the property to change
   * @param newValue the new value as a string
   * @return a new Event with the specified property changed
   * @throws IllegalArgumentException if property is invalid
   */
  private Event updateEvent(Event original, String property, String newValue, String id)
      throws IllegalArgumentException {

    Event.EventBuilder builder = Event.getBuilder()
        .subject(original.getSubject())
        .start(original.getStart())
        .end(original.getEnd())
        .description(original.getDescription())
        .location(original.getLocation())
        .status(original.getStatus());

    switch (property.toLowerCase()) {
      case "subject":
        builder.subject(newValue);
        break;
      case "start":
        LocalDateTime startTime = DateTimeParsing.parseDateTime(newValue);
        builder.start(startTime);
        break;
      case "end":
        LocalDateTime endTime = DateTimeParsing.parseDateTime(newValue);
        builder.end(endTime);
        break;
      case "description":
        builder.description(newValue);
        break;
      case "location":
        builder.location(newValue);
        break;
      case "status":
        builder.status(EventStatus.valueOf(newValue.toUpperCase()));
        break;

      default:
        throw new IllegalArgumentException("Property to update not found.");
    }
    builder.seriesId(id);
    return builder.build();
  }


}

