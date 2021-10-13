package code.ljw.wallet.repository

import org.slf4j.LoggerFactory
import scalikejdbc._

import java.time.{LocalDate}
import java.time.format.DateTimeFormatter

trait HistoryRepository {
  def update(
              session: ScalikeJdbcSession,
              datetime: String,
              amount: Double
            ): Unit
  def btcHistory(
                  session: ScalikeJdbcSession,
                  datetimeFrom: String,
                  datetimeTo: String
                ): List[BtcDailyTotal]
}

class HistoryRepositoryImpl extends HistoryRepository {
  private val logger = LoggerFactory.getLogger(getClass)

  /**
   * inserts a datetime, amount in the btc_wallet.history.table. If row
   * already exists then it will add the amount to the existing row
   */
  override def update(session: ScalikeJdbcSession, datetime: String, amount: Double): Unit = {
      session.db.withinTx { implicit dbSession =>
        // insert new value or update value
        val utcDateTime = DateTime.zonedDateTimeStrToUtc(datetime)
        sql"""
           INSERT INTO btc_wallet_history (btcdatetime, amount) VALUES ($utcDateTime, $amount)
           ON CONFLICT (btcdatetime) DO UPDATE SET amount = btc_wallet_history.amount + $amount
         """.executeUpdate().apply()
      }

  }

  override def btcHistory(session: ScalikeJdbcSession,
                          datetimeFrom: String,
                          datetimeTo: String): List[BtcDailyTotal] = {

    session.db.begin()
    session.db.withinTx { implicit dbSession =>

      val (from, to) = DateTime.range(datetimeFrom, datetimeTo)

      //TO_TIMESTAMP('2019-10-04', 'YYYY-MM-DD')
      val fromDt = LocalDate.parse(from, DateTimeFormatter.ISO_DATE)
      val toDt = LocalDate.parse(to, DateTimeFormatter.ISO_DATE)
      logger.info(s"HistoryRepositoryImpl.btcHistory ${datetimeFrom} to ${datetimeTo}")

     /* val sqlStmt = SQL("SELECT datetime, amount FROM btc_wallet_history WHERE '{f}' <= datetime AND datetime <= '{t}'")
        .bindByName(fromDt -> 'f, toDt -> 't)
*/
        val sqlStmt = sql"SELECT btcdatetime, amount FROM btc_wallet_history WHERE ${fromDt} <= btcdatetime AND btcdatetime <= ${toDt}"
//      val sqlStmt = sql"SELECT datetime, amount FROM btc_wallet_history WHERE TO_TIMESTAMP('${from}', 'YYYY-MM-DD') <= datetime AND datetime <= TO_TIMESTAMP('${to}', 'YYYY-MM-DD')"


      logger.info(sqlStmt.statement)
      val btcHistory = sqlStmt
        .map(implicit rs => BtcDailyTotal(BtcDailyTotal.syntax.resultName))
        .list().apply()
      session.commit()
      logger.info(btcHistory.mkString(", "))
      btcHistory
    }
  }

}
