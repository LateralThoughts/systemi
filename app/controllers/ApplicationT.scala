package controllers

import securesocial.core._
import play.api.mvc.{ Action, RequestHeader }

class ApplicationT(override implicit val env: RuntimeEnvironment[BasicProfile]) extends securesocial.core.SecureSocial[BasicProfile] {
  def index = SecuredAction { implicit request =>
    Ok(views.html.index(request.user))
  }
}