package eozel.module.persistence

import com.zaxxer.hikari.HikariConfig
import doobie.Transactor
import doobie.hikari.HikariTransactor
import eozel.config.AppConfig
import zio._
import zio.blocking.Blocking
import zio.clock.Clock

import java.util.Properties

object LmsConnectionPool {

  val live: RLayer[Has[AppConfig] with Clock with Blocking, Has[Transactor[Task]]] = {
    ZLayer.fromManaged(
      for {
        zRuntime <- ZIO.runtime[Clock with Blocking].toManaged_
        blocking <- ZIO.service[Blocking.Service].toManaged_
        dbConf   <- ZIO.service[AppConfig].map(_.db).toManaged_
        transactor <- {
          val poolSize = Math.max(dbConf.poolSize, 2)
          val props    = new Properties()
          props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
          props.setProperty("dataSource.serverName", dbConf.server)
          props.setProperty("dataSource.portNumber", dbConf.port.toString)
          props.setProperty("dataSource.databaseName", dbConf.database)
          dbConf.schema.map(props.setProperty("dataSource.currentSchema", _))
          props.setProperty("dataSource.user", dbConf.user)
          props.setProperty("dataSource.password", dbConf.password)
          props.setProperty("dataSource.ssl", dbConf.ssl.toString)
          props.setProperty("dataSource.socketTimeout", dbConf.socketTimeout.toString)
          props.setProperty("dataSource.sslfactory", "org.postgresql.ssl.NonValidatingFactory")
          props.setProperty("connectionTimeout", dbConf.connectionTimeout.toString)
          props.setProperty("keepaliveTime", dbConf.keepaliveTime.toString)
          props.setProperty("maximumPoolSize", poolSize.toString)
          props.setProperty("minimumIdle", poolSize.toString)
          props.setProperty("maxLifetime", dbConf.maxLifetime.toString)
          props.setProperty("dataSource.prepareThreshold", "0")
          props.setProperty("poolName", "lms_course_pool")
          import zio.interop.catz._
          implicit val rt: zio.Runtime[Clock with Blocking] = zRuntime
          HikariTransactor
            .fromHikariConfig[Task](
              hikariConfig = new HikariConfig(props),
              connectEC = blocking.blockingExecutor.asEC
            )
            .toManagedZIO
        }
      } yield transactor
    )
  }
}
