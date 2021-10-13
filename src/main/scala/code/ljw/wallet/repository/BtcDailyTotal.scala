package code.ljw.wallet.repository


import scalikejdbc._

case class BtcDailyTotal(datetime: java.sql.Timestamp , amount: Double)

object BtcDailyTotal extends SQLSyntaxSupport[BtcDailyTotal] {
  override val tableName = "btc_wallet_history"

  def apply(btcResult: ResultName[BtcDailyTotal])(implicit rs: WrappedResultSet) =
    new BtcDailyTotal(
      rs.timestamp(btcResult.datetime),
      rs.double(btcResult.amount))

}
