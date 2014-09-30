package domain

import securesocial.core._
import julienrf.variants.Variants
import play.api.libs.json.Json

sealed trait Member
case class Human(user: BasicProfile) extends Member
case class LT(underlying: String) extends Member

case class Account(name: String, stakeholder: Member)

trait BasicProfileSerializer {
  implicit val passwordInfoFormatter = Json.format[PasswordInfo]
  implicit val oAuth1InfoFormatter = Json.format[OAuth1Info]
  implicit val oAuth2InfoFormatter = Json.format[OAuth2Info]
  implicit val authMethodFormatter = Json.format[AuthenticationMethod]
  implicit val basicProfileFormatter = Json.format[BasicProfile]
}

trait MemberSerializer extends BasicProfileSerializer {
  implicit val memberFormatter = Variants.format[Member]
}

trait AccountSerializer extends MemberSerializer {
  implicit val accountFormatter = Json.format[Account]
}

