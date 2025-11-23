package view.gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import model.Event;

/**
 * Simple dialog for creating/editing events.
 * This dialog handles the subject and time components for a single event on a specific date.
 */
public class SimpleEventDialog extends JDialog {
  private final LocalDate date;
  private final JTextField subjectField;
  private final JSpinner startHourSpinner;
  private final JSpinner startMinuteSpinner; // <--- MISSING JAVADOC ADDED
  private final JSpinner endHourSpinner;
  private final JSpinner endMinuteSpinner; // <--- MISSING JAVADOC ADDED
  private boolean confirmed = false;

  /**
   * Constructs a SimpleEventDialog for creating a new event.
   *
   * @param parent The parent JFrame for the dialog.
   * @param date The date on which the event will be scheduled.
   */
  public SimpleEventDialog(JFrame parent, LocalDate date) {
    this(parent, date, null);
  }

  /**
   * Constructs a SimpleEventDialog.
   *
   * @param parent The parent JFrame for the dialog.
   * @param date The date on which the event is scheduled.
   * @param existingEvent The existing event to populate fields for editing, or null for new event.
   */
  public SimpleEventDialog(JFrame parent, LocalDate date, Event existingEvent) {
    super(parent, existingEvent == null ? "Create Event" : "Edit Event", true);
    this.date = date;
    setSize(400, 250);
    setLocationRelativeTo(parent);

    JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Subject
    panel.add(new JLabel("Event Name:"));
    subjectField = new JTextField();
    panel.add(subjectField);

    // Start time
    panel.add(new JLabel("Start Time (HH:MM):"));
    JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    startHourSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
    startMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
    startPanel.add(startHourSpinner);
    startPanel.add(new JLabel(":"));
    startPanel.add(startMinuteSpinner);
    panel.add(startPanel);

    // End time
    panel.add(new JLabel("End Time (HH:MM):"));
    JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    endHourSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 23, 1));
    endMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
    endPanel.add(endHourSpinner);
    endPanel.add(new JLabel(":"));
    endPanel.add(endMinuteSpinner);
    panel.add(endPanel);

    // Buttons
    panel.add(new JLabel(""));
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okButton = new JButton("OK");
    okButton.addActionListener(e -> {
      if (validateEnd()) {
        confirmed = true;
        dispose();
      }
    });
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    panel.add(buttonPanel);

    add(panel);

    // Populate if editing
    if (existingEvent != null) {
      subjectField.setText(existingEvent.getSubject());
      startHourSpinner.setValue(existingEvent.getStart().getHour());
      startMinuteSpinner.setValue(existingEvent.getStart().getMinute());
      endHourSpinner.setValue(existingEvent.getEnd().getHour());
      endMinuteSpinner.setValue(existingEvent.getEnd().getMinute());
    }
  }

  /**
   * Validates that the subject is not empty and the end time is after the start time.
   *
   * @return true if valid, false otherwise (displays JOptionPane error).
   */
  private boolean validateEnd() {
    if (subjectField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please enter event name");
      return false;
    }

    LocalTime start = LocalTime.of((int) startHourSpinner.getValue(),
        (int) startMinuteSpinner.getValue());
    LocalTime end = LocalTime.of((int) endHourSpinner.getValue(),
        (int) endMinuteSpinner.getValue());

    if (!end.isAfter(start)) {
      JOptionPane.showMessageDialog(this, "End time must be after start time");
      return false;
    }

    return true;
  }

  /**
   * Checks if the dialog was confirmed by the user (OK clicked).
   *
   * @return true if confirmed, false if canceled or closed.
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Gets the subject text entered by the user.
   *
   * @return The trimmed subject string.
   */
  public String getSubject() {
    return subjectField.getText().trim();
  }

  /**
   * Combines the selected date with the entered start time.
   *
   * @return The full start date and time as a LocalDateTime object.
   */
  public LocalDateTime getStartDateTime() {
    return LocalDateTime.of(date,
        LocalTime.of((int) startHourSpinner.getValue(), (int) startMinuteSpinner.getValue()));
  }

  /**
   * Combines the selected date with the entered end time.
   *
   * @return The full end date and time as a LocalDateTime object.
   */
  public LocalDateTime getEndDateTime() {
    return LocalDateTime.of(date,
        LocalTime.of((int) endHourSpinner.getValue(), (int) endMinuteSpinner.getValue()));
  }
}