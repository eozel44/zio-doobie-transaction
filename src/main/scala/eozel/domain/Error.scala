package eozel.domain

sealed trait AppError                          extends Throwable
case class AppDaoError(message: String)        extends RuntimeException(message) with AppError
case class AppConfigError(message: String)     extends IllegalStateException(message) with AppError
case class AlreadyExistsError(message: String) extends RuntimeException(message) with AppError
case class NotFoundError(message: String)      extends RuntimeException(message) with AppError

