package controllers

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain._

// Reactive Mongo imports

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController


class ClientController(override implicit val env: Environment[User, SessionAuthenticator]) extends Silhouette[User, SessionAuthenticator]
              with InvoiceSerializer
              with MongoController
               {

  def clientsView = SecuredAction {
    implicit request =>
      Ok(views.html.clients(request.identity))
  }
}
