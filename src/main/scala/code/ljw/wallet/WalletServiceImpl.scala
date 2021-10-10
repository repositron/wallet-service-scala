package code.ljw.wallet

import java.util.concurrent.TimeoutException
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.grpc.GrpcServiceException
import akka.util.Timeout

import io.grpc.Status
import org.slf4j.LoggerFactory
import akka.actor.typed.ActorRef
import akka.pattern.StatusReply

case class WalletServiceImpl()//system: ActorSystem[_])
  extends proto.WalletService {
  //import system.executionContext

  private val logger = LoggerFactory.getLogger(getClass)

 /* implicit private val timeout: Timeout =
    Timeout.create(
      system.settings.config.getDuration("wallet-service.ask-timeout"))
*/
  //private val sharding = ClusterSharding(system)


  /*private val blockingJdbcExecutor: ExecutionContext =
    system.dispatchers.lookup(
      DispatcherSelector
        .fromConfig("akka.projection.jdbc.blocking-jdbc-dispatcher")
    )*/


  override def addBtc(in: proto.AddBtcRequest): Future[proto.AddBtcResponse] = {
    // could return total funds?
    logger.info("addBtc{} to cart {}", in.amount, in.datetime)
    Future.successful(
      proto.AddBtcResponse(true))
    /*
    val entityRef = sharding.entityRefFor(ShoppingCart.EntityKey, in.cartId)
    val reply: Future[ShoppingCart.Summary] =
      entityRef.askWithStatus(ShoppingCart.AddItem(in.itemId, in.quantity, _))
    val response = reply.map(cart => toProtoCart(cart))
    convertError(response)*/
  }




/*  private def convertError[T](response: Future[T]): Future[T] = {
    response.recoverWith {
      case _: TimeoutException =>
        Future.failed(
          new GrpcServiceException(
            Status.UNAVAILABLE.withDescription("Operation timed out")))
      case exc =>
        Future.failed(
          new GrpcServiceException(
            Status.INVALID_ARGUMENT.withDescription(exc.getMessage)))
    }
  }*/

}
