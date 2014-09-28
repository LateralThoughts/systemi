package controllers.api

import domain.{InvoiceNumber, InvoiceRequest, InvoiceSerializer}
import play.api.libs.json.{Json, JsResult, JsError}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}
import util.pdf.GoogleDriveInteraction

/**
 * Created by ogirardot on 29/09/2014.
 */
class InvoiceApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with InvoiceSerializer
  with GoogleDriveInteraction
  with securesocial.core.SecureSocial[BasicProfile] {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def createAndPushInvoice = SecuredAction { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate(invoiceReads) match {

        case errors:JsError =>
          Ok(errors.toString).as("application/json")

        case result: JsResult[InvoiceRequest] =>
          Ok(invoiceToPdfBytes(result.get)).as("application/pdf")
      }
      case None => request.body.asFormUrlEncoded match {
        case Some(body) =>
          val invoiceRequest = invoiceFromForm(body)

          val shouldUpload = body.get("shouldUpload").map(_.head).exists(_.toBoolean)

          val generatedPdfDocument = invoiceToPdfBytes(invoiceRequest)

          if (shouldUpload)
            pushToGoogleDrive(invoiceRequest, generatedPdfDocument)

          Ok(generatedPdfDocument).as("application/pdf")

        case None => Ok("no go")
      }
    }
  }

  def getLastInvoiceNumber = Action.async {
    db.collection[JSONCollection]("invoiceNumber")
      .find(Json.obj())
      .one[InvoiceNumber]
      .map(mayBeObj => Ok(Json.toJson(mayBeObj.get)))
  }

  def reset(value: Int) = Action {
    db.collection[JSONCollection]("invoiceNumber")
      .update(Json.obj(), Json.toJson(InvoiceNumber(value)))
    Ok
  }

}
