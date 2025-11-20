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
        BorderFactory.createEmptyBorder(8, 8, 8, 8)
    ));
    setBackground(new Color(245, 245, 250));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

    // Event details
    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setBackground(new Color(245, 245, 250));

    // Subject
    JLabel subjectLabel = new JLabel(event.getSubject());
    subjectLabel.setFont(new Font("Arial", Font.BOLD, 13));
    detailsPanel.add(subjectLabel);

    // Time
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm a");
    String timeText = event.getStart().format(timeFormat) +
        " - " + event.getEnd().format(timeFormat);
    JLabel timeLabel = new JLabel(timeText);
    timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    timeLabel.setForeground(Color.GRAY);
    detailsPanel.add(timeLabel);

    // Series indicator
    if (event.getSeriesId() != null) {
      JLabel seriesLabel = new JLabel("🔁 Recurring");
      seriesLabel.setFont(new Font("Arial", Font.ITALIC, 10));
      seriesLabel.setForeground(new Color(100, 100, 200));
      detailsPanel.add(seriesLabel);
    }

    add(detailsPanel, BorderLayout.CENTER);

    // Buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    buttonPanel.setBackground(new Color(245, 245, 250));

    JButton editButton = new JButton("Edit");
    editButton.setFont(new Font("Arial", Font.PLAIN, 10));
    editButton.addActionListener(e -> showEditOptions());
    buttonPanel.add(editButton);

    JButton copyButton = new JButton("Copy");
    copyButton.setFont(new Font("Arial", Font.PLAIN, 10));
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