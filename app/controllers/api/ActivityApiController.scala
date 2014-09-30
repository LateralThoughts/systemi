package controllers.api

import securesocial.core.RuntimeEnvironment
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import domain._
import util.pdf.GoogleDriveInteraction
import play.api.libs.json.{JsResult, JsError}
import scala.Some
import securesocial.core.BasicProfile
import domain.ActivityRequest
import org.joda.time.DateTime
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
  
  

}
