package code.ljw.wallet.history


import scalikejdbc._

case class BtcDailyTotal(datetime: java.sql.Timestamp , amount: Double)
case class BtcDailyTotalMember(datetime: java.sql.Timestamp , amount: Double)

object BtcDailyTotal extends SQLSyntaxSupport[BtcDailyTotal] {
  override val tableName = "btc_wallet_history"

  def apply(g: ResultName[BtcDailyTotal])(rs: WrappedResultSet) =
    new BtcDailyTotal(rs.timestamp(g.datetime), rs.double(g.amount))
}

object BtcDailyTotalMember extends SQLSyntaxSupport[BtcDailyTotalMember] {
  def apply(m: ResultName[BtcDailyTotalMember])(rs: WrappedResultSet) =
    new BtcDailyTotalMember(rs.long(m.id), rs.string(m.name), rs.longOpt(m.groupId))

  def apply(m: ResultName[BtcDailyTotalMember], g: ResultName[BtcDailyTotal])(rs: WrappedResultSet) =  {
    apply(m)(rs).copy(group = rs.longOpt(g.id).map(_ => BtcDailyTotal(g)(rs)))
  }
}