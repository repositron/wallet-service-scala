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

  // commands are the external API of an entity
  /**
   * This interface defines all the commands (messages) that the ShoppingCart actor supports.
   */
  sealed trait Command extends CborSerializable

  final case class AddItem(
                            dateTime: String,
                            amount: Double,
                            replyTo: ActorRef[StatusReply[Summary]]) extends Command


  private def handleCommand(
                             datetime: String,
                             state: State,
                             command: Command): ReplyEffect[Event, State] = {
    command match {
      case AddItem(datetime, amount, replyTo) =>
        if (state.hasItem(datetime))
          Effect.reply(replyTo)(
            StatusReply.Error(
              s"Item '$datetime' was already added to this shopping cart"))
        else
          Effect
            .persist(ItemAdded(datetime, amount))
            .thenReply(replyTo) { updatedCart =>
              StatusReply.Success(Summary(updatedCart.items))
            }
    }
  }

  final case class State(items: Map[String, Double]) extends CborSerializable {

    def hasItem(datetime: String): Boolean =
      items.contains(datetime)

    def isEmpty: Boolean =
      items.isEmpty

    def updateItem(datetime: String, amount: Double): State = {
      copy(items = items + (datetime -> amount))
    }
  }


  object State {
    val empty = State(items = Map.empty)
  }

  val EntityKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("Wallet")

  def init(system: ActorSystem[_]): Unit = {
    ClusterSharding(system).init(Entity(EntityKey) { entityContext =>
      Wallet(entityContext.entityId)
    })
  }

  def apply(datetime: String): Behavior[Command] = {
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, State](
        persistenceId = PersistenceId(EntityKey.name, datetime),
        emptyState = State.empty,
        commandHandler =
          (state, command) => handleCommand(datetime, state, command),
        eventHandler = (state, event) => handleEvent(state, event))
      .withRetention(RetentionCriteria
        .snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(
        SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1)
      )
  }


  sealed trait Event extends CborSerializable {
    def datetime: String
  }


  final case class ItemAdded(datetime: String, amount: Double)
    extends Event


  /**
   * Summary of the shopping cart state, used in reply messages.
   */
  final case class Summary(items: Map[String, Double]) extends CborSerializable

/*  def init(system: ActorSystem[_]): Unit = {
    val behaviorFactory: EntityContext[Command] => Behavior[Command] = {
      entityContext =>
        val i = math.abs(entityContext.entityId.hashCode % tags.size)
        val selectedTag = tags(i)
        Wallet(entityContext.entityId, selectedTag)
    }
    ClusterSharding(system).init(Entity(EntityKey)(behaviorFactory))
  }

  def apply(cartId: String, projectionTag: String): Behavior[Command] = {
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, State](
        persistenceId = PersistenceId(EntityKey.name, cartId),
        emptyState = State.empty,
        commandHandler =
          (state, command) => handleCommand(cartId, state, command),
        eventHandler = (state, event) => handleEvent(state, event))
      .withTagger(_ => Set(projectionTag))
      .withRetention(RetentionCriteria
        .snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(
        SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }*/

 /* private def handleCommand(
                             datetime: String,
                             state: State,
                             command: Command): ReplyEffect[Event, State] = {
    // The shopping cart behavior changes if it's checked out or not.
    // The commands are handled differently for each case.
    if (state.isCheckedOut)
      checkedOutShoppingCart(cartId, state, command)
    else
      openShoppingCart(cartId, state, command)
  }*/


  private def handleEvent(state: State, event: Event) = {
    event match {
      case ItemAdded(datetime, amount) =>
        state.updateItem(datetime, amount)
    }
  }

}
