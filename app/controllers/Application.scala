package controllers

import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{Action, Controller}
import securesocial.core.{BasicProfile, RuntimeEnvironment}

class Application(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction {
    implicit request =>
      Ok(views.html.index(request.user))
  }
}