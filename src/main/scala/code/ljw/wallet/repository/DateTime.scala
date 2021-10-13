package code.ljw.wallet.repository

import java.time.{LocalDate, LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter

object DateTime {

  /**
   * converts ISO datetime to UTC and removes timezone string part
   * 2011-10-05T10:48:01+00:00 => 2011-10-05
   */
  def zonedDateTimeStrToUtc(datetime: String): String = {
    zonedDateTimeStrToUtcDate(datetime).format(DateTimeFormatter.ISO_LOCAL_DATE)
  }

  def zonedDateTimeStrToUtcDate(datetime: String): LocalDate = {
    val zonedDateTime = ZonedDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val utcDate = zonedDateTime
      .withZoneSameInstant(ZoneOffset.UTC).toLocalDate
    utcDate
  }

  def range(from: String, to: String): (String, String) = {
    val toAdjusted = zonedDateTimeStrToUtcDate(to).plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
    (zonedDateTimeStrToUtc(from), toAdjusted)
  }
}
