package controllers

import play.api.mvc._
import play.api.libs.json._
import search.engine.SimpleSearchEngine
import domain.{NewClientDefinition, InvoiceRequest, InvoiceSerializer, ClientDefinition}
import oauth.GoogleOAuth


object Client extends Controller with InvoiceSerializer {

  private val clients = collection.mutable.Map(
    0 -> ClientDefinition("0", "VIDAL", "21 rue camille desmoulins", "92110", "Issy les moulineaux"),
    1 -> ClientDefinition("1", "Lateral-Thoughts", "37 rue des mathurins", "75009", "Paris")
  )
  private val engine = SimpleSearchEngine(clients)

  def clientsView = Action {
    implicit request =>
      Ok(views.html.clients(GoogleOAuth.getGoogleAuthUrl))
  }

  def getAll = Action {
    implicit request =>
      Ok(Json.toJson(clients.values))
  }

  def search(q: String) = Action {
    implicit request =>
      Ok(Json.toJson(engine.search(q)))
  }

  def addClient() = Action { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate(invoiceNewClientReads) match {
        case errors:JsError => Ok(errors.toString).as("application/json")
        case result: JsResult[NewClientDefinition] => {
          saveClient(result.get)
          Ok
        }
      }
      case None => BadRequest
    }
  }

  def modifyClient(id: Int) = Action {
    implicit request =>
      request.body.asJson match {
        case Some(json) => json.validate(invoiceClientReads) match {
          case errors:JsError => Ok(errors.toString).as("application/json")
          case result: JsResult[ClientDefinition] => {
            clients.update(id, result.get)
            engine.update(id, result.get)
            Ok
          }
        }
        case None => BadRequest
      }
  }


  private def saveClient(clientProposal: NewClientDefinition) = {
    val newIndex = clients.size
    val client = new ClientDefinition(newIndex.toString, clientProposal)
    clients += (newIndex -> client)
    engine.addToIndex(newIndex, client)
  }
}
