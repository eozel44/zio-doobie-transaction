package eozel.service

import doobie.Transactor
import doobie.implicits._
import eozel.config.AppConfig
import eozel.domain.{AppDaoError, AppError, Role, User, UserRole}
import eozel.persistence.{UserRepository, UserRoleRepository}
import zio._
import zio.interop.catz._
import zio.macros.accessible

@accessible
trait UserService {

  def signupUser(user: User, role: Role): ZIO[Any, AppError, User]

}

case class UserServiceLive(
  appConfig: AppConfig,
  userRepo: UserRepository,
  userRoleRepo: UserRoleRepository,
  tx: Transactor[Task]
) extends UserService {

  override def signupUser(user: User, role: Role): IO[AppError, User] = {

    val tranzactIO = for {
      id   <- userRepo.createIO(user)
      _    <- userRoleRepo.createUserRoleIO(new UserRole(id, role))
      user <- userRepo.getIO(id)
    } yield user

    tranzactIO.transact(tx).orDie.flatMap {
      case Some(item) => ZIO.succeed(item)
      case None       => ZIO.fail(AppDaoError("signup error"))
    }

  }

}

object UserServiceLive {

  val layer: ZLayer[Has[AppConfig] with Has[UserRepository] with Has[UserRoleRepository] with Has[
    Transactor[Task]
  ], Nothing, Has[UserService]] = ZLayer.fromServices[
    AppConfig,
    UserRepository,
    UserRoleRepository,
    Transactor[Task],
    UserService
  ](new UserServiceLive(_, _, _, _))

}
