package controllers

import play.api.mvc._
import securesocial.core.{BasicProfile, RuntimeEnvironment}

class ExpenseController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction {
    implicit request =>
      Ok(views.html.expense.index(request.user))
  }

}
