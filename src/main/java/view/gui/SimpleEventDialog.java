// view/gui/SimpleEventDialog.java
package view.gui;

import model.Event;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Simple dialog for creating/editing events.
 */
public class SimpleEventDialog extends JDialog {
  private JTextField subjectField;
  private JSpinner startHourSpinner;
  private JSpinner startMinuteSpinner;
  private JSpinner endHourSpinner;
  private JSpinner endMinuteSpinner;
  private boolean confirmed = false;
  private final LocalDate date;

  public SimpleEventDialog(JFrame parent, LocalDate date) {
    this(parent, date, null);
  }

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

  private boolean validateEnd() {
    if (subjectField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please enter event name");
      return false;
    }

    LocalTime start = LocalTime.of((int)startHourSpinner.getValue(),
        (int)startMinuteSpinner.getValue());
    LocalTime end = LocalTime.of((int)endHourSpinner.getValue(),
        (int)endMinuteSpinner.getValue());

    if (!end.isAfter(start)) {
      JOptionPane.showMessageDialog(this, "End time must be after start time");
      return false;
    }

    return true;
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getSubject() {
    return subjectField.getText().trim();
  }

  public LocalDateTime getStartDateTime() {
    return LocalDateTime.of(date,
        LocalTime.of((int)startHourSpinner.getValue(), (int)startMinuteSpinner.getValue()));
  }

  public LocalDateTime getEndDateTime() {
    return LocalDateTime.of(date,
        LocalTime.of((int)endHourSpinner.getValue(), (int)endMinuteSpinner.getValue()));
  }
}