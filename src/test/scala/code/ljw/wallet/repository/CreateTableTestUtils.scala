package code.ljw.wallet.repository

import akka.Done
import akka.actor.typed.ActorSystem
import akka.persistence.jdbc.testkit.scaladsl.SchemaUtils
import akka.projection.jdbc.scaladsl.JdbcProjection
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

object CreateTableTestUtils {

  private val createUserTablesFile =
    Paths.get("ddl-scripts/create-history-tables.sql").toFile

  def dropAndRecreateTables(system: ActorSystem[_]): Unit = {
    implicit val sys: ActorSystem[_] = system
    implicit val ec: ExecutionContext = system.executionContext

    // ok to block here, main thread
    Await.result(
      for {
        _ <- SchemaUtils.dropIfExists()
        _ <- SchemaUtils.createIfNotExists()
        _ <- JdbcProjection.dropOffsetTableIfExists(() =>
          new ScalikeJdbcSession())
        _ <- JdbcProjection.createOffsetTableIfNotExists(() =>
          new ScalikeJdbcSession())
      } yield Done,
      30.seconds)
    if (createUserTablesFile.exists()) {
      Await.result(
        for {
          _ <- dropUserTables()
          _ <- SchemaUtils.applyScript(createUserTablesSql)
        } yield Done,
        30.seconds)
      LoggerFactory
        .getLogger("code.ljw.wallet.repository.CreateTableTestUtils")
        .info("Created tables")
    }
    else
      throw new Exception(s"files don't exist")
  }

  private def dropUserTables()(
      implicit system: ActorSystem[_]): Future[Done] = {
    SchemaUtils.applyScript("DROP TABLE IF EXISTS public.btc_wallet_history;")
  }

  private def createUserTablesSql: String = {
    val source = scala.io.Source.fromFile(createUserTablesFile)
    val contents = source.mkString
    source.close()
    contents
  }
}
