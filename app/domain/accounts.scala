package domain

import reactivemongo.bson.BSONObjectID
import securesocial.core._
import julienrf.variants.Variants
import play.api.libs.json.{Format, Json}


sealed trait Member

case class Human(user: BasicProfile) extends Member

case class LT(underlying: String) extends Member

case class Account(name: String, stakeholder: Member, affectable: Boolean = false, _id: Option[BSONObjectID] = None)

trait BasicProfileSerializer {
  implicit val passwordInfoFormatter = Json.format[PasswordInfo]
  implicit val oAuth1InfoFormatter = Json.format[OAuth1Info]
  implicit val oAuth2InfoFormatter = Json.format[OAuth2Info]
  implicit val authMethodFormatter = Json.format[AuthenticationMethod]
  implicit val basicProfileFormatter = Json.format[BasicProfile]
}

trait MemberSerializer extends BasicProfileSerializer {
  // Should explicitly declare Format[Member] type in order to avoid https://github.com/LateralThoughts/systemi/issues/24
  implicit val memberFormatter: Format[Member] = Variants.format[Member]
}

trait AccountSerializer extends MemberSerializer {
  import play.modules.reactivemongo.json.BSONFormats._

  implicit val accountFormatter = Json.format[Account]
}

