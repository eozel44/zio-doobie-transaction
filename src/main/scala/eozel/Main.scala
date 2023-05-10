package eozel

import doobie.Transactor
import eozel.config.AppConfig
import eozel.module.ConnectionPool
import eozel.persistence.{UserRepository, UserRepositoryLive, UserRoleRepository, UserRoleRepositoryLive}
import eozel.service.{UserService, UserServiceLive}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{LogAnnotation, Logging, log}
import zio.{App, ExitCode, Has, Task, ULayer, ZEnv, ZIO, ZLayer}

object Main extends App {

  val loggingLayer: ULayer[Logging] = Slf4jLogger.make { (context, message) =>
    context.get(LogAnnotation.Cause) match {
      case Some(value) => s"$message cause:${value.prettyPrint}"
      case None        => message
    }
  }

  val appLayer: ZLayer[Any, Throwable, Clock with Blocking with Logging with Has[AppConfig] with Has[
    Transactor[Task]
  ] with Has[UserRepository] with Has[UserRoleRepository] with Has[UserService]] =
    //base layers
    Clock.live >+> Blocking.live >+> loggingLayer >+> AppConfig.live >+>
      //persistance layers
      ConnectionPool.layer >+> UserRepositoryLive.layer >+> UserRoleRepositoryLive.layer >+>
      //service layer
      UserServiceLive.layer

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {

    val program = for {
      _ <- log.info(s"starting")
      //us <- UserServiceLive.signupUser(new User(0L, "a", "b", "c", "d", "e"), Role.Admin)
      _ <- log.info(s"done")
    } yield ()

    program
      .provideLayer(appLayer)
      .exitCode

  }

}
