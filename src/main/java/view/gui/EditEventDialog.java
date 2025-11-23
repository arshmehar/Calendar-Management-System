package view.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import model.Event;

/**
 * Dialog for editing event properties with dynamic input fields.
 * Switches between Text, Date/Time, and Dropdown inputs based on selection.
 */
public class EditEventDialog extends JDialog {
  // Card Identifiers
  private static final String CARD_TEXT = "TEXT";
  private static final String CARD_DATETIME = "DATETIME";
  private static final String CARD_STATUS = "STATUS";

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

  /**
   * Constructs an EditEventDialog.
   *
   * @param parent The parent JFrame for the dialog.
   * @param event The event object being edited.
   * @param displayDate The date currently displayed in the calendar (for context).
   * @param editScope The scope of the edit ("single", "fromDate", or "all").
   */
  public EditEventDialog(JFrame parent, Event event, LocalDate displayDate, String editScope) {
    super(parent, "Edit Event", true);
    this.event = event;
    this.editScope = editScope;

    setSize(500, 250);
    setLocationRelativeTo(parent);
    setResizable(false);

    initComponents();
  }

  /**
   * Initializes and lays out all components of the dialog.
   */
  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // 1. Info Header
    String scopeText;
    switch (editScope) {
      case "single":
        scopeText = "this event";
        break;
      case "fromDate":
        scopeText = "this and future events";
        break;
      default:
        scopeText = "all events in series";
        break;
    }

    JLabel infoLabel = new JLabel("Editing " + scopeText + ": " + event.getSubject());
    infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
    mainPanel.add(infoLabel, BorderLayout.NORTH);



    // Property Selector
    JPanel propPanel = new JPanel(new BorderLayout(10, 10));
    propPanel.add(new JLabel("Property to edit:"), BorderLayout.WEST);

    String[] properties = {"subject", "description", "location", "start", "end", "status"};
    propertyCombo = new JComboBox<>(properties);
    propertyCombo.addActionListener(e -> updateInputView());
    propPanel.add(propertyCombo, BorderLayout.CENTER);

    // 2. Input Panel Container
    JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    centerPanel.add(propPanel);

    // Dynamic Input Area (CardLayout)
    cardLayout = new CardLayout();
    inputContainer = new JPanel(cardLayout);

    // --- Card 1: Text Input ---
    JPanel textPanel = new JPanel(new BorderLayout());
    textField = new JTextField();
    textPanel.add(textField, BorderLayout.CENTER);
    inputContainer.add(textPanel, CARD_TEXT);



    dateSpinner = new JSpinner(new SpinnerDateModel());
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

    timeSpinner = new JSpinner(new SpinnerDateModel());
    timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));

    JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    dateTimePanel.add(new JLabel("Date:"));
    dateTimePanel.add(dateSpinner);
    dateTimePanel.add(Box.createHorizontalStrut(10));
    dateTimePanel.add(new JLabel("Time:"));
    dateTimePanel.add(timeSpinner);

    inputContainer.add(dateTimePanel, CARD_DATETIME);

    // --- Card 3: Status Input ---
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusCombo = new JComboBox<>(new String[] {"PUBLIC", "PRIVATE"});
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
    if (selected == null) {
      return;
    }

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
        String currentStatus =
            (event.getStatus() != null) ? event.getStatus().toString().toUpperCase() : "PUBLIC";
        statusCombo.setSelectedItem(currentStatus);
        cardLayout.show(inputContainer, CARD_STATUS);
        break;

      default: // name, location, description
        String textVal = "";
        if (selected.equals("subject")) {
          textVal = event.getSubject();
        } else if (selected.equals("location")) {
          textVal = event.getLocation();
        } else if (selected.equals("description")) {
          textVal = event.getDescription();
        }

        textField.setText(textVal != null ? textVal : "");
        cardLayout.show(inputContainer, CARD_TEXT);
        break;
    }
  }

  /**
   * Performs basic validation based on the active input type.
   * Currently checks if the subject/name field is empty.
   *
   * @return true if validation passes, false otherwise (displays JOptionPane error).
   */
  private boolean validation() {
    String prop = (String) propertyCombo.getSelectedItem();

    // Basic empty check for text fields
    if (inputContainer.getComponent(0).isVisible() && textField.getText().trim().isEmpty()) {
      // Check if the property being edited is subject/name
      if (Objects.equals(prop, "name") || Objects.equals(prop, "subject")) {
        JOptionPane.showMessageDialog(this, "Event name/subject cannot be empty.");
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if the dialog was confirmed by the user (Save clicked).
   *
   * @return true if confirmed, false if canceled or closed.
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Gets the name of the property selected for editing.
   *
   * @return The property name string.
   */
  public String getProperty() {
    return (String) propertyCombo.getSelectedItem();
  }

  /**
   * Gets the new value entered/selected by the user.
   * The format is adjusted based on the property type (ISO string for datetime, or text/status).
   *
   * @return The new property value as a String.
   */
  public String getNewValue() {
    String prop = (String) propertyCombo.getSelectedItem();
    if (prop == null) {
      return "";
    }

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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return ldt.format(formatter);

      case "status":
        return (String) statusCombo.getSelectedItem();

      default:
        return textField.getText().trim();
    }
  }
}