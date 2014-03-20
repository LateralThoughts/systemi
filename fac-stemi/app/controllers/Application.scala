package controllers

import util.pdf._
import play.api.mvc._
import domain._
import play.api.libs.json._
import oauth._
import play.api.Logger
import reactivemongo.bson.BSONObjectID

object Application extends Controller
                    with InvoiceSerializer
                    with InvoiceLinesAnalyzer
                    with GoogleDriveInteraction {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  private val log = Logger("Application")


  def index = Action {
    implicit request =>
      Ok(views.html.index(GoogleOAuth.getGoogleAuthUrl))
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
        Redirect(routes.Application.index)
          .withSession(
            "token" -> token.get, "secret" -> authcode.get
          )
      } else {
        Redirect(routes.Application.index)
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

          val generatedPdfDocument = invoiceToPdfBytes(invoiceRequest)

          pushToGoogleDrive(invoiceRequest, generatedPdfDocument)

          Ok(generatedPdfDocument).as("application/pdf")

        case None => Ok("no go")
      }
    }
  }
  }

  def showInvoiceHtml = Action {
    val invoiceRequest = InvoiceRequest("facture", "VT055", 30,
      ClientDefinition(BSONObjectID.generate.toString(), "VIDAL", "27 rue camille desmoulins", "94550", "chevilly"),
      List(InvoiceLine("blabla", 25.0, 450.0, 19.6)))

    val client = invoiceRequest.client
    val title = invoiceRequest.title
    val id = invoiceRequest.invoiceNumber
    val delay = invoiceRequest.paymentDelay
    val invoiceLines = invoiceRequest.invoice

    Ok(views.html.invoice(title, id, delay, client, invoiceLines))
  }
}