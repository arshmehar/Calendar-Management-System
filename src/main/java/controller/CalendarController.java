package controller;

import java.io.IOException;
import model.CalendarNotFoundException;
import model.Icalendar;
import model.IcalendarSystem;
import view.IcalendarView;

/**
 * Controller Coordinates between the model (calendar data) and view (user interface),
 * parses user commands, and delegates execution.
 */
public class CalendarController implements IcalendarController {
  private final IcalendarView view;
  private Icalendar currentCalendar;
  private final IcalendarSystem model;
  CommandParser commandParser = new CommandParser();
  CommandExecutor commandExecutor;

  /**
   * Constructs a CalendarController with the specified model, view, and mode.
   * Initializes the command executor to handle parsed commands.
   *
   * @param model the calendar model containing event data
   * @param view  the view for displaying output and errors
   */
  public CalendarController(IcalendarSystem model, IcalendarView view) {
    this.model = model;
    this.view = view;
    this.currentCalendar = null;
    this.commandExecutor = new CommandExecutor(model, view);

  }


  @Override
  public void run(ApplicationMode mode, String commandFilePath) {
    ModeManager modeManager = new ModeManager(commandFilePath);
    try {
      modeManager.execute(mode, this);
    } catch (IOException e) {
      view.displayError(e.getMessage());
    }
  }

  @Override
  public void processCommand(String command) {
    try {
      ParsedCommand cmd = commandParser.parse(command);

      if (cmd.getCommandType() == CommandType.SET_CONTEXT) {
        this.currentCalendar = commandExecutor.handleContextSetting(cmd);
        view.displayMessage(
            "Context set successfully to calendar: " + this.currentCalendar.getCalendarName());
      } else {
        commandExecutor.executeCommand(cmd, currentCalendar);
      }

    } catch (CommandParseException | IllegalArgumentException | CalendarNotFoundException e) {
      view.displayError(e.getMessage());
    } catch (Exception e) {
      view.displayError("Unexpected error: " + e.getMessage());
    }

  }

  @Override
  public void shutDown() {

  }

  @Override
  public IcalendarView getView() {
    return this.view;
  }
}
