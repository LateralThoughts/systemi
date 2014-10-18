package controllers

import auth.WithDomain
import play.api.mvc.Controller
import securesocial.core.{BasicProfile, RuntimeEnvironment}


class MovementsController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction(WithDomain()) { implicit request =>
    Ok(views.html.movements.index(request.user))
  }

}
