// view/gui/EventPanel.java
package view.gui;

import controller.features.CalendarFeatures;
import model.Event;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Panel representing a single clickable event.
 * Only displays properties that have been set (non-null, non-empty).
 */
public class EventPanel extends JPanel {
  private final Event event;
  private final LocalDate displayDate;
  private final CalendarFeatures features;

  public EventPanel(Event event, LocalDate displayDate, CalendarFeatures features) {
    this.event = event;
    this.displayDate = displayDate;
    this.features = features;

    setupUI();
  }

  private void setupUI() {
    setLayout(new BorderLayout(5, 5));
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    setBackground(new Color(245, 245, 250));

    // Calculate height based on how many properties are present
    int baseHeight = 60;
    int extraHeight = 0;
    if (hasLocation()) extraHeight += 15;
    if (hasDescription()) extraHeight += 15;
    if (hasStatus()) extraHeight += 15;
    if (event.getSeriesId() != null) extraHeight += 15;

    setMaximumSize(new Dimension(Integer.MAX_VALUE, baseHeight + extraHeight));

    // Event details panel
    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setBackground(new Color(245, 245, 250));

    // Subject (always shown)
    JLabel subjectLabel = new JLabel(event.getSubject());
    subjectLabel.setFont(new Font("Arial", Font.BOLD, 14));
    detailsPanel.add(subjectLabel);
    detailsPanel.add(Box.createVerticalStrut(3));

    // Time (always shown)
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm a");
    String timeText = "⏰ " + event.getStart().format(timeFormat) +
        " - " + event.getEnd().format(timeFormat);
    JLabel timeLabel = new JLabel(timeText);
    timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    timeLabel.setForeground(new Color(80, 80, 80));
    detailsPanel.add(timeLabel);

    // Location (only if set)
    if (hasLocation()) {
      JLabel locationLabel = new JLabel("Location: " + event.getLocation());
      locationLabel.setFont(new Font("Arial", Font.PLAIN, 11));
      locationLabel.setForeground(new Color(80, 80, 80));
      detailsPanel.add(locationLabel);
    }

    // Description (only if set)
    if (hasDescription()) {
      String desc = event.getDescription();
      if (desc.length() > 50) {
        desc = desc.substring(0, 47) + "...";
      }
      JLabel descLabel = new JLabel("Description: " + desc);
      descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
      descLabel.setForeground(new Color(80, 80, 80));
      detailsPanel.add(descLabel);
    }

    // Status (only if set)
    if (hasStatus()) {
      JLabel statusLabel = new JLabel(  "Status: " + event.getStatus());
      statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
      statusLabel.setForeground(new Color(80, 80, 80));
      detailsPanel.add(statusLabel);
    }

    // Series indicator (only if part of series)
    if (event.getSeriesId() != null) {
      JLabel seriesLabel = new JLabel("🔁 Recurring Event");
      seriesLabel.setFont(new Font("Arial", Font.ITALIC, 10));
      seriesLabel.setForeground(new Color(100, 100, 200));
      detailsPanel.add(seriesLabel);
    }

    add(detailsPanel, BorderLayout.CENTER);

    // Action buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    buttonPanel.setBackground(new Color(245, 245, 250));

    JButton editButton = new JButton("Edit");
    editButton.setFont(new Font("Arial", Font.PLAIN, 10));
    editButton.setPreferredSize(new Dimension(60, 25));
    editButton.addActionListener(e -> showEditOptions());
    buttonPanel.add(editButton);

    JButton copyButton = new JButton("Copy");
    copyButton.setFont(new Font("Arial", Font.PLAIN, 10));
    copyButton.setPreferredSize(new Dimension(60, 25));
    copyButton.addActionListener(e -> showCopyDialog());
    buttonPanel.add(copyButton);

    add(buttonPanel, BorderLayout.EAST);

    // Hover effect
    addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        setBackground(new Color(235, 240, 250));
        detailsPanel.setBackground(new Color(235, 240, 250));
        buttonPanel.setBackground(new Color(235, 240, 250));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        setBackground(new Color(245, 245, 250));
        detailsPanel.setBackground(new Color(245, 245, 250));
        buttonPanel.setBackground(new Color(245, 245, 250));
        setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  // Helper methods to check if properties are set

  private boolean hasLocation() {
    return event.getLocation() != null && !event.getLocation().trim().isEmpty();
  }

  private boolean hasDescription() {
    return event.getDescription() != null && !event.getDescription().trim().isEmpty();
  }

  private boolean hasStatus() {
    return event.getStatus() != null;
  }

  private void showEditOptions() {
    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

    // If it's a series event, ask edit scope
    String editScope = "single";
    if (event.getSeriesId() != null) {
      String[] options = {"This Event Only", "This and Future", "All in Series", "Cancel"};
      int choice = JOptionPane.showOptionDialog(
          parentFrame,
          "This is a recurring event. What would you like to edit?",
          "Edit Scope",
          JOptionPane.DEFAULT_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null,
          options,
          options[0]
      );

      if (choice == 3 || choice == -1) return; // Cancel

      if (choice == 0) editScope = "single";
      else if (choice == 1) editScope = "fromDate";
      else if (choice == 2) editScope = "all";
    }

    // Show edit dialog
    EditEventDialog editDialog = new EditEventDialog(
        parentFrame,
        event,
        displayDate,
        editScope
    );
    editDialog.setVisible(true);

    if (editDialog.isConfirmed()) {
      String property = editDialog.getProperty();
      String newValue = editDialog.getNewValue();

      if (editScope.equals("single")) {
        features.editSingleEvent(
            event.getSubject(),
            event.getStart(),
            event.getEnd(),
            property,
            newValue
        );
      } else if (editScope.equals("fromDate")) {
        features.editEventsFromDate(
            event.getSubject(),
            event.getStart(),
            property,
            newValue
        );
      } else if (editScope.equals("all")) {
        features.editAllInSeries(
            event.getSubject(),
            event.getStart(),
            property,
            newValue
        );
      }
    }
  }

  private void showCopyDialog() {
    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

    CopyEventDialog copyDialog = new CopyEventDialog(
        parentFrame,
        event,
        displayDate,
        features
    );
    copyDialog.setVisible(true);
  }
}