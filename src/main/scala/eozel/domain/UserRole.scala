package eozel.domain

import enumeratum._

import java.time.Instant

sealed trait Role extends EnumEntry

case object Role extends Enum[Role] with CirceEnum[Role] {
  case object Admin       extends Role
  case object Responsible extends Role
  val values = findValues
}

case class UserRole(id: Long, userId: Long, role: Role, created: Instant, isActive: Boolean) {
  def this(userId: Long, role: Role) = this(
    0L,
    userId,
    role,
    Instant.now(),
    true
  )
}
