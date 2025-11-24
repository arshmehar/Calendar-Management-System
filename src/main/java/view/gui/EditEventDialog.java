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

  private static final String CARD_TEXT = "TEXT";
  private static final String CARD_DATETIME = "DATETIME";
  private static final String CARD_STATUS = "STATUS";

  private final Event event;
  private final String editScope;
  private boolean confirmed = false;

  private JComboBox<String> propertyCombo;
  private JPanel inputContainer;
  private CardLayout cardLayout;

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

    setSize(500, 280);
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
    infoLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    mainPanel.add(infoLabel, BorderLayout.NORTH);


    JPanel propPanel = new JPanel(new BorderLayout(10, 10));
    propPanel.add(new JLabel("Property to edit:"), BorderLayout.WEST);

    String[] properties = {"Subject", "Description", "Location", "Start", "End", "Status"};
    propertyCombo = new JComboBox<>(properties);
    propertyCombo.addActionListener(e -> updateInputView());
    propPanel.add(propertyCombo, BorderLayout.CENTER);

    JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 10));
    centerPanel.add(propPanel);

    cardLayout = new CardLayout();
    inputContainer = new JPanel(cardLayout);

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

    JPanel statusPanel = new JPanel(new BorderLayout());
    statusCombo = new JComboBox<>(new String[] {"PUBLIC", "PRIVATE"});
    statusPanel.add(statusCombo, BorderLayout.CENTER);
    inputContainer.add(statusPanel, CARD_STATUS);

    centerPanel.add(inputContainer);
    mainPanel.add(centerPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

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
    selected = selected.toLowerCase();

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

      default:
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

    if (prop == null) {
      return false;
    }

    prop =  prop.toLowerCase();

    if (inputContainer.getComponent(0).isVisible() && textField.getText().trim().isEmpty()) {
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
    String selected = (String) propertyCombo.getSelectedItem();
    if (selected != null) {
      return selected.toLowerCase();
    }
    return "";
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
    prop =  prop.toLowerCase();

    switch (prop) {
      case "start":
      case "end":
        Date d = (Date) dateSpinner.getValue();
        Date t = (Date) timeSpinner.getValue();

        LocalDateTime ldt = LocalDateTime.of(
            d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            t.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
        );

        System.out.println(ldt);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        System.out.println(ldt.format(formatter));
        return ldt.format(formatter);

      case "status":
        return (String) statusCombo.getSelectedItem();

      default:
        return textField.getText().trim();
    }
  }
}