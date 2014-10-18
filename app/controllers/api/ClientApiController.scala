package controllers.api

import auth.WithDomain
import domain.{ClientRequest, Client, InvoiceSerializer}
import play.Logger
import play.api.libs.json.{JsObject, JsError, JsResult, Json}
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import search.SimpleSearchEngine
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.Future
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

  def findAll() = {
    collection
      .find(Json.obj())
      .cursor[Client]
      .collect[List]()
  }

  def search(q: Option[String]) = SecuredAction(WithDomain()).async {
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
          collection
            .find(Json.obj())
            .cursor[JsObject]
            .collect[List]()
            .map ( clients => Ok(Json.toJson(clients)))
      }
  }

  def addClient() = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
    request.body.validate(clientRequestFormat) match {
      case errors:JsError =>
        Future(BadRequest(errors.toString).as("application/json"))

      case result: JsResult[ClientRequest] =>
        val clientRequest = result.get
        val clientId = BSONObjectID.generate

        val client = Client(
          clientId,
          clientRequest.name,
          clientRequest.address,
          clientRequest.postalCode,
          clientRequest.city,
          clientRequest.country,
          clientRequest.extraInfo)

        saveClient(client) map {
          case true => Ok
          case false => InternalServerError
        }
    }
  }

  def modifyClient(id: String) = SecuredAction(WithDomain())(parse.json) {
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
    val selector = Json.obj("name" -> Json.obj("$regex" -> client.name, "$options" -> "i"))
    collection
      .update(selector, Json.toJson(client), upsert = true)
      .map(errors => if (errors.inError) {

      Logger.error(s"Failed to upsert client $client")
      false
    } else {
      Logger.info("Successfully upserted client - add to index")
      engine.addToIndex(client)
      true
    })
  }

}
