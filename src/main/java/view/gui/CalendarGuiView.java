package view.gui;

import controller.features.CalendarFeatures;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Event;

/**
 * Simple GUI view - only displays and notifies listeners.
 * Does NOT call controller or model directly.
 * This class implements the IcalendarGuiView contract for the Swing interface.
 */
public class CalendarGuiView implements IcalendarGuiView {
  private final JFrame frame;
  private CalendarFeatures features;

  private JLabel monthLabel;
  private JPanel calendarGrid;
  private JTextArea eventDisplay;
  private JComboBox<String> calendarSelector;
  private JLabel timezoneLabel;

  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private JPanel eventContainer;

  private static final String[] AVAILABLE_TIMEZONES = {
      "UTC", "America/New_York", "America/Chicago", "America/Denver",
      "America/Los_Angeles", "America/Toronto",
      "America/Sao_Paulo", "Europe/London", "Europe/Paris",
      "Asia/Tokyo", "Asia/Hong_Kong",
      "Asia/Dubai", "Asia/Kolkata", "Australia/Sydney"
  };

  /**
   * Constructs the CalendarGuiView and initializes the main application frame
   * with the current month and date.
   */
  public CalendarGuiView() {

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      System.out.println("Could not set look and feel");
    }

    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();

    frame = new JFrame("Calendar Application");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1000, 750);
    frame.setLocationRelativeTo(null);

    setupUi();
  }

  /**
   * Sets the feature listener (controller) for this view.
   *
   * @param features the controller implementation of CalendarFeatures.
   */
  public void setFeatures(CalendarFeatures features) {
    this.features = features;
  }

  /**
   * Sets up the main UI layout, including the top panel, calendar grid, and right panel.
   */
  private void setupUi() {
    frame.setLayout(new BorderLayout(0, 0));
    frame.getContentPane().setBackground(Color.WHITE);

    frame.add(createTopPanel(), BorderLayout.NORTH);

    calendarGrid = new JPanel(new GridLayout(0, 7, 0, 0));
    calendarGrid.setBackground(Color.WHITE);
    calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));
    frame.add(calendarGrid, BorderLayout.CENTER);

    frame.add(createRightPanel(), BorderLayout.EAST);

    updateMonthLabel();
    buildCalendar();
  }

  /**
   * Creates the top panel containing calendar selection, navigation, and timezone display.
   *
   * @return A JPanel representing the top section of the GUI.
   */
  private JPanel createTopPanel() {
    JPanel topContainer = new JPanel(new BorderLayout());
    topContainer.setBackground(Color.WHITE);
    topContainer.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

    JPanel mainLeftPanel = new JPanel();
    mainLeftPanel.setLayout(new BoxLayout(mainLeftPanel, BoxLayout.Y_AXIS));
    mainLeftPanel.setBackground(Color.WHITE);

    JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    toolsPanel.setBackground(Color.WHITE);
    toolsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel calLabel = new JLabel("Calendar:");
    calLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
    toolsPanel.add(calLabel);

    calendarSelector = new JComboBox<>();
    calendarSelector.setFont(new Font("SansSerif", Font.PLAIN, 14));
    calendarSelector.addActionListener(e -> {
      if (features != null && calendarSelector.getSelectedItem() != null) {
        String selected = (String) calendarSelector.getSelectedItem();
        features.selectCalendar(selected);
      }
    });
    toolsPanel.add(calendarSelector);

    JButton editCalButton = new JButton("Edit Calendar");
    editCalButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
    editCalButton.setFocusPainted(false);
    editCalButton.addActionListener(e -> showEditCalendarDialog());
    toolsPanel.add(editCalButton);

    JButton newCalButton = new JButton("New Calendar");
    newCalButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
    newCalButton.setFocusPainted(false);
    newCalButton.addActionListener(e -> showCreateCalendarDialog());
    toolsPanel.add(newCalButton);

    mainLeftPanel.add(toolsPanel);
    mainLeftPanel.add(Box.createVerticalStrut(20));

    JPanel tzPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    tzPanel.setBackground(Color.WHITE);
    tzPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    timezoneLabel = new JLabel("Current Timezone: ");
    timezoneLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
    timezoneLabel.setForeground(Color.GRAY);
    tzPanel.add(timezoneLabel);

    mainLeftPanel.add(tzPanel);

    topContainer.add(mainLeftPanel, BorderLayout.WEST);

    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
    navPanel.setOpaque(false);

    JButton prevButton = new JButton("◀");
    prevButton.setFocusPainted(false);
    prevButton.addActionListener(e -> changeMonth(-1));
    navPanel.add(prevButton);

    monthLabel = new JLabel();
    monthLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    navPanel.add(monthLabel);

    JButton nextButton = new JButton("▶");
    nextButton.setFocusPainted(false);
    nextButton.addActionListener(e -> changeMonth(1));
    navPanel.add(nextButton);

    JButton todayButton = new JButton("Today");
    todayButton.setFocusPainted(false);
    todayButton.addActionListener(e -> goToToday());
    navPanel.add(todayButton);

    topContainer.add(navPanel, BorderLayout.CENTER);

    return topContainer;
  }


  /**
   * Creates the right panel containing the event list display and action buttons.
   *
   * @return A JPanel representing the right section of the GUI.
   */
  private JPanel createRightPanel() {
    JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
    rightPanel.setBackground(Color.WHITE);
    rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 20));
    rightPanel.setPreferredSize(new Dimension(320, 0));

    JLabel eventLabel = new JLabel("Events");
    eventLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    rightPanel.add(eventLabel, BorderLayout.NORTH);

    eventContainer = new JPanel();
    eventContainer.setLayout(new BoxLayout(eventContainer, BoxLayout.Y_AXIS));
    eventContainer.setBackground(new Color(250, 250, 250));

    JScrollPane scrollPane = new JScrollPane(eventContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
    rightPanel.add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 8));
    buttonPanel.setBackground(Color.WHITE);

    Font buttonFont = new Font("SansSerif", Font.BOLD, 14);

    JButton createEventBtn = new JButton("Create Event");
    createEventBtn.setFocusPainted(false);
    createEventBtn.setFont(buttonFont);
    createEventBtn.addActionListener(e -> showCreateEventDialog());
    buttonPanel.add(createEventBtn);

    JButton createRecurringBtn = new JButton("Create Recurring Series");
    createRecurringBtn.setFocusPainted(false);
    createRecurringBtn.setFont(buttonFont);
    createRecurringBtn.addActionListener(e -> showCreateRecurringDialog());
    buttonPanel.add(createRecurringBtn);

    JButton exportBtn = new JButton("Export Calendar");
    exportBtn.setFocusPainted(false);
    exportBtn.setFont(buttonFont);
    exportBtn.addActionListener(e -> showExportDialog());
    buttonPanel.add(exportBtn);

    rightPanel.add(buttonPanel, BorderLayout.SOUTH);

    return rightPanel;
  }

  /**
   * Renders the calendar grid for the currently displayed month.
   */
  private void buildCalendar() {
    calendarGrid.removeAll();

    String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (String day : days) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("SansSerif", Font.BOLD, 12));
      label.setForeground(Color.GRAY);
      label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
      calendarGrid.add(label);
    }

    LocalDate firstDay = currentMonth.atDay(1);

    int startDayOfWeek = firstDay.getDayOfWeek().getValue() - 1;

    for (int i = 0; i < startDayOfWeek; i++) {
      JPanel empty = new JPanel();
      empty.setBackground(Color.WHITE);
      empty.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(245, 245, 245)));
      calendarGrid.add(empty);
    }

    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton dayButton = new JButton(String.valueOf(day));

      dayButton.setOpaque(true);
      dayButton.setContentAreaFilled(true);
      dayButton.setFocusPainted(false);
      dayButton.setFont(new Font("SansSerif", Font.PLAIN, 14));

      dayButton.setBackground(Color.WHITE);
      dayButton.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

      boolean isToday = date.equals(LocalDate.now());
      boolean isSelected = date.equals(selectedDate);

      if (isToday) {
        dayButton.setBackground(new Color(255, 249, 196));
        dayButton.setFont(new Font("SansSerif", Font.BOLD, 14));
      }

      if (isSelected) {
        dayButton.setBackground(new Color(225, 245, 254));
        dayButton.setBorder(BorderFactory.createLineBorder(new Color(129, 212, 250), 2));
      }

      dayButton.addActionListener(e -> selectDate(date));
      calendarGrid.add(dayButton);
    }

    int cellsFilled = startDayOfWeek + daysInMonth;
    int totalCells = 42;
    for (int i = cellsFilled; i < totalCells; i++) {
      JPanel empty = new JPanel();
      empty.setBackground(Color.WHITE);
      empty.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(245, 245, 245)));
      calendarGrid.add(empty);
    }

    calendarGrid.revalidate();
    calendarGrid.repaint();
  }

  /**
   * Handles user selection of a date on the calendar grid.
   *
   * @param date The LocalDate selected by the user.
   */
  private void selectDate(LocalDate date) {
    selectedDate = date;
    buildCalendar();
    if (features != null) {
      features.viewEventsOnDate(date);
    }
  }

  /**
   * Handles navigation to the previous or next month.
   *
   * @param delta -1 for previous month, +1 for next month.
   */
  private void changeMonth(int delta) {
    currentMonth = currentMonth.plusMonths(delta);
    updateMonthLabel();
    buildCalendar();
  }

  /**
   * Handles navigation back to the current system month and day.
   */
  private void goToToday() {
    currentMonth = YearMonth.now();
    selectedDate = LocalDate.now();
    updateMonthLabel();
    buildCalendar();
    if (features != null) {
      features.viewEventsOnDate(selectedDate);
    }
  }

  /**
   * Updates the Month/Year label in the navigation bar.
   */
  private void updateMonthLabel() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    monthLabel.setText(currentMonth.format(formatter));
  }


  /**
   * Displays the dialog for creating a new calendar.
   */
  private void showCreateCalendarDialog() {
    if (features == null) {
      return;
    }

    String name = JOptionPane.showInputDialog(frame, "Enter calendar name:");
    if (name == null || name.trim().isEmpty()) {
      return;
    }

    String timezone = (String) JOptionPane.showInputDialog(frame,
        "Select timezone:", "Timezone", JOptionPane.QUESTION_MESSAGE,
        null, AVAILABLE_TIMEZONES, AVAILABLE_TIMEZONES[0]);

    if (timezone != null) {
      features.createCalendar(name.trim(), timezone);
    }
  }

  /**
   * Displays the dialog for creating a single event.
   */
  private void showCreateEventDialog() {
    if (features == null) {
      return;
    }

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

  /**
   * Displays the dialog for creating a recurring event series.
   */
  private void showCreateRecurringDialog() {
    if (features == null) {
      return;
    }

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

  /**
   * Displays the dialog for editing an existing event.
   */
  private void showEditEventDialog() {
    if (features == null) {
      return;
    }

    String eventName = JOptionPane.showInputDialog(frame,
        "Enter event name to edit:");
    if (eventName == null || eventName.trim().isEmpty()) {
      return;
    }

    String property = "subject";
    String newValue = JOptionPane.showInputDialog(frame,
        "Enter new subject:");
    if (newValue == null || newValue.trim().isEmpty()) {
      return;
    }

    features.editSingleEvent(
        eventName.trim(),
        selectedDate.atTime(10, 0),
        selectedDate.atTime(11, 0),
        property,
        newValue.trim()
    );
  }


  /**
   * Displays the dialog for exporting the calendar contents.
   */
  private void showExportDialog() {
    if (features == null) {
      return;
    }

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

      if (choice == 0) {

        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.setFileFilter(filter);
      } else {

        FileNameExtensionFilter filter = new FileNameExtensionFilter("iCal Files (*.ical)", "ical");
        fileChooser.setFileFilter(filter);
      }

      if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        String filePath = file.getAbsolutePath();

        if (choice == 0) {
          if (!filePath.toLowerCase().endsWith(".csv")) {
            filePath += ".csv";
          }
          features.exportToCsv(filePath);
        } else {

          if (!filePath.toLowerCase().endsWith(".ical")) {
            filePath += ".ical";
          }
          features.exportToIcal(filePath);
        }
      }
    }
  }


  /**
   * Sets the names of the available calendars in the selector dropdown.
   *
   * @param names The list of calendar names.
   */
  public void setCalendarNames(List<String> names) {
    calendarSelector.removeAllItems();
    for (String name : names) {
      calendarSelector.addItem(name);
    }
  }

  /**
   * Sets the currently selected calendar in the dropdown.
   *
   * @param name The name of the calendar to select.
   */
  public void setSelectedCalendar(String name) {
    calendarSelector.setSelectedItem(name);
  }

  /**
   * Displays the list of events for a specific date in the event panel.
   *
   * @param events The list of events to display (can be null or empty).
   * @param date   The date associated with these events.
   */
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

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 5, 8, 5));
        wrapper.add(eventPanel, BorderLayout.CENTER);

        eventContainer.add(wrapper);
      }
    }

    eventContainer.revalidate();
    eventContainer.repaint();
  }

  /**
   * Displays an error message to the user via a warning modal popup.
   *
   * @param message The error description.
   */
  public void showError(String message) {
    JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays a success message to the user, typically as an informational modal popup.
   *
   * @param message The success message to display.
   */
  public void showSuccess(String message) {
    JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Makes the main application frame visible to the user.
   */
  public void display() {
    frame.setVisible(true);
  }

  /**
   * Gets the main application frame.
   *
   * @return the main JFrame.
   */
  public JFrame getFrame() {
    return frame;
  }

  /**
   * Displays the dialog for editing the selected calendar's properties (name or timezone).
   */
  private void showEditCalendarDialog() {
    if (features == null) {
      return;
    }

    String currentCalendarName = (String) calendarSelector.getSelectedItem();
    if (currentCalendarName == null) {
      showError("Please create or select a calendar first.");
      return;
    }

    String[] options = {"Rename", "Change Timezone"};
    int choice = JOptionPane.showOptionDialog(
        frame,
        "What would you like to edit for '" + currentCalendarName + "'?",
        "Edit Calendar",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]
    );

    if (choice == 0) {
      String newName = JOptionPane.showInputDialog(frame,
          "Enter the new name for the calendar:", currentCalendarName);

      if (newName != null && !newName.trim().isEmpty()
          && !newName.trim().equals(currentCalendarName)) {
        features.editCalendarName(currentCalendarName, newName.trim());

      }
    } else if (choice == 1) {
      String newTimezone = (String) JOptionPane.showInputDialog(frame,
          "Select the new timezone:", "Change Timezone", JOptionPane.QUESTION_MESSAGE,
          null, AVAILABLE_TIMEZONES, AVAILABLE_TIMEZONES[0]);

      if (newTimezone != null) {
        features.editCalendarTimezone(currentCalendarName, newTimezone);
      }
    }
  }

  /**
   * Getter for the currently selected date in the view.
   * Required for the controller to refresh the event panel based on context.
   *
   * @return the currently selected date.
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Updates the visible timezone label.
   *
   * @param timezoneId the ID of the timezone to display.
   */
  public void updateTimezoneDisplay(String timezoneId) {
    if (timezoneLabel != null) {
      timezoneLabel.setText("Current Timezone: " + timezoneId);
    }
  }

  /**
   * Returns the YearMonth object representing the month currently displayed.
   *
   * @return the current YearMonth context.
   */
  public YearMonth getCurrentDisplayContext() {
    return currentMonth;
  }

}