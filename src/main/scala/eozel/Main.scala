package eozel

import eozel.config.AppConfig
import eozel.module.LmsConnectionPool
import eozel.persistence.{UserRepositoryLive, UserRoleRepositoryLive}
import eozel.service.UserServiceLive
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{LogAnnotation, Logging, log}
import zio.{App, ExitCode, ULayer, ZEnv, ZIO}
import doobie.Transactor
import eozel.persistence.{UserRepository, UserRoleRepository}
import eozel.service.UserService
import zio.{Has, Task, ZLayer}

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
      LmsConnectionPool.live >+> UserRepositoryLive.layer >+> UserRoleRepositoryLive.layer >+>
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
