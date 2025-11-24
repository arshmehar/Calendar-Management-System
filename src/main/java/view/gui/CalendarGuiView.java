package view.gui;

import controller.features.CalendarFeatures;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import model.Event;

public class CalendarGuiView implements IcalendarGuiView {
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

  // Clean Colors
  private static final Color COL_TODAY = new Color(255, 252, 220); // Soft Yellow
  private static final Color COL_SELECTED = new Color(225, 240, 255); // Soft Blue
  private static final Color COL_BORDER = new Color(220, 220, 220); // Light Gray

  public CalendarGuiView() {
    // 1. ENABLE NATIVE LOOK AND FEEL
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) { }

    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();

    frame = new JFrame("Calendar Application");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1000, 750); // Slightly larger default
    frame.setLocationRelativeTo(null);

    setupUi();
  }

  public void setFeatures(CalendarFeatures features) {
    this.features = features;
  }

  private void setupUi() {
    frame.setLayout(new BorderLayout(0, 0));

    // Main container background
    frame.getContentPane().setBackground(Color.WHITE);

    // Top panel
    frame.add(createTopPanel(), BorderLayout.NORTH);

    // Center - calendar grid
    calendarGrid = new JPanel(new GridLayout(0, 7, 0, 0)); // No gap, let borders handle it
    calendarGrid.setBackground(Color.WHITE);
    calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));
    frame.add(calendarGrid, BorderLayout.CENTER);

    // Right panel
    frame.add(createRightPanel(), BorderLayout.EAST);

    updateMonthLabel();
    buildCalendar();
  }

  private JPanel createTopPanel() {
    JPanel topContainer = new JPanel(new BorderLayout());
    topContainer.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
    topContainer.setBackground(Color.WHITE);

    // Left - Tools
    JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    toolsPanel.setBackground(Color.WHITE);

    toolsPanel.add(new JLabel("Calendar:"));
    calendarSelector = new JComboBox<>();
    calendarSelector.addActionListener(e -> {
      if (features != null && calendarSelector.getSelectedItem() != null) {
        features.selectCalendar((String) calendarSelector.getSelectedItem());
      }
    });
    toolsPanel.add(calendarSelector);

    JButton editCalButton = new JButton("Edit");
    editCalButton.addActionListener(e -> showEditCalendarDialog());
    toolsPanel.add(editCalButton);

    JButton newCalButton = new JButton("New");
    newCalButton.addActionListener(e -> showCreateCalendarDialog());
    toolsPanel.add(newCalButton);

    topContainer.add(toolsPanel, BorderLayout.WEST);

    // Center - Navigation
    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
    navPanel.setBackground(Color.WHITE);

    JButton prevButton = new JButton("◀"); // Unicode Arrow
    prevButton.setFocusPainted(false);
    prevButton.addActionListener(e -> changeMonth(-1));
    navPanel.add(prevButton);

    monthLabel = new JLabel();
    monthLabel.setFont(new Font("SansSerif", Font.BOLD, 20)); // Larger Title
    navPanel.add(monthLabel);

    JButton nextButton = new JButton("▶"); // Unicode Arrow
    nextButton.setFocusPainted(false);
    nextButton.addActionListener(e -> changeMonth(1));
    navPanel.add(nextButton);

    JButton todayButton = new JButton("Today");
    todayButton.setFocusPainted(false);
    todayButton.addActionListener(e -> goToToday());
    navPanel.add(todayButton);

    topContainer.add(navPanel, BorderLayout.CENTER);

    // Bottom - Status Bar
    timezoneLabel = new JLabel("");
    timezoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    timezoneLabel.setForeground(Color.GRAY);

    // Wrapper for timezone to sit at bottom of header
    JPanel bottomStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomStatus.setBackground(Color.WHITE);
    bottomStatus.add(timezoneLabel);

    topContainer.add(bottomStatus, BorderLayout.SOUTH);

    return topContainer;
  }

  private JPanel createRightPanel() {
    JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
    rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 20));
    rightPanel.setPreferredSize(new Dimension(320, 0));
    rightPanel.setBackground(Color.WHITE);

    // Header
    JLabel eventLabel = new JLabel("Events");
    eventLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    eventLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    rightPanel.add(eventLabel, BorderLayout.NORTH);

    // Scrollable event container
    eventContainer = new JPanel();
    eventContainer.setLayout(new BoxLayout(eventContainer, BoxLayout.Y_AXIS));
    eventContainer.setBackground(new Color(250, 250, 250)); // Very light gray

    JScrollPane scrollPane = new JScrollPane(eventContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(BorderFactory.createLineBorder(COL_BORDER));
    rightPanel.add(scrollPane, BorderLayout.CENTER);

    // Action buttons
    JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 8));
    buttonPanel.setBackground(Color.WHITE);

    JButton createEventBtn = new JButton("Create Event");
    createEventBtn.addActionListener(e -> showCreateEventDialog());
    buttonPanel.add(createEventBtn);

    JButton createRecurringBtn = new JButton("Create Recurring Series");
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

    // Day headers
    String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (String day : days) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("SansSerif", Font.BOLD, 12));
      label.setForeground(Color.GRAY);
      label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER));
      calendarGrid.add(label);
    }

    LocalDate firstDay = currentMonth.atDay(1);
    int startDayOfWeek = firstDay.getDayOfWeek().getValue() - 1;

    // Empty slots
    for (int i = 0; i < startDayOfWeek; i++) {
      JPanel empty = new JPanel();
      empty.setBackground(Color.WHITE);
      empty.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(240, 240, 240)));
      calendarGrid.add(empty);
    }

    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton dayButton = new JButton(String.valueOf(day));

      // Modern Flat Styling
      dayButton.setOpaque(true);
      dayButton.setContentAreaFilled(true);
      dayButton.setFocusPainted(false);
      dayButton.setFont(new Font("SansSerif", Font.PLAIN, 14));

      // Default Look
      dayButton.setBackground(Color.WHITE);
      dayButton.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

      boolean isToday = date.equals(LocalDate.now());
      boolean isSelected = date.equals(selectedDate);

      if (isToday) {
        dayButton.setBackground(COL_TODAY); // Soft Yellow
        dayButton.setFont(new Font("SansSerif", Font.BOLD, 14));
      }

      if (isSelected) {
        dayButton.setBackground(COL_SELECTED); // Soft Blue
        dayButton.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 2)); // Cornflower Blue
      }

      dayButton.addActionListener(e -> selectDate(date));
      calendarGrid.add(dayButton);
    }

    // Trailing empty slots
    int cellsFilled = startDayOfWeek + daysInMonth;
    int totalCells = 42;
    for (int i = cellsFilled; i < totalCells; i++) {
      JPanel empty = new JPanel();
      empty.setBackground(Color.WHITE);
      empty.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(240, 240, 240)));
      calendarGrid.add(empty);
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

  // ==================== DIALOG WRAPPERS ====================
  // (Logic remains same, UI styling is inside dialog classes)

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
        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    if (choice == 0 || choice == 1) {
      JFileChooser fileChooser = new JFileChooser();
      if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        if (choice == 0) features.exportToCsv(filePath);
        else features.exportToIcal(filePath);
      }
    }
  }

  private void showEditCalendarDialog() {
    if (features == null) return;
    String currentCalendarName = (String) calendarSelector.getSelectedItem();
    if (currentCalendarName == null) {
      showError("Please create or select a calendar first.");
      return;
    }
    String[] options = {"Rename", "Change Timezone"};
    int choice = JOptionPane.showOptionDialog(frame,
        "Edit '" + currentCalendarName + "'", "Edit Calendar",
        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

    if (choice == 0) {
      String newName = JOptionPane.showInputDialog(frame, "Enter new name:", currentCalendarName);
      if (newName != null && !newName.trim().isEmpty()) {
        features.editCalendarName(currentCalendarName, newName.trim());
      }
    } else if (choice == 1) {
      String[] timezones = {"America/New_York", "Europe/London", "Asia/Tokyo", "Asia/Kolkata"};
      String newTimezone = (String) JOptionPane.showInputDialog(frame,
          "Select new timezone:", "Change Timezone", JOptionPane.QUESTION_MESSAGE, null, timezones, timezones[0]);
      if (newTimezone != null) features.editCalendarTimezone(currentCalendarName, newTimezone);
    }
  }

  // ==================== PUBLIC METHODS ====================

  public void setCalendarNames(List<String> names) {
    calendarSelector.removeAllItems();
    for (String name : names) calendarSelector.addItem(name);
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
        // Wrapper for spacing
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(250, 250, 250));
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        wrapper.add(eventPanel, BorderLayout.CENTER);

        eventContainer.add(wrapper);
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

  public void display() { frame.setVisible(true); }
  public JFrame getFrame() { return frame; }
  public LocalDate getSelectedDate() { return selectedDate; }

  public void updateTimezoneDisplay(String timezoneId) {
    if (timezoneLabel != null) timezoneLabel.setText(timezoneId);
  }
  public YearMonth getCurrentDisplayContext() { return currentMonth; }
}