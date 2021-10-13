package code.ljw.wallet

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.ShardedDaemonProcessSettings
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.Offset
import akka.projection.{ProjectionBehavior, ProjectionId}
import akka.projection.eventsourced.EventEnvelope
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projection.jdbc.scaladsl.JdbcProjection
import akka.projection.scaladsl.{ExactlyOnceProjection, SourceProvider}
import code.ljw.wallet.repository.{HistoryRepository, ScalikeJdbcSession}

object BtcHistoryProjection {
  def init(
            system: ActorSystem[_],
            repository: HistoryRepository): Unit = {
    ShardedDaemonProcess(system).init(
      name = "BtcHistoryProjection",
      Wallet.tags.size,
      index =>
        ProjectionBehavior(createProjectionFor(system, repository, index)),
      ShardedDaemonProcessSettings(system),
      Some(ProjectionBehavior.Stop))
  }


  private def createProjectionFor(
                                   system: ActorSystem[_],
                                   repository: HistoryRepository,
                                   index: Int)
  : ExactlyOnceProjection[Offset, EventEnvelope[Wallet.Event]] = {
    val tag = Wallet.tags(index)

    val sourceProvider
    : SourceProvider[Offset, EventEnvelope[Wallet.Event]] =
      EventSourcedProvider.eventsByTag[Wallet.Event](
        system = system,
        readJournalPluginId = JdbcReadJournal.Identifier,
        tag = tag)

    JdbcProjection.exactlyOnce(
      projectionId = ProjectionId("BtcHistoryProjection", tag),
      sourceProvider,
      handler = () =>
        new BtcHistoryProjectionHandler(tag, system, repository),
      sessionFactory = () => new ScalikeJdbcSession())(system)
  }
}
