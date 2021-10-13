package code.ljw.wallet.repository

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter

object DateTime {

  /**
   * converts ISO datetime to UTC and removes timezone string part
   * 2011-10-05T10:48:01+00:00 => 2011-10-05T:10:00::00
   */
  def zonedDateTimeStrToUtc(datetime: String): String = {
    zonedDateTimeStrToUtcDate(datetime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  }

  def zonedDateTimeStrToUtcDate(datetime: String): LocalDateTime = {
    val zonedDateTime = ZonedDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val utcDate = zonedDateTime
      .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime
      .withMinute(0).withSecond(0).withNano(0)
    utcDate
  }

  def range(from: String, to: String): (LocalDateTime, LocalDateTime) = {
    val toAdjusted = zonedDateTimeStrToUtcDate(to).plusHours(1)
    (zonedDateTimeStrToUtcDate(from), toAdjusted)
  }
}
