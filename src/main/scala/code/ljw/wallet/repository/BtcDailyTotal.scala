package code.ljw.wallet.repository


import scalikejdbc._

import java.time.LocalDateTime

case class BtcDailyTotal(btcdatetime: LocalDateTime, amount: Double)

object BtcDailyTotal extends SQLSyntaxSupport[BtcDailyTotal] {
  override val tableName = "btc_wallet_history"
  override val columns = Seq("btcdatetime", "amount")
  override val useSnakeCaseColumnName = false

  def apply(btcResult: ResultName[BtcDailyTotal])(implicit rs: WrappedResultSet) = {

    new BtcDailyTotal(
      btcdatetime = rs.localDateTime("btcdatetime"),
      amount = rs.double("amount")
    )
  }
}
