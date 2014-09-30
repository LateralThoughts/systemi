package domain

import play.api.libs.json.Json
import securesocial.core._

sealed trait Member
case class Human(user: BasicProfile) extends Member
case object LT extends Member

case class Account(name: String, stakeholders: List[Member] = List())

trait BasicProfileSerializer {
    implicit val passwordInfoFormatter = Json.format[PasswordInfo]
    implicit val oAuth1InfoFormatter = Json.format[OAuth1Info]
    implicit val oAuth2InfoFormatter = Json.format[OAuth2Info]
    implicit val authMethodFormatter = Json.format[AuthenticationMethod]
    implicit val basicProfileFormatter = Json.format[BasicProfile]
}