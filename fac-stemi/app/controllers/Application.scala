package controllers

import util.pdf._
import views.html._
import play.api._
import play.api.mvc._
import domain._
import play.api.libs.json._
import scala.concurrent.Future
import play.api.libs.oauth._  
import play.api.libs.ws._
import oauth._
import play.api.Logger

object Application extends Controller with InvoiceSerializer with InvoiceLinesAnalyzer {
  implicit val context = scala.concurrent.ExecutionContext.Implicits.global
  val log = Logger("invoice@LT")

  def index = Action {
    implicit request => 
      Ok(views.html.index(GoogleOAuth.getGoogleAuthUrl))
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

  private def upload(token: String, invoice : InvoiceRequest, content : Array[Byte]) {
    import java.util.Arrays
    import com.google.api.client.http.ByteArrayContent
    import com.google.api.services.drive.model._
    import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
    import com.google.api.client.http.javanet.NetHttpTransport
    import com.google.api.client.json.jackson.JacksonFactory
    import com.google.api.services.drive.Drive
    
    val credentials = new GoogleCredential()
    credentials.setAccessToken(token)  

    val service = new Drive.Builder(new NetHttpTransport, new JacksonFactory, credentials).build()
    val body = new File()
    body.setTitle(invoice.title)
    body.setMimeType("application/pdf")
    body.setParents(Arrays.asList(new ParentReference().setId("0B7sqFgEnI9EXNEZVcC1KX0xtZlk")));

    val mediaContent = new ByteArrayContent("application/pdf", content)
    try {
      service.files().insert(body, mediaContent).execute()
    } catch {
      case error :Throwable => log.warn("an error occured while trying to upload invoice to Google Drive : " + error)/* nothing to do it failed, it failed... */
    }
  }

  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }

  def showInvoice = Action { implicit request => {
      request.body.asJson match {
        case Some(json) => json.validate(invoiceReads) match {
          case errors:JsError => Ok(errors.toString).as("application/json")
          case result: JsResult[InvoiceRequest] => Ok(invoiceToPdfBytes(result.get)).as("application/pdf")
        }
        case None => request.body.asFormUrlEncoded match {
          case Some(body) => 
            val invoiceRequest = invoiceFromForm(body.map({ case(k,v) => (k, v.headOption.get)}))
            val generatedInvoice = invoiceToPdfBytes(invoiceRequest)
            sessionTokenPair match {
              case Some(tokens) => {
                upload(tokens.token, invoiceRequest, generatedInvoice)
              }
              case _ => log.warn("no access token found - the invoice won't be uploaded to Google Drive")
            }
            Ok(generatedInvoice).as("application/pdf")

          case None => Ok("no go")
        }
      }
    }
  }

  def invoiceHtml = Action {
    val invoiceRequest = InvoiceRequest("facture", "VT055", 
      ClientDefinition("VIDAL", "27 rue camille desmoulins", "94550", "chevilly"), 
        List(InvoiceLine("blabla", 25.0, 450.0, 19.6)))

    val client = invoiceRequest.client
    val title = invoiceRequest.title
    val id = invoiceRequest.invoiceNumber
    val invoiceLines = invoiceRequest.invoice

    Ok(views.html.invoice(title, id, client, invoiceLines))
  }

  private def invoiceFromForm(body : Map[String, String]) = {
    val invoiceRequest = InvoiceRequest(
      body.get("title").get,
      body.get("invoiceNumber").get,
      ClientDefinition(body.get("clientName").get, body.get("clientAddress").get, body.get("clientCity").get, body.get("clientPostalCode").get),
      List(InvoiceLine(body.get("invoiceDescription").get, 
        body.get("invoiceDays").get.toDouble, 
        body.get("invoiceDailyRate").get.toDouble,
        body.get("invoiceTaxRate").get.toDouble)
      )
    )
    invoiceRequest
  }

  private def invoiceToPdfBytes(invoiceRequest : InvoiceRequest) :Array[Byte] = {
    val client = invoiceRequest.client
    val title = invoiceRequest.title
    val id = invoiceRequest.invoiceNumber
    val invoiceLines = invoiceRequest.invoice

    PDF.toBytes(invoice.render(title, id, client, invoiceLines))
  } 
}