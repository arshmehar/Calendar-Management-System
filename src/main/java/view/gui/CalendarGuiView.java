// view/gui/CalendarGuiView.java
package view.gui;

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
  private final List<CalendarViewListener> listeners;

  // UI Components
  private JLabel monthLabel;
  private JPanel calendarGrid;
  private JTextArea eventDisplay;
  private JComboBox<String> calendarSelector;

  private YearMonth currentMonth;
  private LocalDate selectedDate;

  public CalendarGuiView() {
    this.listeners = new ArrayList<>();
    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();

    frame = new JFrame("Calendar Application");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(900, 700);
    frame.setLocationRelativeTo(null);

    setupUI();
  }

  /**
   * Add a listener to receive GUI events.
   */
  public void addListener(CalendarViewListener listener) {
    listeners.add(listener);
  }

  private void setupUI() {
    frame.setLayout(new BorderLayout(10, 10));

    // Top panel
    JPanel topPanel = new JPanel(new FlowLayout());

    topPanel.add(new JLabel("Calendar:"));
    calendarSelector = new JComboBox<>();
    calendarSelector.addActionListener(e -> {
      String selected = (String) calendarSelector.getSelectedItem();
      if (selected != null) {
        notifyCalendarSelected(selected);
      }
    });
    topPanel.add(calendarSelector);

    JButton newCalButton = new JButton("New Calendar");
    newCalButton.addActionListener(e -> showCreateCalendarDialog());
    topPanel.add(newCalButton);

    JButton prevButton = new JButton("< Prev");
    prevButton.addActionListener(e -> changeMonth(-1));
    topPanel.add(prevButton);

    monthLabel = new JLabel();
    monthLabel.setFont(new Font("Arial", Font.BOLD, 16));
    topPanel.add(monthLabel);

    JButton nextButton = new JButton("Next >");
    nextButton.addActionListener(e -> changeMonth(1));
    topPanel.add(nextButton);

    JButton todayButton = new JButton("Today");
    todayButton.addActionListener(e -> goToToday());
    topPanel.add(todayButton);

    frame.add(topPanel, BorderLayout.NORTH);

    // Center - calendar grid
    calendarGrid = new JPanel(new GridLayout(7, 7, 5, 5));
    calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    frame.add(calendarGrid, BorderLayout.CENTER);

    // Right panel
    JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
    rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    rightPanel.setPreferredSize(new Dimension(300, 0));

    JLabel eventLabel = new JLabel("Events:");
    rightPanel.add(eventLabel, BorderLayout.NORTH);

    eventDisplay = new JTextArea();
    eventDisplay.setEditable(false);
    eventDisplay.setLineWrap(true);
    eventDisplay.setWrapStyleWord(true);
    JScrollPane scrollPane = new JScrollPane(eventDisplay);
    rightPanel.add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

    JButton createEventBtn = new JButton("Create Event");
    createEventBtn.addActionListener(e -> showCreateEventDialog());
    buttonPanel.add(createEventBtn);

    JButton editEventBtn = new JButton("Edit Event");
    editEventBtn.addActionListener(e -> showEditEventDialog());
    buttonPanel.add(editEventBtn);

    rightPanel.add(buttonPanel, BorderLayout.SOUTH);
    frame.add(rightPanel, BorderLayout.EAST);

    updateMonthLabel();
    buildCalendar();
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

    // Get first day of month
    LocalDate firstDay = currentMonth.atDay(1);
    int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;

    // Empty cells before month starts
    for (int i = 0; i < startDayOfWeek; i++) {
      calendarGrid.add(new JLabel(""));
    }

    // Day buttons
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
    notifyDateSelected(date);
  }

  private void changeMonth(int delta) {
    currentMonth = currentMonth.plusMonths(delta);
    updateMonthLabel();
    buildCalendar();
    notifyMonthChanged(currentMonth.getYear(), currentMonth.getMonthValue());
  }

  private void goToToday() {
    currentMonth = YearMonth.now();
    selectedDate = LocalDate.now();
    updateMonthLabel();
    buildCalendar();
    notifyDateSelected(selectedDate);
  }

  private void updateMonthLabel() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    monthLabel.setText(currentMonth.format(formatter));
  }

  // Dialog methods - just gather input and notify listeners

  private void showCreateCalendarDialog() {
    String name = JOptionPane.showInputDialog(frame, "Enter calendar name:");
    if (name == null || name.trim().isEmpty()) return;

    String[] timezones = {"America/New_York", "America/Los_Angeles", "America/Chicago",
        "Europe/London", "Europe/Paris", "Asia/Tokyo", "Asia/Kolkata"};
    String timezone = (String) JOptionPane.showInputDialog(frame,
        "Select timezone:", "Timezone", JOptionPane.QUESTION_MESSAGE,
        null, timezones, timezones[0]);

    if (timezone != null) {
      notifyCreateCalendarRequested(name.trim(), timezone);
    }
  }

  private void showCreateEventDialog() {
    SimpleEventDialog dialog = new SimpleEventDialog(frame, selectedDate);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      notifyCreateEventRequested(
          selectedDate,
          dialog.getSubject(),
          dialog.getStartDateTime().toLocalDate(),
          dialog.getStartDateTime().getHour(),
          dialog.getStartDateTime().getMinute(),
          dialog.getEndDateTime().toLocalDate(),
          dialog.getEndDateTime().getHour(),
          dialog.getEndDateTime().getMinute()
      );
    }
  }

  private void showEditEventDialog() {
    // Get current event text to parse which event to edit
    String eventText = eventDisplay.getText();
    if (eventText.contains("No events") || eventText.contains("No calendar")) {
      showError("No events to edit");
      return;
    }

    // Simple: ask user to type event name
    String eventName = JOptionPane.showInputDialog(frame,
        "Enter event name to edit:");
    if (eventName == null || eventName.trim().isEmpty()) return;

    SimpleEventDialog dialog = new SimpleEventDialog(frame, selectedDate);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      notifyEditEventRequested(
          selectedDate,
          eventName.trim(),
          dialog.getSubject(),
          dialog.getStartDateTime().getHour(),
          dialog.getStartDateTime().getMinute(),
          dialog.getEndDateTime().getHour(),
          dialog.getEndDateTime().getMinute()
      );
    }
  }

  // Notification methods - notify all listeners

  private void notifyCreateCalendarRequested(String name, String timezone) {
    for (CalendarViewListener listener : listeners) {
      listener.onCreateCalendarRequested(name, timezone);
    }
  }

  private void notifyCalendarSelected(String name) {
    for (CalendarViewListener listener : listeners) {
      listener.onCalendarSelected(name);
    }
  }

  private void notifyDateSelected(LocalDate date) {
    for (CalendarViewListener listener : listeners) {
      listener.onDateSelected(date);
    }
  }

  private void notifyCreateEventRequested(LocalDate date, String subject,
                                          LocalDate startDate, int startHour, int startMinute,
                                          LocalDate endDate, int endHour, int endMinute) {
    for (CalendarViewListener listener : listeners) {
      listener.onCreateEventRequested(date, subject, startDate, startHour, startMinute,
          endDate, endHour, endMinute);
    }
  }

  private void notifyEditEventRequested(LocalDate date, String eventId,
                                        String newSubject, int startHour, int startMinute,
                                        int endHour, int endMinute) {
    for (CalendarViewListener listener : listeners) {
      listener.onEditEventRequested(date, eventId, newSubject,
          startHour, startMinute, endHour, endMinute);
    }
  }

  private void notifyMonthChanged(int year, int month) {
    for (CalendarViewListener listener : listeners) {
      listener.onMonthChanged(year, month);
    }
  }

  // Public methods for controller to update view

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