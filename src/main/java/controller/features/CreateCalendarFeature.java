//// controller/features/CreateCalendarFeature.java
//package controller.features;
//
//import controller.IguiController;
//import javax.swing.*;
//
//public class CreateCalendarFeature implements CalendarFeature {
//
//  @Override
//  public void execute(IguiController controller) {
//    String name = JOptionPane.showInputDialog(
//        controller.getView().getFrame(),
//        "Enter calendar name:"
//    );
//
//    if (name == null || name.trim().isEmpty()) {
//      return;
//    }
//
//    String[] timezones = {
//        "America/New_York",
//        "America/Los_Angeles",
//        "America/Chicago",
//        "Europe/London",
//        "Asia/Tokyo"
//    };
//
//    String timezone = (String) JOptionPane.showInputDialog(
//        controller.getView().getFrame(),
//        "Select timezone:",
//        "Timezone",
//        JOptionPane.QUESTION_MESSAGE,
//        null,
//        timezones,
//        timezones[0]
//    );
//
//    if (timezone != null) {
//      try {
//        controller.getModel().createCalendar(name, timezone);
//        controller.refreshView();
//        controller.showSuccess("Calendar created successfully");
//      } catch (Exception e) {
//        controller.showError("Error: " + e.getMessage());
//      }
//    }
//  }
//}