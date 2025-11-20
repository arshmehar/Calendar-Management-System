// view/gui/EventCreationDialog.java
package view.gui;

import controller.DayOfWeekAlphabet;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Dialog for creating single or recurring events.
 */
public class EventCreationDialog extends JDialog {
  private final LocalDate selectedDate;
  private final boolean recurringMode;
  private boolean confirmed = false;

  // Basic fields
  private JTextField subjectField;
  private JSpinner startHourSpinner;
  private JSpinner startMinuteSpinner;
  private JSpinner endHourSpinner;
  private JSpinner endMinuteSpinner;

  // Recurring fields
  private JCheckBox[] weekdayCheckboxes;
  private JRadioButton occurrencesRadio;
  private JRadioButton untilDateRadio;
  private JSpinner occurrencesSpinner;
  private JSpinner untilDateSpinner;

  public EventCreationDialog(JFrame parent, LocalDate date, boolean showRecurringOptions) {
    super(parent, showRecurringOptions ? "Create Recurring Event" : "Create Event", true);
    this.selectedDate = date;
    this.recurringMode = showRecurringOptions;

    setSize(500, showRecurringOptions ? 500 : 250);
    setLocationRelativeTo(parent);
    setResizable(false);

    initComponents();
  }

  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Basic event fields
    mainPanel.add(createBasicFieldsPanel(), BorderLayout.NORTH);

    // Recurring options (if enabled)
    if (recurringMode) {
      mainPanel.add(createRecurringPanel(), BorderLayout.CENTER);
    }

    // Buttons
    mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

    add(mainPanel);
  }

  private JPanel createBasicFieldsPanel() {
    JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

    // Subject
    panel.add(new JLabel("Event Name:"));
    subjectField = new JTextField();
    panel.add(subjectField);

    // Start time
    panel.add(new JLabel("Start Time (HH:MM):"));
    JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    startHourSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
    startMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
    startPanel.add(startHourSpinner);
    startPanel.add(new JLabel(":"));
    startPanel.add(startMinuteSpinner);
    panel.add(startPanel);

    // End time
    panel.add(new JLabel("End Time (HH:MM):"));
    JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    endHourSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 23, 1));
    endMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
    endPanel.add(endHourSpinner);
    endPanel.add(new JLabel(":"));
    endPanel.add(endMinuteSpinner);
    panel.add(endPanel);

    return panel;
  }

  private JPanel createRecurringPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder("Recurrence Options"));

    // Days of week
    JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    daysPanel.add(new JLabel("Repeat on:"));

    String[] dayLabels = {"M", "T", "W", "R", "F", "S", "U"};
    String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    weekdayCheckboxes = new JCheckBox[7];

    for (int i = 0; i < 7; i++) {
      weekdayCheckboxes[i] = new JCheckBox(dayNames[i]);
      daysPanel.add(weekdayCheckboxes[i]);
    }

    panel.add(daysPanel);
    panel.add(Box.createVerticalStrut(10));

    // End condition
    JPanel endConditionPanel = new JPanel();
    endConditionPanel.setLayout(new BoxLayout(endConditionPanel, BoxLayout.Y_AXIS));
    endConditionPanel.setBorder(BorderFactory.createTitledBorder("End Condition:"));

    ButtonGroup group = new ButtonGroup();

    // Occurrences option
    JPanel occPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    occurrencesRadio = new JRadioButton("After");
    occurrencesRadio.setSelected(true);
    group.add(occurrencesRadio);
    occPanel.add(occurrencesRadio);

    occurrencesSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
    occPanel.add(occurrencesSpinner);
    occPanel.add(new JLabel("occurrences"));
    endConditionPanel.add(occPanel);

    // Until date option
    JPanel untilPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    untilDateRadio = new JRadioButton("Until");
    group.add(untilDateRadio);
    untilPanel.add(untilDateRadio);

    SpinnerDateModel dateModel = new SpinnerDateModel();
    untilDateSpinner = new JSpinner(dateModel);
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(untilDateSpinner, "MM/dd/yyyy");
    untilDateSpinner.setEditor(dateEditor);

    // Set to 1 month from now
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, 1);
    untilDateSpinner.setValue(cal.getTime());

    untilPanel.add(untilDateSpinner);
    endConditionPanel.add(untilPanel);

    panel.add(endConditionPanel);

    return panel;
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

    JButton createButton = new JButton("Create");
    createButton.setPreferredSize(new Dimension(100, 30));
    createButton.addActionListener(e -> onCreateClicked());

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setPreferredSize(new Dimension(100, 30));
    cancelButton.addActionListener(e -> onCancelClicked());

    panel.add(createButton);
    panel.add(cancelButton);

    return panel;
  }

  private void onCreateClicked() {
    if (validation()) {
      confirmed = true;
      dispose();
    }
  }

  private void onCancelClicked() {
    confirmed = false;
    dispose();
  }

  private boolean validation() {
    // Validate subject
    if (subjectField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Please enter an event name",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      subjectField.requestFocus();
      return false;
    }

    // Validate times
    int startHour = (int) startHourSpinner.getValue();
    int startMinute = (int) startMinuteSpinner.getValue();
    int endHour = (int) endHourSpinner.getValue();
    int endMinute = (int) endMinuteSpinner.getValue();

    LocalTime start = LocalTime.of(startHour, startMinute);
    LocalTime end = LocalTime.of(endHour, endMinute);

    if (!end.isAfter(start)) {
      JOptionPane.showMessageDialog(this,
          "End time must be after start time",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      endHourSpinner.requestFocus();
      return false;
    }

    // Validate recurring options
    if (recurringMode) {
      boolean anyDaySelected = false;
      for (JCheckBox checkbox : weekdayCheckboxes) {
        if (checkbox.isSelected()) {
          anyDaySelected = true;
          break;
        }
      }

      if (!anyDaySelected) {
        JOptionPane.showMessageDialog(this,
            "Please select at least one day for recurring event",
            "Validation Error",
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }

    return true;
  }

  // Getters

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getSubject() {
    return subjectField.getText().trim();
  }

  public LocalDateTime getStartDateTime() {
    int hour = (int) startHourSpinner.getValue();
    int minute = (int) startMinuteSpinner.getValue();
    return LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
  }

  public LocalDateTime getEndDateTime() {
    int hour = (int) endHourSpinner.getValue();
    int minute = (int) endMinuteSpinner.getValue();
    return LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
  }

  public Set<DayOfWeekAlphabet> getSelectedWeekdays() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    DayOfWeekAlphabet[] dayValues = {
        DayOfWeekAlphabet.M,
        DayOfWeekAlphabet.T,
        DayOfWeekAlphabet.W,
        DayOfWeekAlphabet.R,
        DayOfWeekAlphabet.F,
        DayOfWeekAlphabet.S,
        DayOfWeekAlphabet.U
    };

    for (int i = 0; i < weekdayCheckboxes.length; i++) {
      if (weekdayCheckboxes[i].isSelected()) {
        weekdays.add(dayValues[i]);
      }
    }

    return weekdays;
  }

  public boolean isOccurrencesBased() {
    return occurrencesRadio != null && occurrencesRadio.isSelected();
  }

  public int getOccurrences() {
    return (Integer) occurrencesSpinner.getValue();
  }

  public LocalDate getUntilDate() {
    Date date = (Date) untilDateSpinner.getValue();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return LocalDate.of(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    );
  }
}