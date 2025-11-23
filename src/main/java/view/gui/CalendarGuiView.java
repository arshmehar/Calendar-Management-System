// view/gui/CalendarGuiView.java
package view.gui;

import controller.features.CalendarFeatures;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import model.Event;

/**
 * Simple GUI view - only displays and notifies listeners.
 * Does NOT call controller or model directly.
 */
public class CalendarGuiView implements IcalendarGuiView{
  private final JFrame frame;
  private CalendarFeatures features;

  // UI Components
  private JLabel monthLabel;
  private JPanel calendarGrid;
  private JTextArea eventDisplay;
  private JComboBox<String> calendarSelector;
  private JLabel timezoneLabel;

  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private JPanel eventContainer;

  public CalendarGuiView() {
    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();

    frame = new JFrame("Calendar Application");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(900, 700);
    frame.setLocationRelativeTo(null);

    setupUI();
  }

  public void setFeatures(CalendarFeatures features) {
    this.features = features;
  }

  private void setupUI() {
    frame.setLayout(new BorderLayout(10, 10));

    // Top panel
    frame.add(createTopPanel(), BorderLayout.NORTH);

    // Center - calendar grid
    // Use 0 rows to allow dynamic addition, fixed 7 columns
    calendarGrid = new JPanel(new GridLayout(0, 7, 5, 5));
    calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    frame.add(calendarGrid, BorderLayout.CENTER);

    // Right panel
    frame.add(createRightPanel(), BorderLayout.EAST);

    updateMonthLabel();
    buildCalendar();
  }

  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());

    // Left - calendar selection and actions
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    leftPanel.add(new JLabel("Calendar:"));
    calendarSelector = new JComboBox<>();
    calendarSelector.addActionListener(e -> {
      if (features != null && calendarSelector.getSelectedItem() != null) {
        String selected = (String) calendarSelector.getSelectedItem();
        features.selectCalendar(selected);
      }
    });
    leftPanel.add(calendarSelector);

    // *** NEW BUTTON: EDIT CALENDAR ***
    JButton editCalButton = new JButton("Edit Calendar");
    editCalButton.addActionListener(e -> showEditCalendarDialog());
    leftPanel.add(editCalButton);
    // *********************************

    JButton newCalButton = new JButton("New Calendar");
    newCalButton.addActionListener(e -> showCreateCalendarDialog());
    leftPanel.add(newCalButton);

    topPanel.add(leftPanel, BorderLayout.WEST);

    // ... rest of createTopPanel remains the same ...
    // Center - navigation
    JPanel navPanel = new JPanel(new FlowLayout());

    JButton prevButton = new JButton("< Prev");
    prevButton.addActionListener(e -> changeMonth(-1));
    navPanel.add(prevButton);

    monthLabel = new JLabel();
    monthLabel.setFont(new Font("Arial", Font.BOLD, 16));
    navPanel.add(monthLabel);

    JButton nextButton = new JButton("Next >");
    nextButton.addActionListener(e -> changeMonth(1));
    navPanel.add(nextButton);

    JButton todayButton = new JButton("Today");
    todayButton.addActionListener(e -> goToToday());
    navPanel.add(todayButton);

    topPanel.add(navPanel, BorderLayout.CENTER);

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

    // ----------------------------------------------------------------
    // CHANGE: Initialize with an empty string, as the controller
    //         will set the actual value shortly after initialization.
    timezoneLabel = new JLabel("");
    // ----------------------------------------------------------------

    timezoneLabel.setFont(new Font("Arial", Font.ITALIC, 11));
    timezoneLabel.setForeground(Color.GRAY);

    bottomPanel.add(timezoneLabel);
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

    topPanel.add(bottomPanel, BorderLayout.SOUTH);

    return topPanel;
  }

  private JPanel createRightPanel() {
    JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
    rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    rightPanel.setPreferredSize(new Dimension(350, 0));

    // Header
    JLabel eventLabel = new JLabel("Events:");
    eventLabel.setFont(new Font("Arial", Font.BOLD, 14));
    rightPanel.add(eventLabel, BorderLayout.NORTH);

    // Scrollable event container
    eventContainer = new JPanel();
    eventContainer.setLayout(new BoxLayout(eventContainer, BoxLayout.Y_AXIS));
    eventContainer.setBackground(Color.WHITE);

    JScrollPane scrollPane = new JScrollPane(eventContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    rightPanel.add(scrollPane, BorderLayout.CENTER);

    // Action buttons
    JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

    JButton createEventBtn = new JButton("Create Event");
    createEventBtn.addActionListener(e -> showCreateEventDialog());
    buttonPanel.add(createEventBtn);

    JButton createRecurringBtn = new JButton("Create Recurring");
    createRecurringBtn.addActionListener(e -> showCreateRecurringDialog());
    buttonPanel.add(createRecurringBtn);

    JButton exportBtn = new JButton("Export Calendar");
    exportBtn.addActionListener(e -> showExportDialog());
    buttonPanel.add(exportBtn);

    rightPanel.add(buttonPanel, BorderLayout.SOUTH);

    return rightPanel;
  }

  private void buildCalendar() {
    calendarGrid.removeAll();

    // Day headers - Mon to Sun
    String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (String day : days) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 12));
      calendarGrid.add(label);
    }

    LocalDate firstDay = currentMonth.atDay(1);
    // Adjust start day to match Mon-Sun (Mon=1 becomes 0, Sun=7 becomes 6)
    int startDayOfWeek = firstDay.getDayOfWeek().getValue() - 1;

    // Add empty slots before start of month
    for (int i = 0; i < startDayOfWeek; i++) {
      calendarGrid.add(new JLabel(""));
    }

    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton dayButton = new JButton(String.valueOf(day));

      // Allow custom styling to show through
      dayButton.setOpaque(true);
      dayButton.setBorderPainted(true);
      dayButton.setFocusPainted(false);

      // DON'T set background to white - let it use default
      // Only set background for special cases

      boolean isToday = date.equals(LocalDate.now());
      boolean isSelected = date.equals(selectedDate);

      // Today: yellow background
      if (isToday) {
        dayButton.setBackground(Color.PINK);
      }

      // Selected: RED border + bold
      if (isSelected) {
        dayButton.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
        dayButton.setFont(new Font("Arial", Font.BOLD, 14));
      }

      dayButton.addActionListener(e -> selectDate(date));
      calendarGrid.add(dayButton);
    }



    // Add trailing empty slots to maintain fixed grid size (6 rows * 7 cols = 42 cells)
    int cellsFilled = startDayOfWeek + daysInMonth;
    int totalCells = 42;
    for (int i = cellsFilled; i < totalCells; i++) {
      calendarGrid.add(new JLabel(""));
    }

    calendarGrid.revalidate();
    calendarGrid.repaint();
  }

  private void selectDate(LocalDate date) {
    selectedDate = date;
    buildCalendar();
    if (features != null) {
      features.viewEventsOnDate(date);
    }
  }

  private void changeMonth(int delta) {
    currentMonth = currentMonth.plusMonths(delta);
    updateMonthLabel();
    buildCalendar();
  }

  private void goToToday() {
    currentMonth = YearMonth.now();
    selectedDate = LocalDate.now();
    updateMonthLabel();
    buildCalendar();
    if (features != null) {
      features.viewEventsOnDate(selectedDate);
    }
  }

  private void updateMonthLabel() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    monthLabel.setText(currentMonth.format(formatter));
  }

  // ==================== DIALOG METHODS ====================

  private void showCreateCalendarDialog() {
    if (features == null) return;

    String name = JOptionPane.showInputDialog(frame, "Enter calendar name:");
    if (name == null || name.trim().isEmpty()) return;

    String[] timezones = {"America/New_York", "America/Los_Angeles", "America/Chicago",
        "Europe/London", "Europe/Paris", "Asia/Tokyo", "Asia/Kolkata"};
    String timezone = (String) JOptionPane.showInputDialog(frame,
        "Select timezone:", "Timezone", JOptionPane.QUESTION_MESSAGE,
        null, timezones, timezones[0]);

    if (timezone != null) {
      features.createCalendar(name.trim(), timezone);
    }
  }

  private void showCreateEventDialog() {
    if (features == null) return;

    EventCreationDialog dialog = new EventCreationDialog(frame, selectedDate, false);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      features.createSingleEvent(
          dialog.getSubject(),
          dialog.getStartDateTime(),
          dialog.getEndDateTime()
      );
    }
  }

  private void showCreateRecurringDialog() {
    if (features == null) return;

    EventCreationDialog dialog = new EventCreationDialog(frame, selectedDate, true);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      if (dialog.isOccurrencesBased()) {
        features.createRecurringEventWithOccurrences(
            dialog.getSubject(),
            dialog.getStartDateTime(),
            dialog.getEndDateTime(),
            dialog.getSelectedWeekdays(),
            dialog.getOccurrences()
        );
      } else {
        features.createRecurringEventUntilDate(
            dialog.getSubject(),
            dialog.getStartDateTime(),
            dialog.getEndDateTime(),
            dialog.getSelectedWeekdays(),
            dialog.getUntilDate()
        );
      }
    }
  }

  private void showEditEventDialog() {
    if (features == null) return;

    String eventName = JOptionPane.showInputDialog(frame,
        "Enter event name to edit:");
    if (eventName == null || eventName.trim().isEmpty()) return;

    String property = "subject";
    String newValue = JOptionPane.showInputDialog(frame,
        "Enter new subject:");
    if (newValue == null || newValue.trim().isEmpty()) return;

    // For simplicity, just edit single event
    // You can add radio buttons to choose edit scope
    features.editSingleEvent(
        eventName.trim(),
        selectedDate.atTime(10, 0), // Placeholder
        selectedDate.atTime(11, 0),
        property,
        newValue.trim()
    );
  }


  private void showExportDialog() {
    if (features == null) return;

    String[] options = {"CSV", "iCal"};
    int choice = JOptionPane.showOptionDialog(frame,
        "Select export format:",
        "Export Calendar",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);

    if (choice == 0 || choice == 1) {
      JFileChooser fileChooser = new JFileChooser();
      if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
        String filePath = fileChooser.getSelectedFile().getAbsolutePath();

        if (choice == 0) {
          features.exportToCSV(filePath);
        } else {
          features.exportToICal(filePath);
        }
      }
    }
  }

  // ==================== PUBLIC UPDATE METHODS ====================

  public void setCalendarNames(List<String> names) {
    calendarSelector.removeAllItems();
    for (String name : names) {
      calendarSelector.addItem(name);
    }
  }

  public void setSelectedCalendar(String name) {
    calendarSelector.setSelectedItem(name);
  }

  public void displayEvents(List<Event> events, LocalDate date) {
    eventContainer.removeAll();

    if (events == null || events.isEmpty()) {
      JLabel noEvents = new JLabel("No events on " + date);
      noEvents.setForeground(Color.GRAY);
      noEvents.setAlignmentX(Component.CENTER_ALIGNMENT);
      noEvents.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
      eventContainer.add(noEvents);
    } else {
      for (Event event : events) {
        EventPanel eventPanel = new EventPanel(event, date, features);
        eventContainer.add(eventPanel);
        eventContainer.add(Box.createVerticalStrut(5));
      }
    }

    eventContainer.revalidate();
    eventContainer.repaint();
  }

  public void showError(String message) {
    JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  public void showSuccess(String message) {
    JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
  }

  public void display() {
    frame.setVisible(true);
  }

  public JFrame getFrame() {
    return frame;
  }

  private void showEditCalendarDialog() {
    if (features == null) return;

    String currentCalendarName = (String) calendarSelector.getSelectedItem();
    if (currentCalendarName == null) {
      showError("Please create or select a calendar first.");
      return;
    }

    String[] options = {"Rename", "Change Timezone"};
    int choice = JOptionPane.showOptionDialog(
        frame,
        "What would you like to edit for the calendar '" + currentCalendarName + "'?",
        "Edit Calendar",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]
    );

    if (choice == 0) { // Rename
      String newName = JOptionPane.showInputDialog(frame,
          "Enter the new name for the calendar:", currentCalendarName);

      if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(currentCalendarName)) {
        features.editCalendarName(currentCalendarName, newName.trim());
        // Controller will handle success/error messages and list updates
      }
    } else if (choice == 1) { // Change Timezone
      String[] timezones = {"America/New_York", "Europe/London", "Asia/Tokyo", "Asia/Kolkata"};
      String newTimezone = (String) JOptionPane.showInputDialog(frame,
          "Select the new timezone:", "Change Timezone", JOptionPane.QUESTION_MESSAGE,
          null, timezones, timezones[0]);

      if (newTimezone != null) {
        features.editCalendarTimezone(currentCalendarName, newTimezone);
        // Controller will handle success/error messages
      }
    }
  }

  // view/gui/CalendarGuiView.java

// ... existing methods ...

  /**
   * Getter for the currently selected date in the view.
   * Required for the controller to refresh the event panel based on context.
   * @return the currently selected date.
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Updates the visible timezone label.
   */
  public void updateTimezoneDisplay(String timezoneId) {
    if (timezoneLabel != null) {
      timezoneLabel.setText("Current Timezone: " + timezoneId);
    }
  }

}