import controller.ApplicationMode;
import controller.CalendarController;
import controller.IcalendarController;
import java.io.IOException;
import model.CalendarImp;
import model.CalendarSystem;
import model.Icalendar;
import model.IcalendarSystem;
import view.CalendarView;
import view.IcalendarView;

/**
 * Program runner.
 */
public class CalendarRunner {
  /**
   * The main method placeholder.
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 2) {
      System.err.println("Usage: java CalendarApp --mode <interactive|headless> [commandFile]");
      System.exit(1);
    }

    String modeArg = args[1].toLowerCase();
    String commandFilePath = null;
    ApplicationMode mode;
    IcalendarView view = new CalendarView();

    if (modeArg.equals("interactive")) {
      mode = ApplicationMode.INTERACTIVE;

    } else if (modeArg.equals("headless")) {
      mode = ApplicationMode.HEADLESS;

      if (args.length < 3) {
        System.err.println("Headless mode requires a command file");
        System.exit(1);
      }

      commandFilePath = args[2];

    } else {
      System.err.println("Invalid mode. Must be 'interactive' or 'headless'");
      System.exit(1);
      return;
    }

    IcalendarSystem model = new CalendarSystem();

    IcalendarController controller = new CalendarController(model, view);
    controller.run(mode, commandFilePath);
  }


}

