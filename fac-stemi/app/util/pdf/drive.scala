package util.pdf

import domain.InvoiceRequest
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.model._
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.services.drive.Drive
import play.api.mvc.RequestHeader

import play.api.libs.oauth._
import java.util.Arrays

import play.api._

trait GoogleDriveInteraction {
  private val log = Logger("GoogleDriveInteraction")

  private val DESTINATION_FOLDER_ID = "0B7sqFgEnI9EXNEZVcC1KX0xtZlk"

  def upload(token: String, invoice : InvoiceRequest, content : Array[Byte]) {
    val credentials = createGoogleCredentials(token)

    val service = new Drive.Builder(new NetHttpTransport, new JacksonFactory, credentials).build()
    val documentFile = createDocumentFile(invoice)

    val mediaContent = new ByteArrayContent("application/pdf", content)
    try {
      service.files().insert(documentFile, mediaContent).execute()
    } catch {
      case error :Throwable => log.warn("an error occured while trying to upload invoice to Google Drive : " + error)/* nothing to do it failed, it failed... */
    }
  }


  def pushToGoogleDrive(invoiceRequest: InvoiceRequest, generatedInvoice: Array[Byte])(implicit request: RequestHeader) {
    sessionTokenPair match {
      case Some(tokens) => {
        upload(tokens.token, invoiceRequest, generatedInvoice)
      }
      case _ => log.warn("no access token found - the invoice won't be uploaded to Google Drive")
    }
  }

  private def createGoogleCredentials(token: String)  = {
    val credentials = new GoogleCredential()
    credentials.setAccessToken(token)
    credentials
  }

  private def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
    token <- request.session.get("token")
    secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }


  private def createDocumentFile(invoice: InvoiceRequest) = {
    val document = new File()
    document.setTitle(invoice.title)
    document.setMimeType("application/pdf")
    document.setParents(Arrays.asList(new ParentReference().setId(DESTINATION_FOLDER_ID)));
    document
  }
}
