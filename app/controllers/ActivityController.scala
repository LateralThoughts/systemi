package controllers

import securesocial.core.{BasicProfile, RuntimeEnvironment}
import play.api.mvc._

class ActivityController(override implicit val env: RuntimeEnvironment[BasicProfile]) extends Controller
with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction {
    implicit request =>
      Ok(views.html.cra.form(request.user))
  }

}
