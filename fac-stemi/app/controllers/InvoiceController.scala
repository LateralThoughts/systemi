package controllers

import util.pdf._
import play.api.mvc._
import play.api.libs.json._
import oauth._
import play.api.Logger
import domain._

object InvoiceController extends Controller
                    with InvoiceSerializer
                    with InvoiceLinesAnalyzer
                    with GoogleDriveInteraction {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index = Action {
    implicit request =>
      Ok(views.html.invoice_form(GoogleOAuth.getGoogleAuthUrl))
  }

  def cra = Action {
    implicit request =>
      Ok(views.html.cra(GoogleOAuth.getGoogleAuthUrl))
  }

  def auth = Action {
    implicit request =>
      val authcode = GoogleOAuth.getAuthCode(request)

      val token = authcode match {
        case Some(a) => Some(GoogleOAuth.getAccessToken(a))
        case _ => None
      }

      if (token.isDefined) {
        Redirect(routes.InvoiceController.index)
          .withSession(
            "token" -> token.get, "secret" -> authcode.get
          )
      } else {
        Redirect(routes.InvoiceController.index)
      }
  }

  def createAndPushInvoice = Action { implicit request => {
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

          val shouldUpload = body.get("shouldUpload").map(_.head).map(_.toBoolean).getOrElse(false)

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