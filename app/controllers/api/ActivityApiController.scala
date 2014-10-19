package controllers.api

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain.{ActivityRequest, _}
import org.bouncycastle.util.encoders.Base64
import play.api.libs.json.{JsError, JsObject, JsResult, Json}
import play.libs.Akka
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import util.pdf.GoogleDriveInteraction

import scala.concurrent.Future

class ActivityApiController(override implicit val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator]
  with MongoController
  with ActivitySerializer
  with GoogleDriveInteraction
   {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  private val akkaSystem = Akka.system
  private lazy val activityActor = akkaSystem.actorSelection(akkaSystem / "activity")

  def createAndPushCRA = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate(activityReqFormat) match {

      case errors: JsError =>
        val errorMessage = "La requête ne peut être traitée à cause de données non valides, merci de corriger le formulaire"
        Future(Ok(controllers.routes.ActivityController.index().absoluteURL()).flashing(("error",errorMessage)))

      case result: JsResult[ActivityRequest] =>
        val generatedPdfDocument = activityToPdfBytes(result.get)
        val activityId = BSONObjectID.generate

        activityActor ! Activity(activityId, result.get, Attachment("application/pdf", stub = false, generatedPdfDocument), None)

        Future(Ok(routes.ActivityApiController.getPdfByCRA(activityId.stringify).absoluteURL()))
    }
  }


  def getPdfByCRA(oid: String) = SecuredAction.async {
    db
      .collection[JSONCollection]("activities")
      .find(Json.obj("_id" -> Json.obj("$oid" -> oid)), Json.obj("pdfDocument" -> 1))
      .one[JsObject]
      .map {
      case Some(pdfObj) =>
        val doc = (pdfObj \ "pdfDocument" \ "data" \ "data").as[String]
        Ok(Base64.decode(doc)).as("application/pdf")

      case None => BadRequest
    }

  }

  def findAll = SecuredAction.async {
    db
      .collection[JSONCollection]("activities")
      .find(Json.obj(), Json.obj("activity" -> 1, "id" -> 1, "invoiceId" -> 1))
      .cursor[JsObject]
      .collect[List]()
      .map(users => Ok(Json.toJson(users)))
  }

  def delete(oid: String) = SecuredAction.async {
    db
    .collection[JSONCollection]("activities")
    .remove(Json.obj("_id" -> Json.obj("$oid" -> oid)))
    .map(x => Ok(x.stringify))
  }

}
