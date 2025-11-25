# Design Changes

## 1. Separation of I/O Responsibilities from View

### Previous Design
- View layer handled both input reading and output display
- Two separate view implementations:
  - `InteractiveView`: Read from command line using Scanner
  - `HeadlessView`: Read from file
- View was responsible for I/O operations in different modes

### Current Design
- View layer now only handles output display (single responsibility)
- Single `CalendarView` implementation for all display operations
- Controller delegates reading to mode-specific executors:
  - `InteractiveModeExecutor`: Reads from command line
  - `HeadlessModeExecutor`: Reads from file
- Mode executors implement `IModeExecutor` interface

### Benefits
- Simplified view - now only responsible for displaying information
- Better separation of concerns - I/O logic moved to appropriate layer
- Eliminated need for multiple view implementations
- More testable view component


### Simplified Controller Initialization
```java
// Before: Mode passed to controller constructor
IcalendarController controller = new CalendarController(model, view, mode);

// After: Mode handled externally through mode executors
IcalendarController controller = new CalendarController(model, view);
controller.run(mode, commandFilePath);
```

## 2. Removed File Export Responsibility from Model

### Previous Design
- Model layer directly handled CSV and iCal file generation
- Model performed file I/O operations
- Violated single responsibility principle

### Current Design
- Model only returns data (`Set<Event>`)
- Dedicated export classes handle file I/O:
  - `CsvExporter`: Handles CSV file generation
  - `IcalExporter`: Handles iCal file generation
- Controller coordinates between model and exporters

### Benefits
- Model no longer performs I/O operations
- Better separation of concerns
- Exporters are reusable and testable independently
- Model remains focused on business logic

## 3. Multi-Calendar Support through CalendarSystem

### Previous Design
- Model was a single `ICalendar` object
- All operations performed on one calendar instance
- No support for managing multiple calendars

### Current Design
- Model is now an `ICalendarSystem` object
- `CalendarSystem` manages multiple calendars
- Calendar storage structure:
```java
private final Map<String, CalendarImp> calendarsByName;
```

### New CalendarSystem Operations
- `createCalendar(String name, String timezone)`: Create new calendar with unique name and timezone
- `getCalendar(String name)`: Retrieve specific calendar
- `renameCalendar(String oldName, String newName)`: Rename existing calendar
- `changeTimezone(String calendarName, String newTimezone)`: Update calendar timezone
- `copyEvent(...)`: Copy single event between calendars with timezone conversion
- `copyEventsOn(...)`: Copy all events from specific date between calendars
- `copyEventsBetween(...)`: Copy events within date range between calendars

### CalendarImp Enhancements
- Added `timezone` field (ZoneId)
- Existing event management functionality preserved
- Each calendar maintains its own event storage

### Benefits
- Support for multiple independent calendars
- Timezone-aware operations
- Event copying across calendars with automatic timezone conversion
- Centralized calendar management

## 4. Optimized Event Storage with Event Keys

### Previous Design
Event objects duplicated across multiple index maps:
```java
private final Map<EventKey, Event> eventsByKey;
private final Map<String, Set<Event>> eventsBySubjectStart;
private final Map<String, List<Event>> eventsBySeries;
private final Map<LocalDate, Set<Event>> eventsByDate;
```

### Issues
- Event objects stored multiple times in different maps
- Memory inefficiency due to duplication
- Updates required synchronization across all maps containing the event

### Current Design
Event objects stored once; indexes use EventKey references:
```java
private final Map<EventKey, Event> eventsByKey;
private final Map<String, Set<EventKey>> eventsBySubjectStart;
private final Map<String, List<EventKey>> eventsBySeries;
private final Map<LocalDate, Set<EventKey>> eventsByDate;
```

### Benefits
- **Memory efficiency**: Single copy of each event object
- **Consistency**: Event modifications only need to update one location
- **Performance**: Reduced memory footprint
- **Maintainability**: Easier to manage single source of truth

### Usage Pattern
```java
// Index stores EventKey
Set<EventKey> keys = eventsBySubjectStart.get(searchKey);

// Retrieve actual Event when needed
for (EventKey key : keys) {
    Event event = eventsByKey.get(key);
    // Process event
}
```

## Summary

These design changes improve the application architecture by:

1. **Better separation of concerns**: I/O, business logic, and presentation are clearly separated
2. **Single Responsibility Principle**: Each component has one clear purpose
3. **Reduced duplication**: Event storage optimized to eliminate redundancy
4. **Enhanced functionality**: Multi-calendar support with timezone-aware operations
5. **Improved testability**: Components can be tested independently
6. **Scalability**: Architecture supports future enhancements without major refactoring