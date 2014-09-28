package controllers.api

import domain.{Client, InvoiceSerializer}
import play.api.libs.json.{JsError, JsResult, Json}
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import search.SimpleSearchEngine
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.util.{Failure, Success}


class ClientApiController(engine: SimpleSearchEngine)
                         (override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with InvoiceSerializer
  with securesocial.core.SecureSocial[BasicProfile] {

  import play.modules.reactivemongo.json.BSONFormats._
  import scala.concurrent.ExecutionContext.Implicits.global

  val collection = db.collection[JSONCollection]("clients")

  initIndex()

  def search(q: Option[String]) = SecuredAction.async {
    implicit request =>
      q match {
        case Some(query) =>
          val results = engine.search(query)
          val resultSelector = Json.obj {
            "_id" -> Json.obj {
              "$in" -> results.map(BSONObjectID(_))
            }
          }
          collection.find(resultSelector).cursor[Client].collect[List]().map {
            clients => Ok(Json.toJson(clients))
          }

        case None =>
          collection.find(Json.obj()).cursor[BSONDocument].collect[List]().map ( clients => Ok(Json.toJson(clients))
          )
      }
  }

  def addClient() = SecuredAction(parse.json) { implicit request =>
    request.body.validate(invoiceClientReads) match {
      case errors:JsError => Ok(errors.toString).as("application/json")
      case result: JsResult[Client] =>
        saveClient(result.get)
        Ok
    }
  }

  def modifyClient(id: String) = SecuredAction(parse.json) {
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
