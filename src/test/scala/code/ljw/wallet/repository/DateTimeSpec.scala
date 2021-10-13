package code.ljw.wallet.repository

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class DateTimeSpec extends AnyFlatSpec  {

  "A datetime str with time zone" should "be converted to utc date time without timezone" in {
    val utc = DateTime.zonedDateTimeStrToUtc("2011-10-05T10:48:01+00:00")
    utc should equal("2011-10-05")
  }

  "A datetime str with time zone + 5" should "be converted to utc date time without timezone" in {
    val utc = DateTime.zonedDateTimeStrToUtc("2011-10-05T10:48:01+05:00")
    utc should equal("2011-10-05")
  }

  "A datetime str range with time zone + 5" should "be converted to range " in {
    val from = "2011-10-05T10:48:01+05:00"
    val to = "2011-10-06T10:48:01+05:00"
    val (from2, to2) = DateTime.range(from, to)
    from2 should equal("2011-10-05")
    to2 should equal("2011-10-07")
  }



}
