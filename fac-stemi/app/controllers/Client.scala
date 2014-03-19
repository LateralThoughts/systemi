package controllers

import play.api.mvc._
import play.api.libs.json._
import search.engine.SimpleSearchEngine
import domain.{InvoiceSerializer, ClientDefinition}
import oauth.GoogleOAuth


object Client extends Controller with InvoiceSerializer {

  private val clients = Map(
    0 -> ClientDefinition("VIDAL", "21 rue camille desmoulins", "92110", "Issy les moulineaux"),
    1 -> ClientDefinition("Lateral-Thoughts", "37 rue des mathurins", "75009", "Paris")
  )
  private val engine = new SimpleSearchEngine(clients)




  def clientsView = Action {
    implicit request =>
      Ok(views.html.clients(GoogleOAuth.getGoogleAuthUrl))
  }

  def search(q: String) = Action {
    implicit request =>
      Ok(Json.toJson(engine.search(q)))
  }

}
