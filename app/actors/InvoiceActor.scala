package actors

import akka.actor.Actor
import domain.{Invoice, InvoiceSerializer}
import play.api.{Play, Logger}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import repository.Repositories
import util.pdf.GoogleDriveInteraction

case class InvoiceActor() extends Actor
with Repositories
with InvoiceSerializer
with GoogleDriveInteraction {

  import scala.concurrent.ExecutionContext.Implicits.global

  def receive = {

    case (invoice: Invoice, accessToken: String) => {
      invoiceRepository.save(invoice).map {
        case false => Logger.info(s"Saved invoice $invoice")
        case true => Logger.error(s"Unable to save invoice $invoice")
      }

      val shouldPush = Play.maybeApplication.flatMap {
        _.configuration.getBoolean("application.drive.shouldPush")
      }.getOrElse(false)
      if (shouldPush)
        uploadInvoiceToDrive(accessToken, invoice.invoice, invoice.pdfDocument.data)
    }

  }

}
