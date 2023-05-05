package eozel

import doobie.Transactor
import eozel.config._
import eozel.module._
import eozel.persistence._
import eozel.service._
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{LogAnnotation, Logging}

object Main extends App {

  val loggingLayer: ULayer[Logging] = Slf4jLogger.make { (context, message) =>
    context.get(LogAnnotation.Cause) match {
      case Some(value) => s"$message cause:${value.prettyPrint}"
      case None        => message
    }
  }

  val appLayer =
      //base layers
      Clock.live >+> Blocking.live >+> loggingLayer >+> AppConfig.live >+>
      //persistance layers
      LmsConnectionPool.live >+> UserRepositoryLive.layer >+> UserRoleRepositoryLive.layer >+>
      //service layer
      UserServiceLive.layer

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {

    val program = for {      
       _ <- zio.log(s"starting")
      us      <- UserService.signupUser(new User(0L,"a","b","c","d","e"), Role.Admin)      
      _ <- zio.log(s"done")
    } yield us

    program
      .provideLayer(appLayer)
      .exitCode

  }

}
