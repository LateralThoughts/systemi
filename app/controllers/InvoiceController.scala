package controllers

import domain._
import play.api.mvc._
import securesocial.core.{BasicProfile, RuntimeEnvironment}
import util.pdf._
import play.api.libs.json._

class InvoiceController(override implicit val env: RuntimeEnvironment[BasicProfile]) extends Controller
                    with InvoiceSerializer
                    with GoogleDriveInteraction
                    with securesocial.core.SecureSocial[BasicProfile] {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index = SecuredAction {
    implicit request =>
      Ok(views.html.invoice_form(request.user))
  }

  def cra = SecuredAction {
    implicit request =>
      Ok(views.html.cra(request.user))
  }

  def createAndPushInvoice = SecuredAction { implicit request => {
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
  }
}