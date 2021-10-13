package code.ljw.wallet.repository


import scalikejdbc._

case class BtcDailyTotal(btcdatetime: java.sql.Date, amount: Double)

object BtcDailyTotal extends SQLSyntaxSupport[BtcDailyTotal] {
  override val tableName = "btc_wallet_history"
  override val columns = Seq("btcdatetime", "amount")
  override val useSnakeCaseColumnName = false

  def apply(btcResult: ResultName[BtcDailyTotal])(implicit rs: WrappedResultSet) =
    new BtcDailyTotal(
      rs.date(btcResult.btcdatetime),
      rs.double(btcResult.amount))
}
