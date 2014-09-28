package controllers

import _root_.util.pdf.GoogleDriveInteraction
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.mvc._
import play.api.libs.json._
import domain._
import securesocial.core.{RuntimeEnvironment, BasicProfile}
import scala.concurrent.ExecutionContext


class CraController(override implicit val env: RuntimeEnvironment[BasicProfile]) extends Controller
                     with ActivitySerializer
                     with MongoController
                     with GoogleDriveInteraction
                     with NextInvoiceNumbersParser
                     with securesocial.core.SecureSocial[BasicProfile] {
  import ExecutionContext.Implicits.global

  def collection = db.collection[JSONCollection]("cras")

  def add = SecuredAction(parse.json) { implicit request =>
    val json = request.body
    json.validate(activityReads) match {
      case errors:JsError => Ok(errors.toString).as("application/json")
      case result: JsResult[Activity] => {
        val activity = result.get
        collection.insert(activity)
        // and generate corresponding invoice :
        request.session.get("token").map { token =>
          val nextInvoiceName: String = getNextInvoiceNameAndIncrement(token)
          val invoiceRequest = activity.toInvoice(nextInvoiceName)
          val generatedPdfDocument = invoiceToPdfBytes(invoiceRequest)
          pushToGoogleDrive(invoiceRequest, generatedPdfDocument)
        }
        Ok
      }
    }
  }
}
