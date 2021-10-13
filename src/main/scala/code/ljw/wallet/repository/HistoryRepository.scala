package code.ljw.wallet.repository

import org.slf4j.LoggerFactory
import scalikejdbc._

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
           INSERT INTO btc_wallet_history (datetime, amount) VALUES ($utcDateTime, $amount)
           ON CONFLICT (datetime) DO UPDATE SET amount = btc_wallet_history.amount + $amount
         """.executeUpdate().apply()
      }

  }

  override def btcHistory(session: ScalikeJdbcSession,
                          datetimeFrom: String,
                          datetimeTo: String): List[BtcDailyTotal] = {

    session.db.begin()
    session.db.withinTx { implicit dbSession =>

      val from = DateTime.zoneDateTimeToZeroedTime(datetimeFrom)
      val to = DateTime.zoneDateTimeToZeroedTime(datetimeTo)

      logger.info(s"HistoryRepositoryImpl.btcHistory ${datetimeFrom} to ${datetimeTo}")
      val sqlStmt = sql"""
           SELECT datetime, amount FROM btc_wallet_history WHERE $from <= datetime AND datetime <= $to
         """

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
