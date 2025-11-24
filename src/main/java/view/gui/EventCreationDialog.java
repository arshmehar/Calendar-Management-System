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

public class EventCreationDialog extends JDialog {
  private final LocalDate selectedDate;
  private final boolean recurringMode;
  private boolean confirmed = false;

  private JTextField subjectField;
  private JSpinner startHourSpinner, startMinuteSpinner, endHourSpinner, endMinuteSpinner;
  private JCheckBox[] weekdayCheckboxes;
  private JRadioButton occurrencesRadio, untilDateRadio;
  private JSpinner occurrencesSpinner, untilDateSpinner;

  public EventCreationDialog(JFrame parent, LocalDate date, boolean showRecurringOptions) {
    super(parent, showRecurringOptions ? "Create Recurring Event" : "Create Event", true);
    this.selectedDate = date;
    this.recurringMode = showRecurringOptions;
    setSize(500, showRecurringOptions ? 520 : 300); // Increased height for breathing room
    setLocationRelativeTo(parent);
    setResizable(false);
    initComponents();
  }

  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(15, 15)); // Gaps
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25)); // Padding

    mainPanel.add(createBasicFieldsPanel(), BorderLayout.NORTH);
    if (recurringMode) {
      mainPanel.add(createRecurringPanel(), BorderLayout.CENTER);
    }
    mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
    add(mainPanel);
  }

  private JPanel createBasicFieldsPanel() {
    JPanel panel = new JPanel(new GridLayout(3, 2, 10, 15)); // Vertical gap 15

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
    untilDateSpinner.setEditor(new JSpinner.DateEditor(untilDateSpinner, "MM/dd/yyyy"));
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, 1);
    untilDateSpinner.setValue(cal.getTime());
    untilPanel.add(untilDateSpinner);

    endConditionPanel.add(untilPanel);
    panel.add(endConditionPanel);

    return panel;
  }

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
    if (subjectField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please enter an event name", "Missing Info", JOptionPane.WARNING_MESSAGE);
      subjectField.requestFocus();
      return false;
    }
    int startH = (int) startHourSpinner.getValue();
    int startM = (int) startMinuteSpinner.getValue();
    int endH = (int) endHourSpinner.getValue();
    int endM = (int) endMinuteSpinner.getValue();
    if (!LocalTime.of(endH, endM).isAfter(LocalTime.of(startH, startM))) {
      JOptionPane.showMessageDialog(this, "End time must be after start time", "Invalid Time", JOptionPane.WARNING_MESSAGE);
      return false;
    }
    if (recurringMode) {
      boolean daySelected = false;
      for (JCheckBox box : weekdayCheckboxes) if (box.isSelected()) daySelected = true;
      if (!daySelected) {
        JOptionPane.showMessageDialog(this, "Select at least one day", "Missing Info", JOptionPane.WARNING_MESSAGE);
        return false;
      }
    }
    return true;
  }

  public boolean isConfirmed() { return confirmed; }
  public String getSubject() { return subjectField.getText().trim(); }
  public LocalDateTime getStartDateTime() {
    return LocalDateTime.of(selectedDate, LocalTime.of((int)startHourSpinner.getValue(), (int)startMinuteSpinner.getValue()));
  }
  public LocalDateTime getEndDateTime() {
    return LocalDateTime.of(selectedDate, LocalTime.of((int)endHourSpinner.getValue(), (int)endMinuteSpinner.getValue()));
  }
  public Set<DayOfWeekAlphabet> getSelectedWeekdays() {
    Set<DayOfWeekAlphabet> weekdays = new HashSet<>();
    DayOfWeekAlphabet[] vals = {DayOfWeekAlphabet.M, DayOfWeekAlphabet.T, DayOfWeekAlphabet.W, DayOfWeekAlphabet.R, DayOfWeekAlphabet.F, DayOfWeekAlphabet.S, DayOfWeekAlphabet.U};
    for(int i=0; i<7; i++) if(weekdayCheckboxes[i].isSelected()) weekdays.add(vals[i]);
    return weekdays;
  }
  public boolean isOccurrencesBased() { return occurrencesRadio != null && occurrencesRadio.isSelected(); }
  public int getOccurrences() { return (Integer) occurrencesSpinner.getValue(); }
  public LocalDate getUntilDate() {
    Calendar cal = Calendar.getInstance();
    cal.setTime((Date) untilDateSpinner.getValue());
    return LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));
  }
}