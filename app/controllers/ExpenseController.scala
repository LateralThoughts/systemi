package controllers

import auth.WithDomain
import play.api.mvc._
import securesocial.core.{BasicProfile, RuntimeEnvironment}

class ExpenseController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction(WithDomain()) {
    implicit request =>
      Ok(views.html.expense.index(request.user))
  }

}
