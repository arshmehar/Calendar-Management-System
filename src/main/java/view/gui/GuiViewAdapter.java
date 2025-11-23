package view.gui;

import model.Event;
import view.IcalendarView;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adapts the Swing-based CalendarGuiView to the IcalendarView interface
 * expected by the CommandExecutor.
 */
public class GuiViewAdapter implements IcalendarView {
  private final IcalendarGuiView gui;

  public GuiViewAdapter(IcalendarGuiView gui) {
    this.gui = gui;
  }

  @Override
  public void displayWelcome() {
    // GUI doesn't need a welcome message, or could show a splash screen
  }

  @Override
  public void displayPrompt() {
    // GUI is event-driven, no prompt needed
  }

  @Override
  public void displaySuccess(String message) {
    gui.showSuccess(message);
  }

  @Override
  public void displayError(String message) {
    gui.showError(message);
  }

  @Override
  public void displayStatus(boolean isBusy, LocalDateTime date) {
    String status = isBusy ? "Busy" : "Available";
    gui.showSuccess("User is " + status + " on " + date.toString());
  }

  @Override
  public void displayMessage(String message) {
    JOptionPane.showMessageDialog(gui.getFrame(), message, "Message", JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void displayEventsForDate(List<Event> events, LocalDate date) {
    gui.displayEvents(events, date);
  }

  @Override
  public void displayEventsForRange(List<Event> events, LocalDateTime start, LocalDateTime end) {
    // The GUI main view is day-based, so for ranges we display a popup list
    StringBuilder sb = new StringBuilder();
    sb.append("Events from ").append(start.toLocalDate()).append(" to ").append(end.toLocalDate()).append(":\n\n");

    if (events == null || events.isEmpty()) {
      sb.append("No events found.");
    } else {
      for (Event e : events) {
        sb.append(e.toString()).append("\n");
      }
    }

    JTextArea textArea = new JTextArea(sb.toString());
    textArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new java.awt.Dimension(400, 300));

    JOptionPane.showMessageDialog(gui.getFrame(), scrollPane, "Event Range", JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void displayGoodbye() {
    System.exit(0);
  }
}