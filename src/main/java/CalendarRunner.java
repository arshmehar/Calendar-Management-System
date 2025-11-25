import controller.ApplicationMode;
import controller.CalendarController;
import controller.IcalendarController;
import java.io.IOException;
import model.CalendarSystem;
import model.IcalendarSystem;

/**
 * Program runner.
 */
public class CalendarRunner {
  /**
   * The main method placeholder.
   */
  public static void main(String[] args) throws IOException {

    IcalendarSystem model = new CalendarSystem();
    ApplicationMode mode;
    String commandFilePath = null;

    if (args.length == 0) {
      mode = ApplicationMode.GUI;
    } else {
      if (args.length == 1) {
        System.err.println("Usage: java CalendarApp --mode <interactive|headless> [commandFile]");
        System.exit(1);
      }

      String modeArg = args[1].toLowerCase();

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
    }

    IcalendarController controller = new CalendarController(model);
    controller.run(mode, commandFilePath);
  }
}

