package eozel.persistence

import doobie._
import doobie.enumerated.SqlState
import doobie.implicits._
import eozel.domain.{AlreadyExistsError, AppDaoError, AppError, NotFoundError, Role, User}
import zio._
import zio.interop.catz._

trait UserRepository {

  def createIO(user: User): ConnectionIO[Long]
  def getIO(id: Long): ConnectionIO[Option[User]]
  def create(user: User): IO[AppError, Long]
  def get(id: Long): IO[AppError, User]
}

case class UserRepositoryLive(xa: Transactor[Task]) extends UserRepository {

  import UserRepositorySQL._

  def createIO(user: User): ConnectionIO[Long]    = createSQLIO(user)
  def getIO(id: Long): ConnectionIO[Option[User]] = getSQLIO(id)

  override def create(user: User): IO[AppError, Long] =
    createSQLIO(user).attemptSomeSqlState {
      case SqlState("23000") => AlreadyExistsError(s"The user with mail ${user.email} already exists")
      case state             => AppDaoError(s"The user creation has unexpected error with ${state.value}")
    }
      .transact(xa)
      .orDie
      .flatMap {
        case Right(id)   => ZIO.succeed(id)
        case Left(error) => ZIO.fail(error)
      }

  override def get(id: Long): IO[AppError, User] =
    getSQLIO(id)
      .transact(xa)
      .orDie
      .flatMap {
        case Some(item) => ZIO.succeed(item)
        case None       => ZIO.fail(NotFoundError("user not found"))
      }
}

object UserRepositoryLive {

  val layer: ZLayer[Has[Transactor[Task]], Nothing, Has[UserRepository]] =
    ZLayer.fromService[
      Transactor[Task],
      UserRepository
    ](new UserRepositoryLive(_))
}

object UserRepositorySQL {

  implicit val roleMeta: Meta[Role] = Meta[String].imap(Role.withName)(_.entryName)

  def createSQLIO(user: User): ConnectionIO[Long] =
    sql"""INSERT INTO users (email, password, phone, firstName, lastName)
          VALUES ( ${user.email}, ${user.password}, ${user.phone}, ${user.firstName}, ${user.lastName})""".update
      .withUniqueGeneratedKeys[Long]("id")

  def getSQLIO(id: Long): ConnectionIO[Option[User]] =
    sql"""SELECT  
                    u.id,
                    u.email,
                    u.password,
                    u.phone,
                    u.firstName,
                    u.lastName
                    FROM users u
                    WHERE u.id = ${id}"""
      .query[User]
      .option

}
