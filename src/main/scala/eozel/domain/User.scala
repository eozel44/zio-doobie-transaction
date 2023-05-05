package eozel.domain

case class User(
  id: Long,
  email: String,
  password: String,
  phone: String,
  firstName: String,
  lastName: String
)
