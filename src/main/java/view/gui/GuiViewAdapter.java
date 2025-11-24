package view.gui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import model.Event;
import view.IcalendarView;

/**
 * Adapts the Swing-based CalendarGuiView to the IcalendarView interface
 * expected by the CommandExecutor.
 * This allows the CommandExecutor, originally designed for text input/output,
 * to trigger GUI-specific display methods (like showing popups or refreshing panels).
 */
public class GuiViewAdapter implements IcalendarView {
  private final IcalendarGuiView gui;

  /**
   * Constructs a GuiViewAdapter.
   *
   * @param gui The specific GUI view implementation to which all display commands are delegated.
   */
  public GuiViewAdapter(IcalendarGuiView gui) {
    this.gui = gui;
  }

  /**
   * Displays a welcome message.
   * This is generally not used in a GUI, but the method must be implemented.
   */
  @Override
  public void displayWelcome() {
  }

  /**
   * Displays a prompt for user input.
   * This is generally not used in a GUI, as input is event-driven.
   */
  @Override
  public void displayPrompt() {

  }

  /**
   * Displays a success message to the user via a GUI popup.
   *
   * @param message The success message string.
   */
  @Override
  public void displaySuccess(String message) {
    gui.showSuccess(message);
  }

  /**
   * Displays an error message to the user via a GUI warning popup.
   *
   * @param message The error message string.
   */
  @Override
  public void displayError(String message) {
    gui.showError(message);
  }

  /**
   * Displays the user's availability status for a specific date and time via a success popup.
   *
   * @param isBusy True if the user is busy, false if available.
   * @param date The date and time being checked.
   */
  @Override
  public void displayStatus(boolean isBusy, LocalDateTime date) {
    String status = isBusy ? "Busy" : "Available";
    gui.showSuccess("User is " + status + " on " + date.toString());
  }

  /**
   * Displays a generic informational message to the user via a standard popup.
   *
   * @param message The message string.
   */
  @Override
  public void displayMessage(String message) {
    JOptionPane.showMessageDialog(gui.getFrame(), message, "Message",
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Displays the list of events scheduled on a specific date by refreshing the GUI's event panel.
   *
   * @param events The list of events on the specified date.
   * @param date The date to which the events belong.
   */
  @Override
  public void displayEventsForDate(List<Event> events, LocalDate date) {
    gui.displayEvents(events, date);
  }

  /**
   * Displays the list of events within a date range via a scrollable popup list.
   *
   * @param events The list of events in the range.
   * @param start The start date/time of the range.
   * @param end The end date/time of the range.
   */
  @Override
  public void displayEventsForRange(List<Event> events, LocalDateTime start, LocalDateTime end) {

    StringBuilder sb = new StringBuilder();
    sb.append("Events from ").append(start.toLocalDate()).append(" to ").append(end.toLocalDate())
        .append(":\n\n");

    if (events == null || events.isEmpty()) {
      sb.append("No events found.");
    } else {
      for (Event event : events) {
        sb.append(event.toString()).append("\n");
      }
    }

    JTextArea textArea = new JTextArea(sb.toString());
    textArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new java.awt.Dimension(400, 300));

    JOptionPane.showMessageDialog(gui.getFrame(), scrollPane, "Event Range",
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Shuts down the application.
   */
  @Override
  public void displayGoodbye() {
    System.exit(0);
  }
}