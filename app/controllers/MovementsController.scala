package controllers

import play.api.mvc.Controller
import securesocial.core.{BasicProfile, RuntimeEnvironment}


class MovementsController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction { implicit request =>
    Ok(views.html.movements.index(request.user))
  }

}
