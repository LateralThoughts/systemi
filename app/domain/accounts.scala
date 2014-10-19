package domain

import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import julienrf.variants.Variants
import play.api.libs.json.{Format, Json}


sealed trait Member

case class Human(user: User) extends Member

case class LT(underlying: String) extends Member

case class Account(name: String, stakeholder: Member, affectable: Boolean = false)

trait UserSerializer {
  implicit val oAuth2InfoFormatter = Json.format[OAuth2Info]
  implicit val userFormatter = Json.format[User]
}

trait MemberSerializer extends UserSerializer {
  // Should explicitly declare Format[Member] type in order to avoid https://github.com/LateralThoughts/systemi/issues/24
  implicit val memberFormatter: Format[Member] = Variants.format[Member]
}

trait AccountSerializer extends MemberSerializer {
  implicit val accountFormatter = Json.format[Account]
}

