package controller;

/**
 * class to extract event subject and leftover command text.
 */
public final class ExtractUtil {
  ExtractUtil() {
  }


  /**
   * Extracts the event subject and remaining command text.
   * Handles both quoted subjects (multi-word) and unquoted subjects (single word).
   *
   * @param text the command text after "create/copy event"
   * @return array with [0]=subject, [1]=remaining text
   * @throws CommandParseException if subject is empty or quotes are mismatched
   */
  public static String[] extractSubjectAndRest(String text) {
    text = text.trim();
    String[] result = new String[2];

    if (text.startsWith("\"")) {
      int endQuoteIdx = text.indexOf('"', 1);
      if (endQuoteIdx == -1) {
        throw new CommandParseException("Mismatched quotes in subject.");
      }

      result[0] = text.substring(1, endQuoteIdx);
      result[1] = text.substring(endQuoteIdx + 1).trim();

    } else {

      int firstSpaceIdx = text.indexOf(' ');
      if (firstSpaceIdx == -1) {
        throw new CommandParseException("Command missing arguments after subject.");
      }

      result[0] = text.substring(0, firstSpaceIdx);
      result[1] = text.substring(firstSpaceIdx + 1).trim();
    }

    if (result[0].isEmpty()) {
      throw new CommandParseException("Subject cannot be empty.");
    }

    return result;
  }


}
