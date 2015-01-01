package actors

import akka.actor.Actor
import domain.{Invoice, InvoiceSerializer}
import play.api.{Play, Logger}
import repository.Repositories
import util.pdf.GoogleDriveInteraction

case class InvoiceActor() extends Actor
with Repositories
with InvoiceSerializer
with GoogleDriveInteraction {

  import scala.concurrent.ExecutionContext.Implicits.global

  val shouldPush = Play.maybeApplication.flatMap {
    _.configuration.getBoolean("application.drive.shouldPush")
  }.getOrElse(false)

  def receive = {
    case ("created", invoice: Invoice, accessToken: String) =>
      saveInvoiceInDatabase(invoice)
      if (shouldPush) uploadInvoiceToDrive(invoice, accessToken)
    case ("paid", invoice: Invoice, accessToken: String) =>
      moveToPaid(accessToken, invoice)
    case ("inProgress", invoice: Invoice, accessToken: String) =>
      moveToInProgress(accessToken, invoice)
    case ("canceled", invoice: Invoice, accessToken: String) =>
      moveToCanceled(accessToken, invoice)
    case _ => Logger.error("This message is not implemented")
  }

  def saveInvoiceInDatabase(invoice: Invoice) {
    invoiceRepository.save(invoice).map {
      case false =>
        Logger.info(s"Saved invoice $invoice")
        invoiceNumberRepository.increment.map(hasErrors => if (hasErrors) Logger.error("Unable to increment invoice number"))
      case true => Logger.error(s"Unable to save invoice $invoice")
    }
  }

  def uploadInvoiceToDrive(invoice: Invoice, accessToken: String) {
    uploadInvoiceToDrive(accessToken, invoice.invoice, invoice.pdfDocument.data) match {
      case None => Logger.error(s"Unable to upload invoice ${invoice._id.stringify} to google drive")
      case Some(fileId) =>
        Logger.info(s"Invoice ${invoice._id.stringify} uploaded in drive as file $fileId")
        invoiceRepository
          .saveDriveFileId(invoice._id.stringify, fileId)
          .map(hasError => if (hasError) Logger.error(s"Unable to update invoice ${invoice._id.stringify} with google drive invoice id $fileId"))
    }
  }
}
