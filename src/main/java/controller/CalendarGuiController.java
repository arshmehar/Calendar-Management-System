package controller;

import java.time.ZoneId;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import model.Icalendar;
import model.IcalendarSystem;
import view.gui.CalendarGuiView;

public class CalendarGuiController implements IguiController{

  private final IcalendarSystem model;
  private final CalendarGuiView view;
  private final CommandExecutor commandExecutor;
  private Icalendar currentCalendar;

  public CalendarGuiController(IcalendarSystem model) {
    this.model = model;
    this.commandExecutor = new CommandExecutor(model, null); // No text view needed
    this.view = new CalendarGuiView(this); // Pass controller to view

    // Create and set default calendar
    createDefaultCalendar();
  }

  private void createDefaultCalendar() {
    try {
      ZoneId systemZone = ZoneId.systemDefault();
      model.createCalendar("Default", systemZone.getId());
      currentCalendar = model.getCalendar("Default");
    } catch (Exception e) {
      showError("Failed to create default calendar: " + e.getMessage());
    }
  }

  @Override
  public void executeCommand(ParsedCommand cmd) throws Exception {
    // Handle context setting specially
    if (cmd.getCommandType() == CommandType.SET_CONTEXT) {
      currentCalendar = commandExecutor.handleContextSetting(cmd);
      showSuccess("Switched to calendar: " + currentCalendar.getCalendarName());
    } else {
      // Execute regular commands
      commandExecutor.executeCommand(cmd, currentCalendar);
    }
  }

  @Override
  public Icalendar getCurrentCalendar() {
    return currentCalendar;
  }

  @Override
  public void setCurrentCalendar(String calendarName) {
    try {
      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.SET_CONTEXT)
          .context(calendarName)
          .build();

      executeCommand(cmd);
      refreshView();
    } catch (Exception e) {
      showError("Failed to switch calendar: " + e.getMessage());
    }
  }

  @Override
  public void refreshView() {
    view.refresh();
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(
        view.getFrame(),
        message,
        "Error",
        JOptionPane.ERROR_MESSAGE
    );
  }

  @Override
  public void showSuccess(String message) {
    JOptionPane.showMessageDialog(
        view.getFrame(),
        message,
        "Success",
        JOptionPane.INFORMATION_MESSAGE
    );
  }

  @Override
  public IcalendarSystem getModel() {
    return model;
  }

  @Override
  public List<String> getAllCalendarNames() {
    return model.getAllCalendarNames();
  }

  /**
   * Start the GUI application.
   */
  public void run() {
    SwingUtilities.invokeLater(view::display);
  }
}
