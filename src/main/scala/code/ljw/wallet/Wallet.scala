package code.ljw.wallet


import java.time.Instant
import scala.concurrent.duration._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.sharding.typed.scaladsl.EntityContext
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.ReplyEffect
import akka.persistence.typed.scaladsl.RetentionCriteria

object Wallet {

  /**
   * This interface defines all the commands (messages) that the Wallet actor supports.
   */
  sealed trait Command extends CborSerializable

  final case class AddBtc(
      dateTime: String,
      amount: Double,
      replyTo: ActorRef[StatusReply[Boolean]]) extends Command

  private def handleCommand(
   datetime: String,
   state: State,
   command: Command): ReplyEffect[Event, State] = {
    command match {
      case AddBtc(datetime, amount, replyTo) =>
        Effect
          .persist(BtcAdded(datetime, amount))
          .thenReply(replyTo) { updatedWallet =>
            StatusReply.Success(true)
          }
    }
  }

  /**
   *  State is a map of date to a list of amounts
   */
  final case class State(btcPayments: Map[String, List[Double]]) extends CborSerializable {

    def isEmpty: Boolean =
      btcPayments.isEmpty


    def updateItem(datetime: String, amount: Double): State = {
      btcPayments.get(datetime) match {
        case Some(a) => copy(btcPayments = btcPayments + (datetime -> (amount :: a)))
        case None => copy(btcPayments = btcPayments + (datetime -> List(amount)))
      }
    }
  }

  object State {
    val empty = State(btcPayments = Map.empty)
  }

  val EntityKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("Wallet")

  /**
   * User several tags to distribute over several projection instances
   */
  val tags = Vector.tabulate(5)(i => s"wallet-tags-$i")

  def init(system: ActorSystem[_]): Unit = {
    val behaviorFactory: EntityContext[Command] => Behavior[Command] = {
      entityContext =>
        val i = math.abs(entityContext.entityId.hashCode % tags.size)
        val selectedTag = tags(i)
        Wallet(entityContext.entityId, selectedTag)
    }

    ClusterSharding(system).init(Entity(EntityKey)(behaviorFactory))
  }

  def apply(walletId: String, projectionTag: String): Behavior[Command] = {
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, State](
        persistenceId = PersistenceId(EntityKey.name, walletId),
        emptyState = State.empty,
        commandHandler =
          (state, command) => handleCommand(walletId, state, command),
        eventHandler = (state, event) => handleEvent(state, event))
      .withTagger(_ => Set(projectionTag))
      .withRetention(RetentionCriteria
        .snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(
        SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1)
      )
  }


  sealed trait Event extends CborSerializable {
    def datetime: String
  }


  final case class BtcAdded(datetime: String, amount: Double)
    extends Event

  private def handleEvent(state: State, event: Event) = {
    event match {
      case BtcAdded(datetime, amount) =>
        state.updateItem(datetime, amount)
    }
  }

}
