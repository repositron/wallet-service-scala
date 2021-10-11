package code.ljw.wallet.history

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
                ): List[String]
}

class HistoryRepositoryImpl extends HistoryRepository {
  override def update(session: ScalikeJdbcSession, datetime: String, amount: Double): Unit = {
      session.db.withinTx { implicit dbSession =>
        // insert new value or update value
        sql"""
           INSERT INTO btc_wallet_history (datetime, amount) VALUES ($datetime, $amount)
           ON CONFLICT (datetime) DO UPDATE SET amount = btc_wallet_history.amount + $amount
         """.executeUpdate().apply()
      }

  }

  override def btcHistory(session: ScalikeJdbcSession, datetimeFrom: String, datetimeTo: String): List[String] = {
    session.db.withinTx { implicit dbSession =>
    sql"""
           SELECT datetime, amount FROM btc_wallet_history WHERE datetimeFrom <= $datatime AND datatime <= $datetimeTo
         """
      .map(rs => GroupMember(rs))
    }
  }
}