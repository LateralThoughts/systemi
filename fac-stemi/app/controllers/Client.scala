package controllers

import play.api.mvc._
import play.api.libs.json._
import domain._
import oauth.GoogleOAuth
import search.engine.SimpleSearchEngine
import domain.NewClientDefinition
import domain.ClientDefinition
import scala.concurrent.ExecutionContext
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.util.{Success, Failure}

// Reactive Mongo imports

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection


object Client extends Controller
with InvoiceSerializer
with MongoController {
  import play.modules.reactivemongo.json.BSONFormats._
  import ExecutionContext.Implicits.global

  def collection = db.collection[JSONCollection]("clients")

  private val engine = SimpleSearchEngine(collection.find(Json.obj()).cursor[ClientDefinition].collect[List]())

  def clientsView = Action {
    implicit request =>
      Ok(views.html.clients(GoogleOAuth.getGoogleAuthUrl))
  }

  def getAll = Action.async {
    implicit request =>
      val futureClients = collection.find(Json.obj()).cursor[JsObject].collect[List]()
      futureClients.map (clients => Ok(JsArray(clients)))
  }

  def search(q: String) = Action {
    implicit request =>
      Ok(Json.toJson(engine.search(q)))
  }

  def addClient = Action(parse.json) { implicit request =>
    val json = request.body
    json.validate(invoiceNewClientReads) match {
      case errors:JsError => Ok(errors.toString).as("application/json")
      case result: JsResult[NewClientDefinition] => {
        saveClient(result.get)
        Ok
      }
    }
  }

  def modifyClient(id: String) = Action(parse.json) {
    implicit request =>
      val clientJsonModified = request.body
      val idSelector = Json.obj("_id" -> BSONObjectID(id))
      collection.update(idSelector, clientJsonModified).onComplete {
        case Failure(e) => throw e
        case Success(_) => /* Done */
      }
      collection.find(idSelector).one[ClientDefinition].map( _.map(client => engine.update(id, client)))
      Ok
  }


  private def saveClient(clientProposal: NewClientDefinition) = {
    val client = new ClientDefinition(None, clientProposal)
    collection.insert(client)
    engine.addToIndex(client)
  }
}
