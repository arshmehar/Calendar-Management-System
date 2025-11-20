// view/gui/EditEventDialog.java
package view.gui;

import java.util.Objects;
import model.Event;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Dialog for editing event properties.
 */
public class EditEventDialog extends JDialog {
  private final Event event;
  private final LocalDate displayDate;
  private final String editScope;
  private boolean confirmed = false;

  private JComboBox<String> propertyCombo;
  private JTextField newValueField;

  public EditEventDialog(JFrame parent, Event event, LocalDate displayDate, String editScope) {
    super(parent, "Edit Event", true);
    this.event = event;
    this.displayDate = displayDate;
    this.editScope = editScope;

    setSize(450, 200);
    setLocationRelativeTo(parent);

    initComponents();
  }

  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Info label
    String scopeText = editScope.equals("single") ? "this event" :
        editScope.equals("fromDate") ? "this and future events" :
            "all events in series";
    JLabel infoLabel = new JLabel("Editing " + scopeText + ": " + event.getSubject());
    infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
    mainPanel.add(infoLabel, BorderLayout.NORTH);

    // Input panel
    JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));

    inputPanel.add(new JLabel("Property to edit:"));
    propertyCombo = new JComboBox<>(new String[]{
        "Subject", "Start", "End", "Location", "Description", "Status"
    });
    inputPanel.add(propertyCombo);

    inputPanel.add(new JLabel("New value:"));
    newValueField = new JTextField();
    inputPanel.add(newValueField);

    mainPanel.add(inputPanel, BorderLayout.CENTER);

    // Buttons
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
  }

  private boolean validation() {
    if (newValueField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Please enter a new value",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }
  if(Objects.equals(getProperty(), "Status")){
    if(!Objects.equals(getNewValue().toLowerCase(), "private" ) &&
        !Objects.equals(getNewValue().toLowerCase(), "public" )){
      JOptionPane.showMessageDialog(this,
          "Event Status should be Public or Private",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

    return true;
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getProperty() {
    return (String) propertyCombo.getSelectedItem();
  }

  public String getNewValue() {
    return newValueField.getText().trim();
  }
}