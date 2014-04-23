package com.joestelmach.natty;

import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarSource {
  private static ThreadLocal<Date> _baseDate = new ThreadLocal<Date>();

  public static void setBaseDate(Date baseDate) {
    _baseDate.set(baseDate);
  }

  public static GregorianCalendar getCurrentCalendar() {
    GregorianCalendar calendar = new GregorianCalendar();
    if(_baseDate.get() != null) {
      calendar.setTime(_baseDate.get());
    }
    return calendar;
  }
}
