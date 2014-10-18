package controllers

import auth.WithDomain
import play.api.mvc._
import securesocial.core.{BasicProfile, RuntimeEnvironment}

class ConfigurationController(override implicit val env: RuntimeEnvironment[BasicProfile]) extends Controller
                                      with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction(WithDomain()) {
    implicit request =>
      Ok(views.html.configurations.index(request.user))
  }

}