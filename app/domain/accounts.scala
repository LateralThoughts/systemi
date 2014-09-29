package domain

import org.bouncycastle.util.encoders.Base64
import org.joda.time.DateTime
import play.api.libs.json.{Json, Format, JsResult, JsValue}

case class User(userId: String, firstName: String, lastName: String, email: String, avatarUrl: String)

sealed trait Member
case class Human(user: User) extends Member
case object LT extends Member

case class Account(name: String, stakeholders: List[Member] = List())