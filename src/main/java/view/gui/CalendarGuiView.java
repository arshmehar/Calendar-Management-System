// view/gui/CalendarGuiView.java
package view.gui;

import controller.IguiController;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Simple GUI window for the calendar application.
 */
public class CalendarGuiView {
  private final IguiController controller;
  private final JFrame frame;
  private JLabel monthLabel;
  private JPanel calendarGrid;
  private JTextArea eventDisplay;
  private JComboBox<String> calendarSelector;

  private YearMonth currentMonth;
  private LocalDate selectedDate;

  public CalendarGuiView(IguiController controller) {
    this.controller = controller;
    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();

    frame = new JFrame("Calendar Application");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(900, 700);
    frame.setLocationRelativeTo(null);

    setupUI();
  }

  private void setupUI() {
    frame.setLayout(new BorderLayout(10, 10));

    // Top panel - calendar selector and navigation
    JPanel topPanel = new JPanel(new FlowLayout());

    topPanel.add(new JLabel("Calendar:"));
    calendarSelector = new JComboBox<>();
    calendarSelector.addActionListener(e -> onCalendarChanged());
    topPanel.add(calendarSelector);

    JButton newCalButton = new JButton("New Calendar");
    newCalButton.addActionListener(e -> createCalendar());
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

    // Right panel - event display and actions
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
    createEventBtn.addActionListener(e -> createEvent());
    buttonPanel.add(createEventBtn);

    JButton editEventBtn = new JButton("Edit Event");
    editEventBtn.addActionListener(e -> editEvent());
    buttonPanel.add(editEventBtn);

    rightPanel.add(buttonPanel, BorderLayout.SOUTH);

    frame.add(rightPanel, BorderLayout.EAST);

    updateMonthLabel();
    buildCalendar();
    refreshCalendarList();
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

      // Highlight today
      if (date.equals(LocalDate.now())) {
        dayButton.setBackground(Color.YELLOW);
      }

      // Highlight selected
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
    updateEventDisplay();
  }

  private void updateEventDisplay() {
    if (controller.getCurrentCalendar() == null) {
      eventDisplay.setText("No calendar selected");
      return;
    }

    var events = controller.getCurrentCalendar().getEventsOn(selectedDate);
    if (events.isEmpty()) {
      eventDisplay.setText("No events on " + selectedDate);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("Events on ").append(selectedDate).append(":\n\n");
      DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm a");

      for (var event : events) {
        sb.append("• ").append(event.getSubject()).append("\n");
        sb.append("  ").append(event.getStart().format(timeFormat))
            .append(" - ").append(event.getEnd().format(timeFormat)).append("\n\n");
      }
      eventDisplay.setText(sb.toString());
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
    updateEventDisplay();
  }

  private void updateMonthLabel() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    monthLabel.setText(currentMonth.format(formatter));
  }

  private void onCalendarChanged() {
    String selected = (String) calendarSelector.getSelectedItem();
    if (selected != null) {
      controller.setCurrentCalendar(selected);
      buildCalendar();
      updateEventDisplay();
    }
  }

  private void createCalendar() {
    String name = JOptionPane.showInputDialog(frame, "Enter calendar name:");
    if (name == null || name.trim().isEmpty()) return;

    String[] timezones = {"America/New_York", "America/Los_Angeles", "America/Chicago",
        "Europe/London", "Europe/Paris", "Asia/Tokyo", "Asia/Kolkata"};
    String timezone = (String) JOptionPane.showInputDialog(frame,
        "Select timezone:", "Timezone", JOptionPane.QUESTION_MESSAGE,
        null, timezones, timezones[0]);

    if (timezone != null) {
      try {
        controller.getModel().createCalendar(name, timezone);
        refreshCalendarList();
        controller.showSuccess("Calendar created");
      } catch (Exception e) {
        controller.showError("Error: " + e.getMessage());
      }
    }
  }

  private void createEvent() {
    if (controller.getCurrentCalendar() == null) {
      controller.showError("Please select a calendar first");
      return;
    }

    SimpleEventDialog dialog = new SimpleEventDialog(frame, selectedDate);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      try {
        controller.getCurrentCalendar().addEvent(
            dialog.getSubject(),
            dialog.getStartDateTime(),
            dialog.getEndDateTime()
        );
        refresh();
        controller.showSuccess("Event created");
      } catch (Exception e) {
        controller.showError("Error: " + e.getMessage());
      }
    }
  }

  private void editEvent() {
    if (controller.getCurrentCalendar() == null) {
      controller.showError("Please select a calendar first");
      return;
    }

    var events = controller.getCurrentCalendar().getEventsOn(selectedDate);
    if (events.isEmpty()) {
      controller.showError("No events on selected date");
      return;
    }

    // Simple: show list of events to select
    String[] eventNames = events.stream()
        .map(e -> e.getSubject() + " (" + e.getStart().toLocalTime() + ")")
        .toArray(String[]::new);

    String selected = (String) JOptionPane.showInputDialog(frame,
        "Select event to edit:", "Edit Event",
        JOptionPane.QUESTION_MESSAGE, null, eventNames, eventNames[0]);

    if (selected != null) {
      int index = java.util.Arrays.asList(eventNames).indexOf(selected);
      var event = events.get(index);

      SimpleEventDialog dialog = new SimpleEventDialog(frame, selectedDate, event);
      dialog.setVisible(true);

      if (dialog.isConfirmed()) {
        try {
          controller.getCurrentCalendar().editEvent(
              event.getSubject(),
              event.getStart(),
              event.getEnd(),
              "subject",
              dialog.getSubject()
          );
          refresh();
          controller.showSuccess("Event updated");
        } catch (Exception e) {
          controller.showError("Error: " + e.getMessage());
        }
      }
    }
  }

  private void refreshCalendarList() {
    calendarSelector.removeAllItems();
    for (String name : controller.getAllCalendarNames()) {
      calendarSelector.addItem(name);
    }
  }

  public void refresh() {
    buildCalendar();
    updateEventDisplay();
    refreshCalendarList();
  }

  public void display() {
    frame.setVisible(true);
  }

  public JFrame getFrame() {
    return frame;
  }
}