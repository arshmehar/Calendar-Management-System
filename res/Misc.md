# Project Design Changes Summary

This document outlines the major design and structural changes implemented to transition the Calendar application from a purely text-based system to one that supports a Graphical User Interface (GUI) using Swing.

## 1. Controller Enhancements for GUI Support

### Unified Controller (`CalendarController.java`)
* **Dual-Mode Support:** The controller was refactored to handle both `GUI` and `TEXT` (headless/interactive) modes within a single class.
* **`CalendarFeatures` Interface Implementation:** The controller now implements `CalendarFeatures`, an interface that defines all user-triggered actions available in the GUI (e.g., creating events, editing calendars, copying events).
* **GUI Initialization:** Added a `startGuiMode()` method that initializes the `CalendarGuiView`, links it to the controller via `setFeatures(this)`, and sets up the initial state (default calendar, event lists).
* **GUI Command Execution:** 
    1.  Building a `ParsedCommand` object from GUI inputs.
    2.  Executing the command via the `CommandExecutor`.
    3.  Refreshing the UI (e.g., updating the event list).
    4.  Handling errors via popup messages.

## 2. GUI Implementation (View Layer)

### Main View (`CalendarGuiView.java`)
* **Swing-Based UI:** A complete GUI implementation using `JFrame`, `JPanel`, and `GridBagLayout`/`BorderLayout`.
* **Interface-Based Design:** Implements `ICalendarGuiView` to decouple the specific Swing implementation from the controller.
* **Interactive Components:**
    * **Calendar Grid:** A dynamic 7-column grid representing the days of the month.
    * **Event List:** A scrollable panel (`eventContainer`) that displays detailed `EventPanel` objects for the selected date.
    * **Control Panel:** Buttons for creating events, editing calendars, exporting data, and navigating months.
* **Dialogs:** Uses custom `JDialog` classes for complex inputs:
    * `EventCreationDialog`: For creating single and recurring events.
    * `EditEventDialog`: For editing existing event properties.
    * `CopyEventDialog`: For copying events to different dates or calendars.

### Event Visualization (`EventPanel.java`)
* **Custom Component:** A dedicated panel to render a single event's details (Subject, Time, Location, Status).
* **Multi-Day Support:** Logic added to display full date ranges (e.g., "10 Nov, 8:00 AM - 11 Nov, 9:00 AM") if an event spans multiple days.
* **Interactive Actions:** Each panel includes "Edit" and "Copy" buttons that trigger specific dialogs and controller actions.

## 3. Adapter Pattern for View Compatibility

### `GuiViewAdapter.java` 
*  Created to bridge the gap between the Swing-based GUI and the existing `CommandExecutor`.
* Adapts the Swing-based CalendarGuiView to the IcalendarView interface
* expected by the CommandExecutor.
* This allows the CommandExecutor, originally designed for text input/output, to trigger GUI-specific display methods (like showing popups or refreshing panels).


## 4. Logic Improvements

### Command Execution Strategy (`CommandExecutor.java`)
* **Refactoring:** Replaced a large, complex `switch` statement with a `Map<CommandType, Consumer<ParsedCommand>>`.
* **Benefit:** Improved readability, maintainability, and extensibility of the command dispatch logic.