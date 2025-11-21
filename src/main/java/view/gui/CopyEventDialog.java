package view.gui;

import controller.features.CalendarFeatures;
import model.Event;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Dialog for copying a single event to another calendar and time.
 */
public class CopyEventDialog extends JDialog {
  private final Event event;
  private final CalendarFeatures features;
  private final List<String> availableCalendars;

  private JComboBox<String> targetCalendarCombo;
  private JSpinner dateSpinner;
  private JSpinner timeSpinner;

  public CopyEventDialog(JFrame parent, Event event, CalendarFeatures features, List<String> availableCalendars) {
    super(parent, "Copy Event", true);
    this.event = event;
    this.features = features;
    this.availableCalendars = availableCalendars;

    setSize(450, 250);
    setLocationRelativeTo(parent);
    setResizable(false);

    initComponents();
  }

  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Info Header
    JLabel infoLabel = new JLabel("Copying: " + event.getSubject());
    infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
    mainPanel.add(infoLabel, BorderLayout.NORTH);

    // Input Panel
    JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));

    // 1. Target Calendar Selector
    inputPanel.add(new JLabel("Target Calendar:"));
    targetCalendarCombo = new JComboBox<>(availableCalendars.toArray(new String[0]));
    inputPanel.add(targetCalendarCombo);

    // 2. Target Date/Time Selector
    inputPanel.add(new JLabel("Target Start Time:"));

    JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    // Date Spinner
    dateSpinner = new JSpinner(new SpinnerDateModel());
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
    // Set default to event's current start date
    Date startDate = Date.from(event.getStart().atZone(ZoneId.systemDefault()).toInstant());
    dateSpinner.setValue(startDate);

    // Time Spinner
    timeSpinner = new JSpinner(new SpinnerDateModel());
    timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
    timeSpinner.setValue(startDate);

    dateTimePanel.add(dateSpinner);
    dateTimePanel.add(Box.createHorizontalStrut(5));
    dateTimePanel.add(timeSpinner);

    inputPanel.add(dateTimePanel);

    mainPanel.add(inputPanel, BorderLayout.CENTER);

    // Button Panel
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
    if (targetCalendar == null) {
      JOptionPane.showMessageDialog(this, "Please select a target calendar.");
      return;
    }

    // Combine Date and Time spinners into LocalDateTime
    Date datePart = (Date) dateSpinner.getValue();
    Date timePart = (Date) timeSpinner.getValue();

    LocalDateTime targetDateTime = LocalDateTime.of(
        datePart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
        timePart.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
    );

    features.copySingleEvent(
        event.getSubject(),
        event.getStart(),
        targetDateTime,
        targetCalendar
    );

    dispose();
  }
}