package actors

import akka.actor.Actor
import domain.{Invoice, InvoiceSerializer}
import play.api.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import util.pdf.GoogleDriveInteraction

case class InvoiceActor() extends Actor with InvoiceSerializer with GoogleDriveInteraction {
  import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

  def receive = {

    case (invoice: Invoice, accessToken: String) => {
      saveInvoice(invoice)
      //uploadInvoiceToDrive(accessToken, invoice.invoice, invoice.pdfDocument.data)
    }

  }

  private def saveInvoice(invoice: Invoice) = {
    val db = ReactiveMongoPlugin.db
    db.
      collection[JSONCollection]("invoices")
      .save(Json.toJson(invoice))
    Logger.info(s"Saved invoice $invoice")
  }

}
