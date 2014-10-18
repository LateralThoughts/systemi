package auth

import play.api.{Play, Logger}
import play.api.mvc.RequestHeader
import securesocial.core.{BasicProfile, Authorization}

case class WithDomain() extends Authorization[BasicProfile] {

  private val DOMAIN = Play.maybeApplication.flatMap(_.configuration.getString("application.auth.domain")).getOrElse("all")

  override def isAuthorized(user: BasicProfile, request: RequestHeader): Boolean = {
    if (DOMAIN.equals("all")) {
      true
    } else {
      user.email match {
        case Some(email) => email.endsWith("@" + DOMAIN)
        case None => {
          Logger.warn("No email found in user profile, will refuse authorization")
          false
        }
      }
    }

  }
}
