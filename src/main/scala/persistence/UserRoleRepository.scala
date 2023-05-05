package eozel.persistence.user

import doobie._
import doobie.enumerated.SqlState
import doobie.implicits._
import doobie.implicits.legacy.instant._
import eozel.domain.error.{AlreadyExistsError, AppDaoError, AppError}
import eozel.domain.user._
import zio._
import zio.interop.catz._

trait UserRoleRepository {

  def createUserRole(userRole: UserRole): IO[AppError, Long]
  def getUserRoles(userId: Long): IO[AppError, List[UserRole]]
  def createUserRoleIO(userRole: UserRole): ConnectionIO[Long]

}
case class UserRoleRepositoryLive(xa: Transactor[Task]) extends UserRoleRepository {

  import UserRoleSQL._

  override def createUserRoleIO(userRole: UserRole): ConnectionIO[Long] = createUserRoleSQLIO(userRole)

  override def createUserRole(userRole: UserRole): IO[AppError, Long] =
    createUserRoleSQLIO(userRole).attemptSomeSqlState {
      case SqlState("23000") => AlreadyExistsError(s"The user with role - ${userRole.role} already exists")
      case state             => AppDaoError(s"The createUserRole has unexpected error with ${state.value}")
    }
      .transact(xa)
      .orDie
      .flatMap {
        case Right(id)   => ZIO.succeed(id)
        case Left(error) => ZIO.fail(error)
      }

  override def getUserRoles(userId: Long): IO[AppError, List[UserRole]] =
    getUserRolesIO(userId)
      .transact(xa)
      .orDie

}

object UserRoleRepositoryLive {

  val layer: ZLayer[Has[Transactor[Task]], Nothing, Has[UserRoleRepository]] =
    ZLayer.fromService[
      Transactor[Task],
      UserRoleRepository
    ](new UserRoleRepositoryLive(_))

}

object UserRoleSQL {

  implicit val roleMeta: Meta[Role] = Meta[String].imap(Role.withName)(_.entryName)

  def createUserRoleSQLIO(userRole: UserRole): ConnectionIO[Long] =
    sql"""INSERT INTO userRole(
                    userId,
                    role,
                    created,
                    isActive) VALUES (
                    ${userRole.userId},
                    ${userRole.role},
                    ${userRole.created},
                    ${userRole.isActive}
                    )""".update
      .withUniqueGeneratedKeys[Long]("id")

  def getUserRolesIO(userId: Long): ConnectionIO[List[UserRole]] =
    sql"""SELECT  id,
                    userId,
                    role,
                    created,
                    isActive
                    FROM userRole
                    WHERE isActive = ${true} AND userId = ${userId}"""
      .query[UserRole]
      .to[List]

}
