package util.pdf

import domain.{Invoice, NextInvoiceNumbersParser, InvoiceRequest}
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.model._
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.services.drive.Drive

import java.util.Arrays

import play.api._

trait GoogleDriveInteraction extends NextInvoiceNumbersParser {
  private val log = Logger("GoogleDriveInteraction")

  private val IN_PROGRESS_FOLDER_ID = "0B7sqFgEnI9EXNEZVcC1KX0xtZlk"
  private val PAID_FOLDER_ID = "0B7sqFgEnI9EXa1hIajN3ckc4cUU"
  private val CANCELED_FOLDER_ID = "0B_MLNoY3PRlASmw1a2ROcFlEZFE"

  private val nextFolderId: String = "0B8t2tXgYCAKNRk1kcF83V2lHT2c"

  def uploadInvoiceToDrive(token: String, invoice: InvoiceRequest, content: Array[Byte]): Option[String] = {
    val service: Drive = initDriveService(token)
    val documentFile = createDocumentFile(invoice)
    val mediaContent = new ByteArrayContent("application/pdf", content)
    try {
      val result = service.files().insert(documentFile, mediaContent).execute()
      log.info(s"Invoice successfully uploaded to Google Drive : ${documentFile.getTitle}")
      Some(result.getId)
    } catch {
      case error: Throwable =>
        log.warn("An error occurred while trying to upload invoice to Google Drive : " + error) /* nothing to do it failed, it failed... */
        None
    }
  }

  def moveToPaid(token: String, invoice: Invoice) = {
    log.info(s"Invoice ${invoice._id.stringify} will be moved to Paid folder")
    moveInvoice(token, invoice, PAID_FOLDER_ID)
  }

  def moveToCanceled(token: String, invoice: Invoice) = {
    log.info(s"Invoice ${invoice._id.stringify} will be moved to Canceled folder")
    moveInvoice(token, invoice, CANCELED_FOLDER_ID)
  }

  def moveToInProgress(token: String, invoice: Invoice) = {
    log.info(s"Invoice ${invoice._id.stringify} will be moved to In Progress folder")
    moveInvoice(token, invoice, IN_PROGRESS_FOLDER_ID)
  }

  def moveInvoice(token: String, invoice: Invoice, newFolder: String) {
    val service: Drive = initDriveService(token)
    invoice.driveFileId.map(id => {
      try {
        // update file metadata
        val retrievedFile: File = service.files().get(id).execute()
        retrievedFile.setParents(Arrays.asList(new ParentReference().setId(newFolder)))

        // update file content
        val mediaContent = new ByteArrayContent("application/pdf", invoice.pdfDocument.data)

        // execute update request
        service.files().update(id, retrievedFile, mediaContent).execute()
      } catch {
        case error: Throwable =>
          log.warn("An error occurred while trying to upload invoice to Google Drive : " + error) /* nothing to do it failed, it failed... */
      }
    })
  }

  def getNextInvoiceNameAndIncrement(token: String) = {
    val service: Drive = initDriveService(token)

    val nextFolder = service.files().get(nextFolderId).execute
    val title: String = nextFolder.getTitle

    val (current, next) = extractInvoiceNumber(title)

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

  private def createGoogleCredentials(token: String) = {
    val credentials = new GoogleCredential()
    credentials.setAccessToken(token)
    credentials
  }

  private def createDocumentFile(invoice: InvoiceRequest) = {
    val document = new File()
    document.setTitle(invoice.driveName)
    document.setMimeType("application/pdf")
    document.setParents(Arrays.asList(new ParentReference().setId(IN_PROGRESS_FOLDER_ID)))
    document
  }
}
