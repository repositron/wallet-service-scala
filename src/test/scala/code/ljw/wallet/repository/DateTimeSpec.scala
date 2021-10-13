package code.ljw.wallet.repository

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class DateTimeSpec extends AnyFlatSpec  {

  "A datetime str with time zone" should "be converted to utc date time without timezone" in {
    val utc = DateTime.zonedDateTimeStrToUtc("2011-10-05T10:48:01+00:00")
    utc should equal("2011-10-05T10:48:01")
  }

  "A datetime str with time zone + 5" should "be converted to utc date time without timezone" in {
    val utc = DateTime.zonedDateTimeStrToUtc("2011-10-05T10:48:01+05:00")
    utc should equal("2011-10-05T05:48:01")
  }

  "A datetime str with time zone + 5" should
    "be converted to utc date time without timezone and time 00:00:00" in {
    val utc = DateTime.zonedDateTimeStrToUtc("2011-10-05T10:48:01+05:00")
    utc should equal("2011-10-05T00:00:00")
  }

}
