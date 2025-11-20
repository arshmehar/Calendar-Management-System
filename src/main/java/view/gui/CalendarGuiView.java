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

/**
 * Simple GUI view - only displays and notifies listeners.
 * Does NOT call controller or model directly.
 */
public class CalendarGuiView {
  private final JFrame frame;
  private CalendarFeatures features;

  // UI Components
  private JLabel monthLabel;
  private JPanel calendarGrid;
  private JTextArea eventDisplay;
  private JComboBox<String> calendarSelector;

  private YearMonth currentMonth;
  private LocalDate selectedDate;

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
    calendarGrid = new JPanel(new GridLayout(7, 7, 5, 5));
    calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    frame.add(calendarGrid, BorderLayout.CENTER);

    // Right panel
    frame.add(createRightPanel(), BorderLayout.EAST);

    updateMonthLabel();
    buildCalendar();
  }

  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());

    // Left - calendar selection
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    leftPanel.add(new JLabel("Calendar:"));
    calendarSelector = new JComboBox<>();
    calendarSelector.addActionListener(e -> {
      if (features != null) {
        String selected = (String) calendarSelector.getSelectedItem();
        if (selected != null) {
          features.selectCalendar(selected);
        }
      }
    });
    leftPanel.add(calendarSelector);

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

    return topPanel;
  }

  private JPanel createRightPanel() {
    JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
    rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    rightPanel.setPreferredSize(new Dimension(320, 0));

    JLabel eventLabel = new JLabel("Events:");
    eventLabel.setFont(new Font("Arial", Font.BOLD, 14));
    rightPanel.add(eventLabel, BorderLayout.NORTH);

    eventDisplay = new JTextArea();
    eventDisplay.setEditable(false);
    eventDisplay.setLineWrap(true);
    eventDisplay.setWrapStyleWord(true);
    JScrollPane scrollPane = new JScrollPane(eventDisplay);
    rightPanel.add(scrollPane, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));

    JButton createEventBtn = new JButton("Create Event");
    createEventBtn.addActionListener(e -> showCreateEventDialog());
    buttonPanel.add(createEventBtn);

    JButton createRecurringBtn = new JButton("Create Recurring");
    createRecurringBtn.addActionListener(e -> showCreateRecurringDialog());
    buttonPanel.add(createRecurringBtn);

    JButton editEventBtn = new JButton("Edit Event");
    editEventBtn.addActionListener(e -> showEditEventDialog());
    buttonPanel.add(editEventBtn);

    JButton copyEventBtn = new JButton("Copy Event");
    copyEventBtn.addActionListener(e -> showCopyEventDialog());
    buttonPanel.add(copyEventBtn);

    JButton exportBtn = new JButton("Export Calendar");
    exportBtn.addActionListener(e -> showExportDialog());
    buttonPanel.add(exportBtn);

    rightPanel.add(buttonPanel, BorderLayout.SOUTH);

    return rightPanel;
  }

  private void buildCalendar() {
    calendarGrid.removeAll();

    // Day headers
    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : days) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 12));
      calendarGrid.add(label);
    }

    LocalDate firstDay = currentMonth.atDay(1);
    int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;

    for (int i = 0; i < startDayOfWeek; i++) {
      calendarGrid.add(new JLabel(""));
    }

    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton dayButton = new JButton(String.valueOf(day));

      if (date.equals(LocalDate.now())) {
        dayButton.setBackground(Color.YELLOW);
      }

      if (date.equals(selectedDate)) {
        dayButton.setBackground(Color.LIGHT_GRAY);
      }

      dayButton.addActionListener(e -> selectDate(date));
      calendarGrid.add(dayButton);
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

  private void showCopyEventDialog() {
    if (features == null) return;

    // Simple copy dialog - can be enhanced
    JOptionPane.showMessageDialog(frame,
        "Copy feature - implement based on needs",
        "Copy Event",
        JOptionPane.INFORMATION_MESSAGE);
  }

  private void showExportDialog() {
    if (features == null) return;

    String[] options = {"CSV", "iCal", "Cancel"};
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

  public void setEventDisplayText(String text) {
    eventDisplay.setText(text);
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
}