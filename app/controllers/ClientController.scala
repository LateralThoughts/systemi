package controllers

import auth.WithDomain
import domain._
import play.api.mvc._
import securesocial.core.{BasicProfile, RuntimeEnvironment}

// Reactive Mongo imports

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController


class ClientController(override implicit val env: RuntimeEnvironment[BasicProfile]) extends Controller
              with InvoiceSerializer
              with MongoController
              with securesocial.core.SecureSocial[BasicProfile] {

  def clientsView = SecuredAction(WithDomain()) {
    implicit request =>
      Ok(views.html.clients(request.user))
  }
}
