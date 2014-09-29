package controllers.api

import securesocial.core.RuntimeEnvironment
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import domain._
import util.pdf.GoogleDriveInteraction
import play.api.libs.json.{JsResult, JsError}
import play.api.libs.json
import domain.Invoice
import scala.Some
import securesocial.core.BasicProfile
import domain.ActivityRequest

class ActivityApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with ActivitySerializer
  with GoogleDriveInteraction
  with securesocial.core.SecureSocial[BasicProfile] {

  def createAndPushCRA = SecuredAction { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate(activityReqFormat) match {

        case errors: JsError =>
          Ok(errors.toString).as("application/json")

        case result: JsResult[ActivityRequest] =>
          Ok(activityToPdfBytes(result.get)).as("application/pdf")

      }

      case None => request.body.asFormUrlEncoded match {
        case Some(body) =>
          val activityRequest = activityFromForm(body)

          val shouldUpload = body.get("shouldUpload").map(_.head).exists(_.toBoolean)

          val generatedPdfDocument = activityToPdfBytes(activityRequest)

          //if (shouldUpload)
          //  activityActor ! ActivityRequest(activityRequest, Attachment("application/pdf", stub = false, generatedPdfDocument))

          Ok(generatedPdfDocument).as("application/pdf")

        case None => Ok("no go")
      }
    }

  }
  
  

}
