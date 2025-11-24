package view.gui;

import controller.DayOfWeekAlphabet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;

/**
 * Dialog for creating single or recurring events.
 * This modal dialog collects all necessary input fields for event definition,
 * including subject, time, and recurrence options.
 */
public class EventCreationDialog extends JDialog {
  private final LocalDate selectedDate;
  private final boolean recurringMode;
  private boolean confirmed = false;

  private JTextField subjectField;
  private JSpinner startHourSpinner;
  private JSpinner startMinuteSpinner;
  private JSpinner endHourSpinner;
  private JSpinner endMinuteSpinner;

  private JCheckBox[] weekdayCheckboxes;
  private JRadioButton occurrencesRadio;
  private JRadioButton untilDateRadio;
  private JSpinner occurrencesSpinner;
  private JSpinner untilDateSpinner;

  /**
   * Constructs an EventCreationDialog.
   *
   * @param parent The parent frame for the dialog.
   * @param date The date initially selected in the calendar grid.
   * @param showRecurringOptions True to show recurrence options, false for a single event.
   */
  public EventCreationDialog(JFrame parent, LocalDate date, boolean showRecurringOptions) {
    super(parent, showRecurringOptions ? "Create Recurring Event" : "Create Event", true);
    this.selectedDate = date;
    this.recurringMode = showRecurringOptions;

    setSize(500, showRecurringOptions ? 520 : 300);
    setLocationRelativeTo(parent);
    setResizable(false);

    initComponents();
  }

  /**
   * Initializes and lays out all components of the dialog.
   */
  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

    mainPanel.add(createBasicFieldsPanel(), BorderLayout.NORTH);

    if (recurringMode) {
      mainPanel.add(createRecurringPanel(), BorderLayout.CENTER);
    }

    mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

    add(mainPanel);
  }


  /**
   * Creates the panel containing basic event fields: subject, start time, and end time.
   *
   * @return A JPanel containing the basic event input fields.
   */
  private JPanel createBasicFieldsPanel() {
    JPanel panel = new JPanel(new GridLayout(3, 2, 10, 15));

    panel.add(new JLabel("Event Name:"));
    subjectField = new JTextField();
    panel.add(subjectField);

    panel.add(new JLabel("Start Time (HH:MM):"));
    JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    startHourSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
    startMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
    startPanel.add(startHourSpinner);
    startPanel.add(new JLabel(":"));
    startPanel.add(startMinuteSpinner);
    panel.add(startPanel);

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

  /**
   * Creates the panel containing recurrence options (weekdays and end conditions).
   * This panel is only visible if the dialog is in recurring mode.
   *
   * @return A JPanel containing recurrence input fields.
   */
  private JPanel createRecurringPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder("Recurrence Options"));

    JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    daysPanel.add(new JLabel("Repeat on:"));

    String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    weekdayCheckboxes = new JCheckBox[7];

    for (int i = 0; i < 7; i++) {
      weekdayCheckboxes[i] = new JCheckBox(dayNames[i]);
      daysPanel.add(weekdayCheckboxes[i]);
    }

    panel.add(daysPanel);
    panel.add(Box.createVerticalStrut(10));

    JPanel endConditionPanel = new JPanel();
    endConditionPanel.setLayout(new BoxLayout(endConditionPanel, BoxLayout.Y_AXIS));
    ButtonGroup group = new ButtonGroup();

    occurrencesRadio = new JRadioButton("Ends after:");
    occurrencesRadio.setSelected(true);
    group.add(occurrencesRadio);

    JPanel occPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    occPanel.add(occurrencesRadio);

    occurrencesSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
    occPanel.add(occurrencesSpinner);
    occPanel.add(new JLabel("occurrences"));
    endConditionPanel.add(occPanel);

    JPanel untilPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    untilDateRadio = new JRadioButton("Ends by:");
    group.add(untilDateRadio);
    untilPanel.add(untilDateRadio);

    SpinnerDateModel dateModel = new SpinnerDateModel();
    untilDateSpinner = new JSpinner(dateModel);
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(untilDateSpinner, "MM/dd/yyyy");
    untilDateSpinner.setEditor(dateEditor);

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, 1);
    untilDateSpinner.setValue(cal.getTime());

    untilPanel.add(untilDateSpinner);
    endConditionPanel.add(untilPanel);

    panel.add(endConditionPanel);

    return panel;
  }

  /**
   * Creates the panel containing the Create and Cancel buttons.
   *
   * @return A JPanel containing the action buttons.
   */
  private JPanel createButtonPanel() {

    JButton createButton = new JButton("Create");
    createButton.setPreferredSize(new Dimension(90, 30));
    createButton.addActionListener(e -> onCreateClicked());

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setPreferredSize(new Dimension(90, 30));
    cancelButton.addActionListener(e -> onCancelClicked());

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    panel.add(createButton);
    panel.add(cancelButton);

    return panel;
  }

  /**
   * Handles the action when the Create button is clicked.
   * Validates input and closes the dialog upon success.
   */
  private void onCreateClicked() {
    if (validation()) {
      confirmed = true;
      dispose();
    }
  }

  /**
   * Handles the action when the Cancel button is clicked or the dialog is closed.
   */
  private void onCancelClicked() {
    confirmed = false;
    dispose();
  }

  /**
   * Performs validation checks on all required fields.
   * Checks for empty subject, valid time range (end after start), and
   * at least one weekday selected for recurring events.
   *
   * @return true if all input is valid, false otherwise (displays JOptionPane error).
   */
  private boolean validation() {

    if (subjectField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Please enter an event name",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      subjectField.requestFocus();
      return false;
    }

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


  /**
   * Checks if the user confirmed the action by clicking the Create button.
   *
   * @return true if confirmed, false otherwise.
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Gets the subject/name of the event entered by the user.
   *
   * @return The trimmed subject string.
   */
  public String getSubject() {
    return subjectField.getText().trim();
  }

  /**
   * Combines the selected date with the entered start time components.
   *
   * @return The full start date and time as a LocalDateTime object.
   */
  public LocalDateTime getStartDateTime() {
    int hour = (int) startHourSpinner.getValue();
    int minute = (int) startMinuteSpinner.getValue();
    return LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
  }

  /**
   * Combines the selected date with the entered end time components.
   *
   * @return The full end date and time as a LocalDateTime object.
   */
  public LocalDateTime getEndDateTime() {
    int hour = (int) endHourSpinner.getValue();
    int minute = (int) endMinuteSpinner.getValue();
    return LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
  }

  /**
   * Gets the set of weekdays selected for a recurring event.
   *
   * @return A Set of DayOfWeekAlphabet enums.
   */
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

  /**
   * Checks if the recurrence end condition is based on a number of occurrences.
   *
   * @return true if 'After (N occurrences)' is selected, false if 'Until (date)' is selected.
   */
  public boolean isOccurrencesBased() {
    return occurrencesRadio != null && occurrencesRadio.isSelected();
  }

  /**
   * Gets the number of occurrences specified for the recurrence end condition.
   *
   * @return The integer number of occurrences.
   */
  public int getOccurrences() {
    return (Integer) occurrencesSpinner.getValue();
  }

  /**
   * Gets the end date specified for the recurrence end condition (for Until mode).
   *
   * @return The end date as a LocalDate object.
   */
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