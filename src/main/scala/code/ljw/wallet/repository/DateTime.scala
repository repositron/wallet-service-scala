package code.ljw.wallet.repository

import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter

object DateTime {

  /**
   * converts ISO datetime to UTC and removes timezone string part
   * 2011-10-05T10:48:01+00:00 => 2011-10-05T10:48:01
   */
  def zonedDateTimeStrToUtc(datetime: String): String = {
    val zonedDateTime = ZonedDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val utcDateTime = zonedDateTime
      .withZoneSameInstant(ZoneOffset.UTC)
      .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    utcDateTime
  }

  /**
   * converts datetime to utc and sets the time to 00:00:00
   * 2011-10-05T10:48:01+00:00 => 2011-10-05T00:00:00
   */
  def zoneDateTimeToDate(datetime: String): String = {
    val zonedDateTime = ZonedDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val utcDateTime = zonedDateTime
      .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime
    utcDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0)
      .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  }

}
