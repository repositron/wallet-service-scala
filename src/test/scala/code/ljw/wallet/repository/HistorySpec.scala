package code.ljw.wallet.repository

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.cluster.MemberStatus
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{Cluster, Join}
import akka.persistence.testkit.scaladsl.PersistenceInit
import code.ljw.wallet.{BtcHistoryProjection, Wallet}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.OptionValues
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object HistorySpec {
  val config: Config =
    ConfigFactory.load("history-integration-test.conf")
}

class HistorySpec
    extends ScalaTestWithActorTestKit(HistorySpec.config)
    with AnyWordSpecLike
    with OptionValues {

  private lazy val historyRepository =
    new HistoryRepositoryImpl()

  override protected def beforeAll(): Unit = {
    ScalikeJdbcSetup.init(system)
    CreateTableTestUtils.dropAndRecreateTables(system)
    // avoid concurrent creation of keyspace and tables
    val timeout = 10.seconds
    Await.result(
      PersistenceInit.initializeDefaultPlugins(system, timeout),
      timeout)

    Wallet.init(system)

    BtcHistoryProjection.init(system, historyRepository)

    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
  }

  "History projection" should {
    "init and join Cluster" in {
      Cluster(system).manager ! Join(Cluster(system).selfMember.address)

      // let the node join and become Up
      eventually {
        Cluster(system).selfMember.status should ===(MemberStatus.Up)
      }
    }

    "consume Btc events and add to repository" in {
      val sharding = ClusterSharding(system)

      val d1zone1 = "2019-10-05T14:48:01+01:00"
      val d2zone1 = "2019-10-05T15:45:01+01:00"

      val from = "2019-10-01T14:48:01+01:00"
      val to = "2019-10-04T14:48:01+01:00"

      val d1 = sharding.entityRefFor(Wallet.EntityKey, d1zone1)
      val d2 = sharding.entityRefFor(Wallet.EntityKey, d2zone1)

      val reply1: Future[Boolean] =
        d1.askWithStatus(
          Wallet.AddBtc("2019-10-05T14:41:01+01:00", 200.2, _))
      reply1.futureValue should === (true)
      val reply2: Future[Boolean] =
        d2.askWithStatus(
          Wallet.AddBtc("2019-10-05T15:45:01+01:00", 20.0, _))
      reply2.futureValue should === (true)

      eventually {
        ScalikeJdbcSession.withSession { session =>
          val historyArray = historyRepository.btcHistory(session, d1zone1, d2zone1).toArray

          historyArray.size should equal (2)
          historyArray(0).amount should equal (200.2)
          historyArray(0).btcdatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) should equal ("2019-10-05T13:00:00")

          historyArray(1).amount should equal (20.0)
          historyArray(1).btcdatetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) should equal ("2019-10-05T14:00:00")


        }
      }
    }
  }
}
