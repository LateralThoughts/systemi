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
import play.libs.Akka
import play.modules.reactivemongo.json.collection.JSONCollection
import org.bouncycastle.util.encoders.Base64
import play.api.mvc.Action

class ActivityApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with ActivitySerializer
  with GoogleDriveInteraction
  with securesocial.core.SecureSocial[BasicProfile] {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  private val akkaSystem = Akka.system
  private lazy val activityActor = akkaSystem.actorSelection(akkaSystem / "activity")

  def createAndPushCRA = SecuredAction { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate(activityReqFormat) match {

        case errors: JsError =>
          Ok(errors.toString).as("application/json")

        case result: JsResult[ActivityRequest] =>
          val generatedPdfDocument = activityToPdfBytes(result.get)

          activityActor ! Activity(result.get, Attachment("application/pdf", stub = false, generatedPdfDocument))

          Ok(generatedPdfDocument).as("application/pdf")

      }
      case None => BadRequest

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
