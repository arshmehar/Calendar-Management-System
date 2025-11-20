package controller.features;
import controller.IguiController;

/**
 * Represents a user-triggered feature/action in the GUI.
 * Each feature knows how to gather its required input and execute.
 */
public interface CalendarFeature {
  /**
   * Execute this feature. The feature will gather necessary input
   * from the user via dialogs and then execute the appropriate command.
   *
   * @param controller the GUI controller to interact with
   */
  void execute(IguiController controller);

}