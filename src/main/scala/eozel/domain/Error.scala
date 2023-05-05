package eozel.domain

import io.circe._
import io.circe.generic.JsonCodec
import io.circe.syntax._

@JsonCodec sealed trait AppError                          extends Throwable
@JsonCodec case class AppDaoError(message: String)        extends RuntimeException(message) with AppError
@JsonCodec case class AppConfigError(message: String)     extends IllegalStateException(message) with AppError
@JsonCodec case class AlreadyExistsError(message: String) extends RuntimeException(message) with AppError
@JsonCodec case class NotFoundError(message: String)      extends RuntimeException(message) with AppError

object AppError {
  implicit val decodeAppError: Decoder[AppError] = Decoder[AppDaoError]
    .map[AppError](identity)
    .or(Decoder[AppConfigError].map[AppError](identity))
    .or(Decoder[AlreadyExistsError].map[AppError](identity))

  implicit val encodeAppError: Encoder[AppError] = Encoder.instance {
    case e1 @ AppDaoError(_)        => e1.asJson
    case e2 @ AppConfigError(_)     => e2.asJson
    case e3 @ AlreadyExistsError(_) => e3.asJson
    case e4 @ NotFoundError(_)      => e4.asJson
  }
}
