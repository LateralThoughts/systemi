package controllers.api

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain._
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future

class MovementsApiController(override implicit val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator]
  with MongoController
  with AccountSerializer
  with MovementsSerializer
   {

  import scala.concurrent.ExecutionContext.Implicits.global

  def findAll = SecuredAction.async {
    db
      .collection[JSONCollection]("movements")
      .find(Json.obj())
      .cursor[JsObject]
      .collect[List]()
      .map(movements => Ok(Json.toJson(movements)))
  }

  def create = SecuredAction.async(parse.urlFormEncoded) { implicit request =>
    val from = request.body.get("from")
    val to = request.body.get("to")
    val value = request.body.get("value")

    if (from.isDefined && to.isDefined && value.isDefined) {
      val futureMayBeFrom = db
        .collection[JSONCollection]("accounts")
        .find(Json.obj("_id" -> Json.obj("$oid" -> from.get.head)))
        .one[Account]

      val futureMayBeTo = db
        .collection[JSONCollection]("accounts")
        .find(Json.obj("_id" -> Json.obj("$oid" -> to.get.head)))
        .one[Account]

      for {
        mayBeFrom <- futureMayBeFrom
        mayBeTo <- futureMayBeTo
        result <- (for {
          fromAccount <- mayBeFrom
          toAccount <- mayBeTo
        } yield {
          val movement = Movement(fromAccount, toAccount, value.get.head.toDouble)
          db
            .collection[JSONCollection]("movements")
            .save(movement)
            .map(errors => if (errors.inError) {
            InternalServerError
          } else {
            Redirect(controllers.routes.MovementsController.index)
          })
        }).getOrElse(Future(InternalServerError))
      } yield result
    } else
      Future(BadRequest)
  }
}
