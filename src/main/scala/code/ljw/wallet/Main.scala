package code.ljw.wallet

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import code.ljw.wallet.repository.{HistoryRepositoryImpl, ScalikeJdbcSetup}
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

object Main {
  val logger = LoggerFactory.getLogger("wallet.Main")

  def main(args: Array[String]): Unit = {
    val system = ActorSystem[Nothing](Behaviors.empty, "WalletService")
    try {
      init(system)
    } catch {
      case NonFatal(e) =>
        logger.error("Terminating due to initialization failure.", e)
        system.terminate()
    }
  }

  def init(system: ActorSystem[_]): Unit = {
    ScalikeJdbcSetup.init(system)

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    Wallet.init(system)

    val historyRepository = new HistoryRepositoryImpl
    BtcHistoryProjection.init(system, historyRepository)

    val grpcInterface =
      system.settings.config.getString("wallet-service.grpc.interface")
    val grpcPort =
      system.settings.config.getInt("wallet-service.grpc.port")


    val grpcService = new WalletServiceImpl(system, historyRepository)
    WalletServer.start(grpcInterface, grpcPort, system, grpcService)
  }

}
