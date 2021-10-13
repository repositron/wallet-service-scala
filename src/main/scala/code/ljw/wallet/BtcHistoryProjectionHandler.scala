package code.ljw.wallet

import akka.actor.typed.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import akka.projection.jdbc.scaladsl.JdbcHandler
import code.ljw.wallet.repository.{HistoryRepository, ScalikeJdbcSession}
import org.slf4j.LoggerFactory

class BtcHistoryProjectionHandler(
  tag: String,
  system: ActorSystem[_],
  repo: HistoryRepository)
  extends JdbcHandler[
    EventEnvelope[Wallet.Event],
    ScalikeJdbcSession]() {

    private val log = LoggerFactory.getLogger(getClass)

    override def process(
              session: ScalikeJdbcSession,
              envelope: EventEnvelope[Wallet.Event]): Unit = {
      envelope.event match {
        // should datetime be event
        case Wallet.BtcAdded(datetime, amount) =>
          repo.update(session, datetime, amount)
      }
    }

}
