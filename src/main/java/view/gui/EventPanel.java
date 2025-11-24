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
import javax.swing.border.Border;
import model.Event;

/**
 * Panel representing a single clickable event.
 * Only displays properties that have been set (non-null, non-empty).
 * This component is used in the right-hand events display area.
 */
public class EventPanel extends JPanel {
  private final Event event;
  private final LocalDate displayDate;
  private final CalendarFeatures features;

  /**
   * Constructs an EventPanel.
   *
   * @param event The event object to display.
   * @param displayDate The date context under which the event is being shown.
   * @param features The controller features interface for triggering edits.
   */
  public EventPanel(Event event, LocalDate displayDate, CalendarFeatures features) {
    this.event = event;
    this.displayDate = displayDate;
    this.features = features;

    setupUi();
  }

  /**
   * Initializes and lays out the UI components for the event panel,
   * calculating the required height based on available data fields.
   */
  private void setupUi() {
    setLayout(new BorderLayout(5, 5));

    Border outerBorder = BorderFactory.createLineBorder(new Color(220, 220, 220), 1);
    Border innerBorder = BorderFactory.createEmptyBorder(12, 12, 12, 12);
    setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

    setBackground(Color.WHITE);

    int extraHeight = 0;
    if (hasLocation()) {
      extraHeight += 16;
    }
    if (hasDescription()) {
      extraHeight += 16;
    }
    if (hasStatus()) {
      extraHeight += 16;
    }
    if (event.getSeriesId() != null) {
      extraHeight += 16;
    }

    int baseHeight = 65;
    setMaximumSize(new Dimension(Integer.MAX_VALUE, baseHeight + extraHeight));

    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setBackground(Color.WHITE);

    JLabel subjectLabel = new JLabel(event.getSubject());
    subjectLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    subjectLabel.setForeground(new Color(30, 30, 30));
    detailsPanel.add(subjectLabel);
    detailsPanel.add(Box.createVerticalStrut(4));

    DateTimeFormatter timeFormat;
    if (event.getStart().toLocalDate().equals(event.getEnd().toLocalDate())) {
      timeFormat = DateTimeFormatter.ofPattern("h:mm a");
    } else {
      timeFormat = DateTimeFormatter.ofPattern("MMM d, h:mm a");
    }

    String timeText = event.getStart().format(timeFormat)
        + " - " + event.getEnd().format(timeFormat);
    JLabel timeLabel = new JLabel(timeText);
    timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    timeLabel.setForeground(new Color(100, 100, 100));
    detailsPanel.add(timeLabel);

    if (hasLocation()) {
      detailsPanel.add(Box.createVerticalStrut(2));
      JLabel locationLabel = new JLabel("📍 Location: " + event.getLocation());
      locationLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
      locationLabel.setForeground(new Color(80, 80, 80));
      detailsPanel.add(locationLabel);
    }

    if (hasDescription()) {
      detailsPanel.add(Box.createVerticalStrut(2));
      String desc = event.getDescription();
      if (desc.length() > 50) {
        desc = desc.substring(0, 47) + "...";
      }
      JLabel descLabel = new JLabel("📝 Description: " + desc);
      descLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
      descLabel.setForeground(new Color(80, 80, 80));
      detailsPanel.add(descLabel);
    }

    if (hasStatus()) {
      detailsPanel.add(Box.createVerticalStrut(2));
      JLabel statusLabel = new JLabel("Status: " + event.getStatus());
      statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
      statusLabel.setForeground(new Color(80, 80, 80));
      detailsPanel.add(statusLabel);
    }

    if (event.getSeriesId() != null) {
      detailsPanel.add(Box.createVerticalStrut(4));
      JLabel seriesLabel = new JLabel("Recurring Series");
      seriesLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
      seriesLabel.setForeground(new Color(70, 130, 180));
      detailsPanel.add(seriesLabel);
    }

    add(detailsPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    buttonPanel.setBackground(Color.WHITE);

    JButton editButton = new JButton("Edit");
    editButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
    editButton.setFocusPainted(false);
    editButton.setBackground(new Color(245, 245, 245));
    editButton.setPreferredSize(new Dimension(60, 25));
    editButton.addActionListener(e -> showEditOptions());
    buttonPanel.add(editButton);

    add(buttonPanel, BorderLayout.EAST);

    addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        setBackground(new Color(248, 250, 255));
        detailsPanel.setBackground(new Color(248, 250, 255));
        buttonPanel.setBackground(new Color(248, 250, 255));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        setBackground(Color.WHITE);
        detailsPanel.setBackground(Color.WHITE);
        buttonPanel.setBackground(Color.WHITE);
        setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  /**
   * Checks if the event has a location property set.
   *
   * @return true if location is non-null and non-empty.
   */
  private boolean hasLocation() {
    return event.getLocation() != null && !event.getLocation().trim().isEmpty();
  }

  /**
   * Checks if the event has a description property set.
   *
   * @return true if description is non-null and non-empty.
   */
  private boolean hasDescription() {
    return event.getDescription() != null && !event.getDescription().trim().isEmpty();
  }

  /**
   * Checks if the event has a status property set.
   *
   * @return true if status is non-null.
   */
  private boolean hasStatus() {
    return event.getStatus() != null;
  }

  /**
   * Prompts the user for the edit scope (single, from date, all series)
   * and launches the EditEventDialog. Triggers the corresponding
   * controller method upon confirmation.
   */
  private void showEditOptions() {
    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

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

      if (choice == 3 || choice == -1) {
        return;
      }

      if (choice == 0) {
        editScope = "single";
      } else if (choice == 1) {
        editScope = "fromDate";
      } else if (choice == 2) {
        editScope = "all";
      }
    }

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

}