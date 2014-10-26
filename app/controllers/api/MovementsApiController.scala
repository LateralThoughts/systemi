package controllers.api

import auth.WithDomain
import domain._
import play.api.Logger
import play.api.libs.json.{JsResult, JsError, JsObject, Json}
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.Future

class MovementsApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with AccountSerializer
  with MovementsSerializer
  with securesocial.core.SecureSocial[BasicProfile] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def findAll = SecuredAction(WithDomain()).async {
    db
      .collection[JSONCollection]("movements")
      .find(Json.obj())
      .cursor[JsObject]
      .collect[List]()
      .map(movements => Ok(Json.toJson(movements)))
  }

  def create = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
    request.body.validate(movementRequestFormatter) match {
      case errors: JsError =>
        Future(BadRequest(errors.toString).as("application/json"))

      case result: JsResult[MovementRequest] => {
        val futureMayBeFrom = db
          .collection[JSONCollection]("accounts")
          .find(Json.obj("_id" -> Json.obj("$oid" -> result.get.from._id.get.stringify)))
          .one[Account]

        val futureMayBeTo = db
          .collection[JSONCollection]("accounts")
          .find(Json.obj("_id" -> Json.obj("$oid" -> result.get.to._id.get.stringify)))
          .one[Account]

        for {
          mayBeFrom <- futureMayBeFrom
          mayBeTo <- futureMayBeTo
          result <- (for {
            fromAccount <- mayBeFrom
            toAccount <- mayBeTo
          } yield {
            val movement = Movement(result.get.description, fromAccount, toAccount, result.get.value)
            db
              .collection[JSONCollection]("movements")
              .save(movement)
              .map(errors => if (errors.inError) {
              InternalServerError
            } else {
              Ok(controllers.routes.MovementsController.index.absoluteURL())
            })
          }).getOrElse(Future(InternalServerError))
        } yield result

      }
    }

  }
}
