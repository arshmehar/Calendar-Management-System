package mocks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.Event;
import view.IcalendarView;

/**
 * Mock implementation of IcalendarView for testing.
 * Logs all method calls for verification in tests.
 */
public class MockIcalendarView implements IcalendarView {
  private final StringBuilder log;

  /**
   * Constructs a MockIcalendarView with log.
   *
   * @param log StringBuilder to record display calls
   */
  public MockIcalendarView(StringBuilder log) {
    this.log = log;
  }

  @Override
  public void displayWelcome() {
    log.append("displayWelcome\n");
  }

  @Override
  public void displayPrompt() {
    log.append("displayPrompt\n");
  }

  @Override
  public void displaySuccess(String message) {
    log.append("displaySuccess: ").append(message).append(System.lineSeparator());
  }

  @Override
  public void displayError(String message) {
    log.append("displayError: ").append(message).append(System.lineSeparator());
  }

  @Override
  public void displayStatus(boolean isBusy, LocalDateTime date) {
    log.append("displayStatus: date=").append(date)
        .append(" busy=").append(isBusy).append(System.lineSeparator());
  }

  @Override
  public void displayMessage(String message) {
    log.append("displayMessage: ").append(message).append(System.lineSeparator());
  }

  @Override
  public void displayEventsForDate(List<Event> events, LocalDate date) {
    log.append("displayEventsForDate: date=").append(date)
        .append(" count=").append(events.size()).append(System.lineSeparator());
  }

  @Override
  public void displayEventsForRange(List<Event> events, LocalDateTime start,
                                    LocalDateTime end) {
    log.append("displayEventsForRange: start=").append(start)
        .append(" end=").append(end)
        .append(" count=").append(events.size()).append(System.lineSeparator());
  }

  @Override
  public void displayGoodbye() {
    log.append("displayGoodbye").append(System.lineSeparator());
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
}