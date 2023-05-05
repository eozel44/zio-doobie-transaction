package eozel.domain.user

case class User(
  id: Long,
  email: String,
  password: String,
  phone: String,
  firstName: String,
  lastName: String
)
