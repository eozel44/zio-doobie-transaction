# Functional Transaction Management

Build your transactions in purely functional way. 
With zio, doobie & postgres.

### Dependencies:

```scala
val zio         = "1.0.11"
val doobie      = "1.0.0-RC2"
val catsInterop = "3.1.1.0"
val postgres    = "42.3.5"
val hikari      = "4.0.3"
val zioLogging  = "0.5.10"
val circe       = "0.14.1"
val circeEnum   = "1.7.0"
val pureConfig  = "0.17.0"   
```

### Code:

```scala
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
```
### run:

1- execute scripts in resources/migration
2- sbt run


### keywords:
zio, doobie, postgres, transaction