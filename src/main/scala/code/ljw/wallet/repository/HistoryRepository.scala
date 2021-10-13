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

  override def update(session: ScalikeJdbcSession, datetime: String, amount: Double): Unit = {
      session.db.withinTx { implicit dbSession =>
        // insert new value or update value
        sql"""
           INSERT INTO btc_wallet_history (datetime, amount) VALUES ($datetime, $amount)
           ON CONFLICT (datetime) DO UPDATE SET amount = btc_wallet_history.amount + $amount
         """.executeUpdate().apply()
      }

  }

  override def btcHistory(session: ScalikeJdbcSession,
                          datetimeFrom: String,
                          datetimeTo: String): List[BtcDailyTotal] = {
    session.db.withinTx { implicit dbSession =>
      logger.info(s"HistoryRepositoryImpl.btcHistory ${datetimeFrom} to ${datetimeTo}")
      val sqlStmt = sql"""
           SELECT datetime, amount FROM btc_wallet_history WHERE $datetimeFrom <= datatime AND datatime <= $datetimeTo
         """

      logger.info(sqlStmt.statement)
      val btcHistory = sqlStmt
        .map(implicit rs => BtcDailyTotal(BtcDailyTotal.syntax.resultName))
        .list().apply()
      logger.info(btcHistory.mkString(", "))
      btcHistory
    }
  }

}
