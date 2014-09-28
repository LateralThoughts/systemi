package controllers

import play.api.mvc._
import securesocial.core.{BasicProfile, RuntimeEnvironment}

class InvoiceController(override implicit val env: RuntimeEnvironment[BasicProfile]) extends Controller
                                      with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction {
    implicit request =>
      Ok(views.html.invoice_form(request.user))
  }

  def cra = SecuredAction {
    implicit request =>
      Ok(views.html.cra(request.user))
  }
}