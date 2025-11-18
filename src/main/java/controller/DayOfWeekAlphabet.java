package controller;

import java.time.DayOfWeek;

/**
 * Represents days of the week using single-character alphabetic codes
 * as specified in the assignment. Maps to Java's DayOfWeek enum.
 * M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday.
 */
public enum DayOfWeekAlphabet {
  M(DayOfWeek.MONDAY),
  T(DayOfWeek.TUESDAY),
  W(DayOfWeek.WEDNESDAY),
  R(DayOfWeek.THURSDAY),
  F(DayOfWeek.FRIDAY),
  S(DayOfWeek.SATURDAY),
  U(DayOfWeek.SUNDAY);

  /**
   * Constructs a DayOfWeekAlphabet with the corresponding Java DayOfWeek.
   */
  private final DayOfWeek dayOfWeek;

  DayOfWeekAlphabet(DayOfWeek dayOfWeek) {
    this.dayOfWeek = dayOfWeek;
  }

  /**
   * Converts this alphabetic day code to Java's standard DayOfWeek enum.
   *
   * @return the corresponding DayOfWeek value
   */
  public DayOfWeek toJavaDay() {
    return dayOfWeek;
  }
}
