// view/gui/CopyEventDialog.java
package view.gui;

import controller.features.CalendarFeatures;
import model.Event;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Dialog for copying events to another calendar/date.
 */
public class CopyEventDialog extends JDialog {
  private final Event event;
  private final LocalDate sourceDate;
  private final CalendarFeatures features;

  private JComboBox<String> targetCalendarCombo;
  private JSpinner targetDateSpinner;
  private JSpinner targetHourSpinner;
  private JSpinner targetMinuteSpinner;

  public CopyEventDialog(JFrame parent, Event event, LocalDate sourceDate,
                         CalendarFeatures features) {
    super(parent, "Copy Event", true);
    this.event = event;
    this.sourceDate = sourceDate;
    this.features = features;

    setSize(450, 250);
    setLocationRelativeTo(parent);

    initComponents();
  }

  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Info
    JLabel infoLabel = new JLabel("Copy: " + event.getSubject());
    infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
    mainPanel.add(infoLabel, BorderLayout.NORTH);

    // Input panel
    JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));

    // Target calendar
    inputPanel.add(new JLabel("Target Calendar:"));
    targetCalendarCombo = new JComboBox<>();
    // This should be populated from controller - simplified here
    targetCalendarCombo.addItem("Default");
    inputPanel.add(targetCalendarCombo);

    // Target date
    inputPanel.add(new JLabel("Target Date (days from now):"));
    targetDateSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 365, 1));
    inputPanel.add(targetDateSpinner);

    // Target time
    inputPanel.add(new JLabel("Target Time (HH:MM):"));
    JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    targetHourSpinner = new JSpinner(new SpinnerNumberModel(
        event.getStart().getHour(), 0, 23, 1));
    targetMinuteSpinner = new JSpinner(new SpinnerNumberModel(
        event.getStart().getMinute(), 0, 59, 15));
    timePanel.add(targetHourSpinner);
    timePanel.add(new JLabel(":"));
    timePanel.add(targetMinuteSpinner);
    inputPanel.add(timePanel);

    mainPanel.add(inputPanel, BorderLayout.CENTER);

    // Buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

    JButton copyButton = new JButton("Copy");
    copyButton.addActionListener(e -> onCopyClicked());

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(copyButton);
    buttonPanel.add(cancelButton);

    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    add(mainPanel);
  }

  private void onCopyClicked() {
    String targetCalendar = (String) targetCalendarCombo.getSelectedItem();
    int daysOffset = (int) targetDateSpinner.getValue();
    LocalDate targetDate = sourceDate.plusDays(daysOffset);

    int targetHour = (int) targetHourSpinner.getValue();
    int targetMinute = (int) targetMinuteSpinner.getValue();
    LocalDateTime targetDateTime = LocalDateTime.of(targetDate,
        LocalTime.of(targetHour, targetMinute));

    features.copySingleEvent(
        event.getSubject(),
        event.getStart(),
        targetDateTime,
        targetCalendar
    );

    JOptionPane.showMessageDialog(this,
        "Event copied successfully!",
        "Success",
        JOptionPane.INFORMATION_MESSAGE);

    dispose();
  }
}