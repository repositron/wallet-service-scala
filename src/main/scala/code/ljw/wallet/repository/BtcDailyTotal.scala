package code.ljw.wallet.repository


import scalikejdbc._

import java.time.LocalDate

case class BtcDailyTotal(btcdatetime: LocalDate, amount: Double)

object BtcDailyTotal extends SQLSyntaxSupport[BtcDailyTotal] {
  override val tableName = "btc_wallet_history"
  override val columns = Seq("btcdatetime", "amount")
  override val useSnakeCaseColumnName = false

  def apply(btcResult: ResultName[BtcDailyTotal])(implicit rs: WrappedResultSet) = {

    new BtcDailyTotal(
      btcdatetime = rs.localDate("btcdatetime"),
      amount = rs.double("amount")
    )
  }
}
