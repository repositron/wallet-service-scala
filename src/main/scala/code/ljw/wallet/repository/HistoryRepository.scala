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
        val utcDateTime = DateTime.zonedDateTimeStrToUtcDate(datetime)
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

      val b = BtcDailyTotal.syntax("b")
      val sqlStmt = sql"""
        SELECT ${b.btcdatetime}, ${b.amount}
        FROM ${BtcDailyTotal as b}
        WHERE ${fromDt} <= ${b.btcdatetime} AND ${b.btcdatetime} <= ${toDt}
        """

      logger.info(sqlStmt.statement)
      val btcHistory = sqlStmt
        .map(implicit rs => BtcDailyTotal(b.resultName))
        //.map(implicit rs => BtcDailyTotal(BtcDailyTotal.syntax.resultName))
        .list().apply()
      session.commit()
      logger.info(btcHistory.mkString(", "))
      btcHistory
    }
  }

}
