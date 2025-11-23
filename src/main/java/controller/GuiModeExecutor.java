package controller;

import javax.swing.SwingUtilities;
import view.IcalendarView;
import view.gui.CalendarGuiView;
import view.gui.GuiViewAdapter;
import view.gui.IcalendarGuiView;

public class GuiModeExecutor {

  IcalendarGuiView guiView;
  private IcalendarView view;
  CommandExecutor commandExecutor;

  public GuiModeExecutor() {

  }



  private void startGuiMode(CalendarController controller) {
    controller.createDefaultCalendar();
    controller.updateCalendarList();
    SwingUtilities.invokeLater(guiView::display);
  }
}
