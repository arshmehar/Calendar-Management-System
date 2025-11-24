package mocks;

import controller.features.CalendarFeatures;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JFrame;
import model.Event;
import view.gui.IcalendarGuiView;

/**
 * Mock implementation of IcalendarGuiView for testing.
 * Logs all method calls to verify controller-view interactions.
 */
public class MockIcalendarGuiView implements IcalendarGuiView {
  private final StringBuilder log;
  private CalendarFeatures features;
  private String selectedCalendar;
  private LocalDate selectedDate;

  /**
   * Constructs a MockIcalendarGuiView.
   *
   * @param log StringBuilder to record all method calls
   */
  public MockIcalendarGuiView(StringBuilder log) {
    this.log = log;
    this.selectedDate = LocalDate.now();
  }

  @Override
  public void setFeatures(CalendarFeatures features) {
    log.append("setFeatures").append(System.lineSeparator());
    this.features = features;
  }

  @Override
  public void display() {
    log.append("display").append(System.lineSeparator());
  }

  @Override
  public void setCalendarNames(List<String> names) {
    log.append("setCalendarNames: ").append(names).append(System.lineSeparator());
  }

  @Override
  public void setSelectedCalendar(String name) {
    log.append("setSelectedCalendar: ").append(name).append(System.lineSeparator());
    this.selectedCalendar = name;
  }

  @Override
  public void updateTimezoneDisplay(String timezoneId) {
    log.append("updateTimezoneDisplay: ").append(timezoneId).append(System.lineSeparator());
  }

  @Override
  public void displayEvents(List<Event> events, LocalDate date) {
    log.append("displayEvents: date=").append(date);
    if (events != null) {
      log.append(" count=").append(events.size());
    }
    log.append(System.lineSeparator());
  }

  @Override
  public void showSuccess(String message) {
    log.append("showSuccess: ").append(message).append(System.lineSeparator());
  }

  @Override
  public void showError(String message) {
    log.append("showError: ").append(message).append(System.lineSeparator());
  }

  @Override
  public LocalDate getSelectedDate() {
    log.append("getSelectedDate").append(System.lineSeparator());
    return selectedDate;
  }

  @Override
  public JFrame getFrame() {
    log.append("getFrame").append(System.lineSeparator());
    return null;
  }

  /**
   * Gets the log of all method calls.
   *
   * @return the accumulated log
   */
  public String getLog() {
    return log.toString();
  }

  /**
   * Clears the log.
   */
  public void clearLog() {
    log.setLength(0);
  }

  /**
   * Sets the selected date for testing purposes.
   *
   * @param date the date to set
   */
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;
  }

  /**
   * Gets the features object set by the controller.
   *
   * @return the CalendarFeatures instance
   */
  public CalendarFeatures getFeatures() {
    return features;
  }


}