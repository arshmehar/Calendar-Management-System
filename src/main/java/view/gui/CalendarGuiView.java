package view.gui;

import controller.features.CalendarFeatures;
import model.Event;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CalendarGuiView {
  private final JFrame frame;
  private CalendarFeatures features;

  // UI Components
  private JLabel monthLabel;
  private JPanel calendarGrid;
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
    frame.setSize(1000, 750);
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

    JButton editCalButton = new JButton("Edit Calendar");
    editCalButton.addActionListener(e -> showEditCalendarDialog());
    leftPanel.add(editCalButton);

    JButton newCalButton = new JButton("New Calendar");
    newCalButton.addActionListener(e -> showCreateCalendarDialog());
    leftPanel.add(newCalButton);

    topPanel.add(leftPanel, BorderLayout.WEST);

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
    timezoneLabel = new JLabel("");
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

    // --- HEADER SECTION ---
    JPanel headerPanel = new JPanel(new BorderLayout());

    JLabel eventLabel = new JLabel("Events");
    eventLabel.setFont(new Font("Arial", Font.BOLD, 16));
    headerPanel.add(eventLabel, BorderLayout.WEST);

    // Copy Events Button (Bulk Copy)
    JButton copyEventsBtn = new JButton("Copy Events...");
    copyEventsBtn.setMargin(new Insets(2, 5, 2, 5));
    copyEventsBtn.setFocusable(false);
    copyEventsBtn.addActionListener(e -> showBulkCopyDialog());
    headerPanel.add(copyEventsBtn, BorderLayout.EAST);

    rightPanel.add(headerPanel, BorderLayout.NORTH);

    // --- SCROLLABLE EVENTS ---
    eventContainer = new JPanel();
    eventContainer.setLayout(new BoxLayout(eventContainer, BoxLayout.Y_AXIS));
    eventContainer.setBackground(Color.WHITE);

    JScrollPane scrollPane = new JScrollPane(eventContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    rightPanel.add(scrollPane, BorderLayout.CENTER);

    // --- ACTION BUTTONS ---
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

  // ==================== CORE DISPLAY METHODS ====================

  private void buildCalendar() {
    calendarGrid.removeAll();

    String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (String day : days) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 12));
      calendarGrid.add(label);
    }

    LocalDate firstDay = currentMonth.atDay(1);
    int startDayOfWeek = firstDay.getDayOfWeek().getValue() - 1;

    for (int i = 0; i < startDayOfWeek; i++) {
      calendarGrid.add(new JLabel(""));
    }

    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton dayButton = new JButton(String.valueOf(day));

      dayButton.setOpaque(true);
      dayButton.setBorderPainted(true);
      dayButton.setFocusPainted(false);

      boolean isToday = date.equals(LocalDate.now());
      boolean isSelected = date.equals(selectedDate);

      if (isToday) dayButton.setBackground(Color.PINK);
      if (isSelected) {
        dayButton.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
        dayButton.setFont(new Font("Arial", Font.BOLD, 14));
      }

      dayButton.addActionListener(e -> selectDate(date));
      calendarGrid.add(dayButton);
    }

    int cellsFilled = startDayOfWeek + daysInMonth;
    int totalCells = 42;
    for (int i = cellsFilled; i < totalCells; i++) {
      calendarGrid.add(new JLabel(""));
    }

    calendarGrid.revalidate();
    calendarGrid.repaint();
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
      List<String> availableCalendars = getCalendarNames();

      for (Event event : events) {
        // We pass the list of calendars to the EventPanel so it can populate its dropdown
        EventPanel eventPanel = new EventPanel(event, features, availableCalendars);
        eventContainer.add(eventPanel);
        eventContainer.add(Box.createVerticalStrut(5));
      }
    }

    eventContainer.revalidate();
    eventContainer.repaint();
  }

  // ==================== COPY LOGIC ====================

  private void showBulkCopyDialog() {
    if (features == null) return;

    Object[] options = {"Copy All on " + selectedDate, "Copy Date Range"};
    int choice = JOptionPane.showOptionDialog(frame,
        "Select Copy Mode:",
        "Copy Events",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);

    if (choice == JOptionPane.YES_OPTION) {
      showCopyEventsOnDateDialog();
    } else if (choice == JOptionPane.NO_OPTION) {
      showCopyEventsInRangeDialog();
    }
  }

  private void showCopyEventsOnDateDialog() {
    // 1. Fields
    JComboBox<String> targetCalSelector = new JComboBox<>(getCalendarNamesArray());

    // Use JSpinner for robust date picking
    JSpinner targetDateSpinner = new JSpinner(new SpinnerDateModel());
    targetDateSpinner.setEditor(new JSpinner.DateEditor(targetDateSpinner, "yyyy-MM-dd"));
    // Default to currently selected date
    Date defaultDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    targetDateSpinner.setValue(defaultDate);

    JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
    panel.add(new JLabel("Source Date: " + selectedDate));
    panel.add(new JLabel("Target Calendar:"));
    panel.add(targetCalSelector);
    panel.add(new JLabel("Target Date (YYYY-MM-DD):"));
    panel.add(targetDateSpinner);

    int result = JOptionPane.showConfirmDialog(frame, panel, "Copy Events On Date",
        JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      try {
        String targetCal = (String) targetCalSelector.getSelectedItem();
        if (targetCal == null) return;

        // Convert Date to LocalDate
        Date utilDate = (Date) targetDateSpinner.getValue();
        LocalDate targetDate = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Execute Command
        features.copyEventsOnDate(selectedDate, targetDate, targetCal);

      } catch (Exception e) {
        showError("Error reading date: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  private void showCopyEventsInRangeDialog() {
    // 1. Fields using JSpinner for safety
    JSpinner startSpinner = new JSpinner(new SpinnerDateModel());
    startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd"));

    JSpinner endSpinner = new JSpinner(new SpinnerDateModel());
    endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd"));

    JSpinner targetStartSpinner = new JSpinner(new SpinnerDateModel());
    targetStartSpinner.setEditor(new JSpinner.DateEditor(targetStartSpinner, "yyyy-MM-dd"));

    // Set defaults
    Date sDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date eDate = Date.from(selectedDate.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());
    startSpinner.setValue(sDate);
    endSpinner.setValue(eDate);
    targetStartSpinner.setValue(sDate);

    JComboBox<String> targetCalSelector = new JComboBox<>(getCalendarNamesArray());

    JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
    panel.add(new JLabel("Source Range Start:"));
    panel.add(startSpinner);
    panel.add(new JLabel("Source Range End:"));
    panel.add(endSpinner);
    panel.add(new JLabel("Target Calendar:"));
    panel.add(targetCalSelector);
    panel.add(new JLabel("Target Start Date:"));
    panel.add(targetStartSpinner);

    int result = JOptionPane.showConfirmDialog(frame, panel, "Copy Events In Range",
        JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      try {
        Date d1 = (Date) startSpinner.getValue();
        Date d2 = (Date) endSpinner.getValue();
        Date d3 = (Date) targetStartSpinner.getValue();

        LocalDate start = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate targetStart = d3.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String targetCal = (String) targetCalSelector.getSelectedItem();
        if (targetCal == null) return;

        System.out.println("[DEBUG] Copying range " + start + " -> " + end + " to target start " + targetStart);

        // Execute Command
        features.copyEventsBetweenDates(start, end, targetStart, targetCal);

      } catch (Exception e) {
        showError("Error reading dates: " + e.getMessage());
      }
    }
  }

  // ==================== UTILS & HELPERS ====================

  private List<String> getCalendarNames() {
    List<String> names = new ArrayList<>();
    for (int i = 0; i < calendarSelector.getItemCount(); i++) {
      names.add(calendarSelector.getItemAt(i));
    }
    return names;
  }

  private String[] getCalendarNamesArray() {
    return getCalendarNames().toArray(new String[0]);
  }

  // ==================== STANDARD ACTIONS ====================

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

  // ==================== DIALOGS (CREATE/EDIT) ====================

  private void showCreateCalendarDialog() {
    if (features == null) return;
    String name = JOptionPane.showInputDialog(frame, "Enter calendar name:");
    if (name == null || name.trim().isEmpty()) return;

    String[] timezones = {"America/New_York", "America/Los_Angeles", "America/Chicago",
        "Europe/London", "Europe/Paris", "Asia/Tokyo", "Asia/Kolkata"};
    String timezone = (String) JOptionPane.showInputDialog(frame,
        "Select timezone:", "Timezone", JOptionPane.QUESTION_MESSAGE,
        null, timezones, timezones[0]);

    if (timezone != null) features.createCalendar(name.trim(), timezone);
  }

  private void showEditCalendarDialog() {
    if (features == null) return;
    String currentCalendarName = (String) calendarSelector.getSelectedItem();
    if (currentCalendarName == null) {
      showError("Please create or select a calendar first.");
      return;
    }
    String[] options = {"Rename", "Change Timezone"};
    int choice = JOptionPane.showOptionDialog(frame, "Edit Calendar: " + currentCalendarName,
        "Edit Calendar", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, options, options[0]);

    if (choice == 0) { // Rename
      String newName = JOptionPane.showInputDialog(frame, "Enter new name:", currentCalendarName);
      if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(currentCalendarName)) {
        features.editCalendarName(currentCalendarName, newName.trim());
      }
    } else if (choice == 1) { // Timezone
      String[] timezones = {"America/New_York", "Europe/London", "Asia/Tokyo", "Asia/Kolkata"};
      String newTz = (String) JOptionPane.showInputDialog(frame, "Select new timezone:",
          "Change Timezone", JOptionPane.QUESTION_MESSAGE, null, timezones, timezones[0]);
      if (newTz != null) features.editCalendarTimezone(currentCalendarName, newTz);
    }
  }

  private void showCreateEventDialog() {
    if (features == null) return;
    EventCreationDialog dialog = new EventCreationDialog(frame, selectedDate, false);
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      features.createSingleEvent(dialog.getSubject(), dialog.getStartDateTime(), dialog.getEndDateTime());
    }
  }

  private void showCreateRecurringDialog() {
    if (features == null) return;
    EventCreationDialog dialog = new EventCreationDialog(frame, selectedDate, true);
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      if (dialog.isOccurrencesBased()) {
        features.createRecurringEventWithOccurrences(dialog.getSubject(), dialog.getStartDateTime(),
            dialog.getEndDateTime(), dialog.getSelectedWeekdays(), dialog.getOccurrences());
      } else {
        features.createRecurringEventUntilDate(dialog.getSubject(), dialog.getStartDateTime(),
            dialog.getEndDateTime(), dialog.getSelectedWeekdays(), dialog.getUntilDate());
      }
    }
  }

  private void showExportDialog() {
    if (features == null) return;
    String[] options = {"CSV", "iCal"};
    int choice = JOptionPane.showOptionDialog(frame, "Select export format:", "Export Calendar",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

    if (choice == 0 || choice == 1) {
      JFileChooser fileChooser = new JFileChooser();
      if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        if (choice == 0) features.exportToCSV(filePath);
        else features.exportToICal(filePath);
      }
    }
  }

  // ==================== PUBLIC ACCESSORS ====================

  public void setCalendarNames(List<String> names) {
    calendarSelector.removeAllItems();
    for (String name : names) calendarSelector.addItem(name);
  }

  public void setSelectedCalendar(String name) {
    calendarSelector.setSelectedItem(name);
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

  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  public void updateTimezoneDisplay(String timezoneId) {
    if (timezoneLabel != null) timezoneLabel.setText("Current Timezone: " + timezoneId);
  }

  public JFrame getFrame() {
    return this.frame;
  }
}