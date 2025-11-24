package view.gui;

import controller.features.CalendarFeatures;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import model.Event;

public class EventPanel extends JPanel {
  private final Event event;
  private final LocalDate displayDate;
  private final CalendarFeatures features;

  public EventPanel(Event event, LocalDate displayDate, CalendarFeatures features) {
    this.event = event;
    this.displayDate = displayDate;
    this.features = features;
    setupUi();
  }

  private void setupUi() {
    setLayout(new BorderLayout(10, 5));

    // Clean, flat card look
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
        BorderFactory.createEmptyBorder(12, 12, 12, 12) // More internal padding
    ));

    // DETAILS
    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setBackground(Color.WHITE);
    detailsPanel.setOpaque(false);

    // Subject
    JLabel subjectLabel = new JLabel(event.getSubject());
    subjectLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    subjectLabel.setForeground(new Color(30, 30, 30));
    detailsPanel.add(subjectLabel);
    detailsPanel.add(Box.createVerticalStrut(4));

    // Time
    DateTimeFormatter timeFormat;
    if (event.getStart().toLocalDate().equals(event.getEnd().toLocalDate())) {
      timeFormat = DateTimeFormatter.ofPattern("h:mm a");
    } else {
      timeFormat = DateTimeFormatter.ofPattern("MMM d, h:mm a");
    }
    String timeText = event.getStart().format(timeFormat) + " - " + event.getEnd().format(timeFormat);

    JLabel timeLabel = new JLabel(timeText);
    timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    timeLabel.setForeground(new Color(100, 100, 100));
    detailsPanel.add(timeLabel);

    // Optional fields
    if (hasLocation()) {
      detailsPanel.add(Box.createVerticalStrut(2));
      addDetailLabel(detailsPanel, "📍 " + event.getLocation());
    }
    if (hasDescription()) {
      detailsPanel.add(Box.createVerticalStrut(2));
      String desc = event.getDescription();
      if (desc.length() > 40) desc = desc.substring(0, 37) + "...";
      addDetailLabel(detailsPanel, "📝 " + desc);
    }
    if (hasStatus()) {
      detailsPanel.add(Box.createVerticalStrut(2));
      addDetailLabel(detailsPanel, "Status: " + event.getStatus());
    }
    if (event.getSeriesId() != null) {
      detailsPanel.add(Box.createVerticalStrut(4));
      JLabel seriesLabel = new JLabel("Recurring Series");
      seriesLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
      seriesLabel.setForeground(new Color(70, 130, 180));
      detailsPanel.add(seriesLabel);
    }

    add(detailsPanel, BorderLayout.CENTER);

    // BUTTON
    JButton editButton = new JButton("Edit");
    editButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
    editButton.setFocusPainted(false);
    editButton.setBackground(new Color(245, 245, 245));
    editButton.addActionListener(e -> showEditOptions());

    JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    btnContainer.setOpaque(false);
    btnContainer.add(editButton);
    add(btnContainer, BorderLayout.EAST);

    // Dynamic sizing based on content
    int h = 65;
    if(hasLocation()) h+=16;
    if(hasDescription()) h+=16;
    if(hasStatus()) h+=16;
    setPreferredSize(new Dimension(0, h));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
  }

  private void addDetailLabel(JPanel panel, String text) {
    JLabel lbl = new JLabel(text);
    lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
    lbl.setForeground(new Color(80, 80, 80));
    panel.add(lbl);
  }

  private boolean hasLocation() { return event.getLocation() != null && !event.getLocation().trim().isEmpty(); }
  private boolean hasDescription() { return event.getDescription() != null && !event.getDescription().trim().isEmpty(); }
  private boolean hasStatus() { return event.getStatus() != null; }

  // Logic unchanged
  private void showEditOptions() {
    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
    String editScope = "single";
    if (event.getSeriesId() != null) {
      String[] options = {"This Event Only", "This and Future", "All in Series", "Cancel"};
      int choice = JOptionPane.showOptionDialog(parentFrame,
          "This is a recurring event. What would you like to edit?", "Edit Scope",
          JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
      if (choice == 3 || choice == -1) return;
      if (choice == 0) editScope = "single";
      else if (choice == 1) editScope = "fromDate";
      else if (choice == 2) editScope = "all";
    }

    EditEventDialog editDialog = new EditEventDialog(parentFrame, event, displayDate, editScope);
    editDialog.setVisible(true);

    if (editDialog.isConfirmed()) {
      String property = editDialog.getProperty();
      String newValue = editDialog.getNewValue();
      if (editScope.equals("single")) features.editSingleEvent(event.getSubject(), event.getStart(), event.getEnd(), property, newValue);
      else if (editScope.equals("fromDate")) features.editEventsFromDate(event.getSubject(), event.getStart(), property, newValue);
      else if (editScope.equals("all")) features.editAllInSeries(event.getSubject(), event.getStart(), property, newValue);
    }
  }
}