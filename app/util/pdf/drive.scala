package util.pdf

import domain.{NextInvoiceNumbersParser, InvoiceRequest}
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

trait GoogleDriveInteraction extends NextInvoiceNumbersParser {
  private val log = Logger("GoogleDriveInteraction")

  private val DESTINATION_FOLDER_ID = "0B7sqFgEnI9EXNEZVcC1KX0xtZlk"

  private val nextFolderId: String = "0B8t2tXgYCAKNRk1kcF83V2lHT2c"

  def uploadInvoiceToDrive(token: String, invoice : InvoiceRequest, content : Array[Byte]) {
    val service: Drive = initDriveService(token)
    val documentFile = createDocumentFile(invoice)
    val mediaContent = new ByteArrayContent("application/pdf", content)
    try {
      service.files().insert(documentFile, mediaContent).execute()
      log.info(s"Invoice successfully uploaded to Google Drive : ${documentFile.getTitle}")
    } catch {
      case error :Throwable => log.warn("an error occurred while trying to upload invoice to Google Drive : " + error)/* nothing to do it failed, it failed... */
    }
  }

  def getNextInvoiceNameAndIncrement(token :String)  = {
    val service: Drive = initDriveService(token)

    val nextFolder = service.files().get(nextFolderId).execute
    val title: String = nextFolder.getTitle
    
    val (current,next) = extractInvoiceNumber(title)

    renameFolder(service, s"NEXT_VT$next")

    service.files()
    s"VT$current"
  }


  def renameFolder(service: Drive, newName: String) {
    val file: File = new File
    file.setTitle(newName)
    val patchRequest = service.files().patch(nextFolderId, file)
    patchRequest.setFields("title")
    patchRequest.execute()
  }


  def initDriveService(token: String): Drive = {
    val credentials = createGoogleCredentials(token)
    val service = new Drive.Builder(new NetHttpTransport, new JacksonFactory, credentials).build()
    service
  }

  private def createGoogleCredentials(token: String)  = {
    val credentials = new GoogleCredential()
    credentials.setAccessToken(token)
    credentials
  }

  private def createDocumentFile(invoice: InvoiceRequest) = {
    val document = new File()
    document.setTitle(s"${invoice.invoiceNumber}_LateralThoughts_${invoice.client.name}.pdf")
    document.setMimeType("application/pdf")
    document.setParents(Arrays.asList(new ParentReference().setId(DESTINATION_FOLDER_ID)));
    document
  }
}
