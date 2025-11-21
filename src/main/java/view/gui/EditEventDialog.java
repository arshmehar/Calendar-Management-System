package view.gui;

import model.Event;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Dialog for editing event properties with dynamic input fields.
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

  private static final String CARD_TEXT = "TEXT";
  private static final String CARD_DATETIME = "DATETIME";
  private static final String CARD_STATUS = "STATUS";

  public EditEventDialog(JFrame parent, Event event, String editScope) {
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

    // Header
    JLabel infoLabel = new JLabel("Editing: " + event.getSubject());
    infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
    mainPanel.add(infoLabel, BorderLayout.NORTH);

    // Center Input Area
    JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));

    // Property Selector
    JPanel propPanel = new JPanel(new BorderLayout(10, 10));
    propPanel.add(new JLabel("Property:"), BorderLayout.WEST);
    propertyCombo = new JComboBox<>(new String[]{"name", "description", "location", "start", "end", "status"});
    propertyCombo.addActionListener(e -> updateInputView());
    propPanel.add(propertyCombo, BorderLayout.CENTER);
    centerPanel.add(propPanel);

    // Dynamic Inputs
    cardLayout = new CardLayout();
    inputContainer = new JPanel(cardLayout);

    // 1. Text
    textField = new JTextField();
    inputContainer.add(textField, CARD_TEXT);

    // 2. DateTime
    JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    dateSpinner = new JSpinner(new SpinnerDateModel());
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
    timeSpinner = new JSpinner(new SpinnerDateModel());
    timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
    dateTimePanel.add(new JLabel("Date:"));
    dateTimePanel.add(dateSpinner);
    dateTimePanel.add(new JLabel(" Time:"));
    dateTimePanel.add(timeSpinner);
    inputContainer.add(dateTimePanel, CARD_DATETIME);

    // 3. Status
    statusCombo = new JComboBox<>(new String[]{"PUBLIC", "PRIVATE", "CONFIDENTIAL", "TENTATIVE"});
    inputContainer.add(statusCombo, CARD_STATUS);

    centerPanel.add(inputContainer);
    mainPanel.add(centerPanel, BorderLayout.CENTER);

    // Buttons
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveBtn = new JButton("Save");
    saveBtn.addActionListener(e -> { confirmed = true; dispose(); });
    JButton cancelBtn = new JButton("Cancel");
    cancelBtn.addActionListener(e -> dispose());
    btnPanel.add(saveBtn);
    btnPanel.add(cancelBtn);
    mainPanel.add(btnPanel, BorderLayout.SOUTH);

    add(mainPanel);
    updateInputView();
  }

  private void updateInputView() {
    String selected = (String) propertyCombo.getSelectedItem();
    if (selected == null) return;

    switch (selected) {
      case "start":
      case "end":
        LocalDateTime val = selected.equals("start") ? event.getStart() : event.getEnd();
        Date date = Date.from(val.atZone(ZoneId.systemDefault()).toInstant());
        dateSpinner.setValue(date);
        timeSpinner.setValue(date);
        cardLayout.show(inputContainer, CARD_DATETIME);
        break;
      case "status":
        if (event.getStatus() != null) statusCombo.setSelectedItem(event.getStatus().toString().toUpperCase());
        cardLayout.show(inputContainer, CARD_STATUS);
        break;
      default:
        String txt = "";
        if (selected.equals("name")) txt = event.getSubject();
        else if (selected.equals("location")) txt = event.getLocation();
        else if (selected.equals("description")) txt = event.getDescription();
        textField.setText(txt);
        cardLayout.show(inputContainer, CARD_TEXT);
        break;
    }
  }

  public boolean isConfirmed() { return confirmed; }
  public String getProperty() { return (String) propertyCombo.getSelectedItem(); }
  public String getNewValue() {
    String prop = (String) propertyCombo.getSelectedItem();
    if ("start".equals(prop) || "end".equals(prop)) {
      Date d = (Date) dateSpinner.getValue();
      Date t = (Date) timeSpinner.getValue();
      LocalDateTime ldt = LocalDateTime.of(
          d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
          t.toInstant().atZone(ZoneId.systemDefault()).toLocalTime());
      return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } else if ("status".equals(prop)) {
      return (String) statusCombo.getSelectedItem();
    }
    return textField.getText().trim();
  }
}