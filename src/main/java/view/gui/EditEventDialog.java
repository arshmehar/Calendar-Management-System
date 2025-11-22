package view.gui;

import java.time.LocalDate;
import model.Event;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * Dialog for editing event properties with dynamic input fields.
 * Switches between Text, Date/Time, and Dropdown inputs based on selection.
 */
public class EditEventDialog extends JDialog {
  private final Event event;
  private final String editScope;
  private boolean confirmed = false;

  // UI Components
  private JComboBox<String> propertyCombo;
  private JPanel inputContainer;
  private CardLayout cardLayout;

  // Input Fields
  private JTextField textField;
  private JSpinner dateSpinner;
  private JSpinner timeSpinner;
  private JComboBox<String> statusCombo;

  // Card Identifiers
  private static final String CARD_TEXT = "TEXT";
  private static final String CARD_DATETIME = "DATETIME";
  private static final String CARD_STATUS = "STATUS";

  public EditEventDialog(JFrame parent, Event event, LocalDate displayDate, String editScope) {
    super(parent, "Edit Event", true);
    this.event = event;
    this.editScope = editScope;

    setSize(500, 250);
    setLocationRelativeTo(parent);
    setResizable(false);

    initComponents();
  }

  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // 1. Info Header
    String scopeText = switch (editScope) {
      case "single" -> "this event";
      case "fromDate" -> "this and future events";
      default -> "all events in series";
    };
    JLabel infoLabel = new JLabel("Editing " + scopeText + ": " + event.getSubject());
    infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
    mainPanel.add(infoLabel, BorderLayout.NORTH);

    // 2. Input Panel Container
    JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));

    // Property Selector
    JPanel propPanel = new JPanel(new BorderLayout(10, 10));
    propPanel.add(new JLabel("Property to edit:"), BorderLayout.WEST);

    // Note: Keys must match what the controller expects ("subject" -> "name") or be mapped later
    // For simplicity here, we use display names and map them in getters if needed,
    // or use the lowercase names the controller likely uses.
    String[] properties = {"name", "description", "location", "start", "end", "status"};
    propertyCombo = new JComboBox<>(properties);
    propertyCombo.addActionListener(e -> updateInputView());
    propPanel.add(propertyCombo, BorderLayout.CENTER);

    centerPanel.add(propPanel);

    // Dynamic Input Area (CardLayout)
    cardLayout = new CardLayout();
    inputContainer = new JPanel(cardLayout);

    // --- Card 1: Text Input ---
    JPanel textPanel = new JPanel(new BorderLayout());
    textField = new JTextField();
    textPanel.add(textField, BorderLayout.CENTER);
    inputContainer.add(textPanel, CARD_TEXT);

    // --- Card 2: Date/Time Input ---
    JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    dateSpinner = new JSpinner(new SpinnerDateModel());
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

    timeSpinner = new JSpinner(new SpinnerDateModel());
    timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));

    dateTimePanel.add(new JLabel("Date:"));
    dateTimePanel.add(dateSpinner);
    dateTimePanel.add(Box.createHorizontalStrut(10));
    dateTimePanel.add(new JLabel("Time:"));
    dateTimePanel.add(timeSpinner);

    inputContainer.add(dateTimePanel, CARD_DATETIME);

    // --- Card 3: Status Input ---
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusCombo = new JComboBox<>(new String[]{"PUBLIC", "PRIVATE"});
    statusPanel.add(statusCombo, BorderLayout.CENTER);
    inputContainer.add(statusPanel, CARD_STATUS);

    centerPanel.add(inputContainer);
    mainPanel.add(centerPanel, BorderLayout.CENTER);

    // 3. Buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> {
      if (validation()) {
        confirmed = true;
        dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    add(mainPanel);

    // Initialize the view based on the default selection
    updateInputView();
  }

  /**
   * Updates the visible input field based on the selected property
   * and pre-fills it with the current event data.
   */
  private void updateInputView() {
    String selected = (String) propertyCombo.getSelectedItem();
    if (selected == null) return;

    switch (selected) {
      case "start":
      case "end":
        LocalDateTime timeToSet = selected.equals("start") ? event.getStart() : event.getEnd();
        Date date = Date.from(timeToSet.atZone(ZoneId.systemDefault()).toInstant());
        dateSpinner.setValue(date);
        timeSpinner.setValue(date);
        cardLayout.show(inputContainer, CARD_DATETIME);
        break;

      case "status":
        String currentStatus = (event.getStatus() != null) ? event.getStatus().toString().toUpperCase() : "PUBLIC";
        statusCombo.setSelectedItem(currentStatus);
        cardLayout.show(inputContainer, CARD_STATUS);
        break;

      default: // name, location, description
        String textVal = "";
        if (selected.equals("name")) textVal = event.getSubject();
        else if (selected.equals("location")) textVal = event.getLocation();
        else if (selected.equals("description")) textVal = event.getDescription();

        textField.setText(textVal != null ? textVal : "");
        cardLayout.show(inputContainer, CARD_TEXT);
        break;
    }
  }

  private boolean validation() {
    String prop = (String) propertyCombo.getSelectedItem();

    // Basic empty check for text fields
    if (inputContainer.getComponent(0).isVisible() && textField.getText().trim().isEmpty()) {
      // Allow empty description/location, but maybe not name?
      if (Objects.equals(prop, "name")) {
        JOptionPane.showMessageDialog(this, "Event name cannot be empty.");
        return false;
      }
    }

    // Date validation could go here (e.g. start < end),
    // but might be complex if we are only editing one property at a time.
    // We leave logic validation to the controller/model.

    return true;
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getProperty() {
    return (String) propertyCombo.getSelectedItem();
  }

  public String getNewValue() {
    String prop = (String) propertyCombo.getSelectedItem();
    if (prop == null) return "";

    switch (prop) {
      case "start":
      case "end":
        // Combine Date and Time spinners into ISO format string
        Date d = (Date) dateSpinner.getValue();
        Date t = (Date) timeSpinner.getValue();

        LocalDateTime ldt = LocalDateTime.of(
            d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            t.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
        );
        // Return in format parsable by your command parser (usually ISO)
        return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

      case "status":
        return (String) statusCombo.getSelectedItem();

      default:
        return textField.getText().trim();
    }
  }
}