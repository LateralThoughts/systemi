package domain

case class ClientDefinition(name: String, address: String)

case class InvoiceLine(days: Double, dailyRate: Double, taxRate: Double = 19.6)

case class InvoiceRequest(client: ClientDefinition, invoice: List[InvoiceLine])

import play.api.libs.json._

trait InvoiceSerializer {
	implicit val invoiceClientReads = Json.reads[ClientDefinition]
   	implicit val invoiceLineReads = Json.reads[InvoiceLine]
   	implicit val invoiceReads = Json.reads[InvoiceRequest]
}