package view.gui;

import controller.features.CalendarFeatures;
import model.Event;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Represents a single event in the UI.
 * Includes Edit and Copy buttons.
 */
public class EventPanel extends JPanel {
  private final Event event;
  private final CalendarFeatures features;
  private final List<String> availableCalendars;

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  public EventPanel(Event event, CalendarFeatures features, List<String> availableCalendars) {
    this.event = event;
    this.features = features;
    this.availableCalendars = availableCalendars;

    setupUI();
  }

  private void setupUI() {
    this.setLayout(new BorderLayout(5, 5));
    this.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    this.setBackground(new Color(245, 245, 250));
    this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

    // Left: Event Info
    JPanel infoPanel = new JPanel(new GridLayout(2, 1));
    infoPanel.setOpaque(false);

    JLabel subjectLabel = new JLabel(event.getSubject());
    subjectLabel.setFont(new Font("Arial", Font.BOLD, 12));
    infoPanel.add(subjectLabel);

    String timeStr = event.getStart().format(TIME_FMT) + " - " +
        event.getEnd().format(TIME_FMT);
    JLabel timeLabel = new JLabel(timeStr);
    timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    timeLabel.setForeground(Color.DARK_GRAY);
    infoPanel.add(timeLabel);

    this.add(infoPanel, BorderLayout.CENTER);

    // Right: Action Buttons
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    btnPanel.setOpaque(false);

    JButton editBtn = new JButton("Edit");
    editBtn.setFont(new Font("Arial", Font.PLAIN, 10));
    editBtn.setMargin(new Insets(2, 5, 2, 5));
    editBtn.addActionListener(e -> showEditOptions());
    btnPanel.add(editBtn);

    JButton copyBtn = new JButton("Copy");
    copyBtn.setFont(new Font("Arial", Font.PLAIN, 10));
    copyBtn.setMargin(new Insets(2, 5, 2, 5));
    copyBtn.addActionListener(e -> showCopyDialog());
    btnPanel.add(copyBtn);

    this.add(btnPanel, BorderLayout.EAST);
  }

  private void showEditOptions() {
    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
    String editScope = "single";

    // If recurring, ask for scope
    if (event.getSeriesId() != null) {
      String[] options = {"This Event Only", "This and Future", "All in Series", "Cancel"};
      int choice = JOptionPane.showOptionDialog(
          parentFrame, "This is a recurring event. What would you like to edit?", "Edit Scope",
          JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

      if (choice == 3 || choice == -1) return;
      if (choice == 0) editScope = "single";
      else if (choice == 1) editScope = "fromDate";
      else if (choice == 2) editScope = "all";
    }

    EditEventDialog editDialog = new EditEventDialog(parentFrame, event, editScope);
    editDialog.setVisible(true);

    if (editDialog.isConfirmed()) {
      String property = editDialog.getProperty();
      String newValue = editDialog.getNewValue();

      if (editScope.equals("single")) {
        features.editSingleEvent(event.getSubject(), event.getStart(), event.getEnd(), property, newValue);
      } else if (editScope.equals("fromDate")) {
        features.editEventsFromDate(event.getSubject(), event.getStart(), property, newValue);
      } else if (editScope.equals("all")) {
        features.editAllInSeries(event.getSubject(), event.getStart(), property, newValue);
      }
    }
  }

  private void showCopyDialog() {
    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
    CopyEventDialog dialog = new CopyEventDialog(parentFrame, event, features, availableCalendars);
    dialog.setVisible(true);
  }
}