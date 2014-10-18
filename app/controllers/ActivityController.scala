package controllers

import auth.WithDomain
import securesocial.core.{BasicProfile, RuntimeEnvironment}
import play.api.mvc._

class ActivityController(override implicit val env: RuntimeEnvironment[BasicProfile]) extends Controller
with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction(WithDomain()) {
    implicit request =>
      Ok(views.html.activity.index(request.user))
  }

}
