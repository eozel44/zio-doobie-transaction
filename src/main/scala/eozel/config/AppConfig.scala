package eozel.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio._

import scala.util.control.NoStackTrace

case class DBConfig(
  server: String,
  database: String,
  user: String,
  password: String,
  schema: Option[String],
  port: Int,
  ssl: Boolean,
  poolSize: Int,
  connectionTimeout: Int,
  keepaliveTime: Int,
  maxLifetime: Int,
  socketTimeout: Int
)

case class AppConfig(
  db: DBConfig
  //more http, security
)

object AppConfig {

  def live: ZLayer[Any, Throwable, Has[AppConfig]] =
    ZIO
      .fromEither(ConfigSource.default.load[AppConfig])
      .foldM(
        err => ZIO.fail(new IllegalArgumentException(s"config error: $err") with NoStackTrace),
        v => ZIO(v)
      )
      .toLayer
}
