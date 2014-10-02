package controllers.api

import securesocial.core.RuntimeEnvironment
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import domain._
import util.pdf.GoogleDriveInteraction
import play.api.libs.json.{JsObject, Json, JsResult, JsError}
import scala.Some
import securesocial.core.BasicProfile
import domain.ActivityRequest
import play.modules.reactivemongo.json.collection.JSONCollection
import org.bouncycastle.util.encoders.Base64
import play.api.mvc.Action
import scala.concurrent.Future
import reactivemongo.bson.BSONObjectID
import play.libs.Akka

class ActivityApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with ActivitySerializer
  with GoogleDriveInteraction
  with securesocial.core.SecureSocial[BasicProfile] {

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

        activityActor ! Activity(activityId, result.get, Attachment("application/pdf", stub = false, generatedPdfDocument))

        Future(Ok(routes.ActivityApiController.getPdfByCRA(activityId.stringify).absoluteURL()))
    }
  }


  def getPdfByCRA(oid: String) = Action.async {
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

}
