package controllers

import play.api.mvc._
import play.api.libs.json._
import play.api.db.slick._
import domain._
import oauth.GoogleOAuth
import search.engine.SimpleSearchEngine
import domain.NewClientDefinition
import domain.ClientDefinition
import scala.Some

object Client extends Controller with InvoiceSerializer {

  private val engine = SimpleSearchEngine()

  def clientsView = Action {
    implicit request =>
      Ok(views.html.clients(GoogleOAuth.getGoogleAuthUrl))
  }

  def getAll = DBAction {
    implicit rs =>
      val clients = Clients.list()
      Ok(Json.toJson(clients))
  }

  def search(q: String) = Action {
    implicit request =>
      Ok(Json.toJson(engine.search(q)))
  }

  def addClient = DBAction(parse.json) { implicit rs =>
    val json = rs.request.body
    json.validate(invoiceNewClientReads) match {
      case errors:JsError => Ok(errors.toString).as("application/json")
      case result: JsResult[NewClientDefinition] => {
        saveClient(result.get)
        Ok
      }
    }
  }

  def modifyClient(id: Long) = DBAction(parse.json) {
    implicit rs =>
      val json = rs.request.body
      json.validate(invoiceClientReads) match {
        case errors:JsError => Ok(errors.toString).as("application/json")
        case result: JsResult[ClientDefinition] => {
          Clients.update(id, result.get)
          engine.update(id.toString, result.get)
          Ok
        }
      }
  }


  private def saveClient(clientProposal: NewClientDefinition)(implicit s: play.api.db.slick.Config.driver.simple.Session) = {
    val client = new ClientDefinition(None, clientProposal)
    Clients.insert(client)
    engine.addToIndex(client)
  }
}
