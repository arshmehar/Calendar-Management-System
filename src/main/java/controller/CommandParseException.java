package controller;

/**
 * Exception passed when a command cannot be passed correctly.
 * due to invalid command syntax or unrecognized command format.
 */
public class CommandParseException extends RuntimeException {

  /**
   *Constructs a CommandParseException.
   *
   * @param message description of parsing error.
   */
  public CommandParseException(String message) {
    super(message);
  }
}
