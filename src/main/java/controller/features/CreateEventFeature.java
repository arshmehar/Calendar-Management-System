//
//package controller.features;
//
//import controller.IguiController;
//import controller.ParsedCommand;
//import controller.CommandType;
//import view.gui.dialogs.EventCreationDialog;
//import model.DuplicateEventException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
///**
// * Feature to create a new event on a selected date.
// */
//public class CreateEventFeature implements CalendarFeature {
//  private final LocalDate selectedDate;
//
//  public CreateEventFeature(LocalDate selectedDate) {
//    this.selectedDate = selectedDate;
//  }
//
//  @Override
//  public void execute(IguiController controller) {
//    // Check if calendar is selected
//    if (controller.getCurrentCalendar() == null) {
//      controller.showError("Please select a calendar first");
//      return;
//    }
//
//    // Show event creation dialog
//    EventCreationDialog dialog = new EventCreationDialog(
//        controller.getView().getFrame(),
//        selectedDate
//    );
//    dialog.showDialog();
//
//    if (!dialog.wasConfirmed()) {
//      return;
//    }
//
//    // Extract data
//    String subject = dialog.getSubject();
//    LocalDateTime start = dialog.getStartDateTime();
//    LocalDateTime end = dialog.getEndDateTime();
//
//    // Check if it's recurring
//    if (dialog.isRecurring()) {
//      // Handle recurring event
//      ParsedCommand cmd = ParsedCommand.builder()
//          .commandType(CommandType.CREATE_RECURRING_EVENT)
//          .subject(subject)
//          .startDateTime(start)
//          .endDateTime(end)
//          .weekdays(dialog.getSelectedWeekdays())
//          .occurrences(dialog.getOccurrences())
//          .build();
//
//      try {
//        controller.executeCommand(cmd);
//        controller.refreshView();
//        controller.showSuccess("Recurring event created successfully");
//      } catch (Exception e) {
//        controller.showError("Failed to create recurring event: " + e.getMessage());
//      }
//    } else {
//      // Handle single event
//      ParsedCommand cmd = new ParsedCommand.Builder(CommandType.CREATE_SINGLE_EVENT)
//          .subject(subject)
//          .startDateTime(start)
//          .endDateTime(end)
//          .build();
//
//      try {
//        controller.executeCommand(cmd);
//        controller.refreshView();
//        controller.showSuccess("Event '" + subject + "' created successfully");
//      } catch (DuplicateEventException e) {
//        controller.showError("Event already exists at this time");
//      } catch (Exception e) {
//        controller.showError("Failed to create event: " + e.getMessage());
//      }
//    }
//  }
//
//  @Override
//  public String getFeatureName() {
//    return "Create Event";
//  }
//}