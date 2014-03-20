package controllers

import play.api.mvc._
import play.api.libs.json._
import domain._
import oauth.GoogleOAuth
import domain.Client
import scala.concurrent.ExecutionContext
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.util.{Success, Failure}
import search.SimpleSearchEngine

// Reactive Mongo imports

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection


object ClientController extends Controller
              with InvoiceSerializer
              with MongoController {
  import play.modules.reactivemongo.json.BSONFormats._
  import ExecutionContext.Implicits.global

  def collection = db.collection[JSONCollection]("clients")

  private val engine = SimpleSearchEngine()
  initIndex()

  def clientsView = Action {
    implicit request =>
      Ok(views.html.clients(GoogleOAuth.getGoogleAuthUrl))
  }

  def getAll = Action.async {
    implicit request =>
      val futureClients = collection.find(Json.obj()).cursor[BSONDocument].collect[List]()
      futureClients.map (
        clients => Ok(Json.toJson(clients))
      )
  }

  def search(q: String) = Action {
    implicit request =>
      Ok(Json.toJson(engine.search(q)))
  }

  def addClient = Action(parse.json) { implicit request =>
    val json = request.body
    json.validate(invoiceClientReads) match {
      case errors:JsError => Ok(errors.toString).as("application/json")
      case result: JsResult[Client] => {
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
        case Success(_) => collection.find(idSelector).one[Client].map( _.map(client => engine.update(id, client)))
      }
      Ok
  }


  private def saveClient(client: Client) = {
    collection.insert(client)
    engine.addToIndex(client)
  }

  private def initIndex() {
    collection
      .find(Json.obj())
      .cursor[Client]
      .collect[List]()
      .map(engine.initWithDocuments)
  }
}
