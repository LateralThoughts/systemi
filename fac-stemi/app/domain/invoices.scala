package domain

case class ClientDefinition(name: String, address: String, postalCode : String = "", city: String = "")

case class InvoiceLine(description: String, days: Double, dailyRate: Double, taxRate: Double = 19.6)

case class InvoiceRequest(client: ClientDefinition, invoice: List[InvoiceLine])

import play.api.libs.json._

trait InvoiceSerializer {
	implicit val invoiceClientReads = Json.reads[ClientDefinition]
   	implicit val invoiceLineReads = Json.reads[InvoiceLine]
   	implicit val invoiceReads = Json.reads[InvoiceRequest]
}

trait InvoiceLinesAnalyzer {
	def computeTotalHT(items : List[InvoiceLine]) = roundUpToSecondDecimal(items.foldLeft(0.0)(
			(cur: Double, item: InvoiceLine) => cur + (item.days * item.dailyRate)
		)
	)

	def computeTotal(items : List[InvoiceLine]) = roundUpToSecondDecimal(items.foldLeft(0.0)(
			(cur: Double, item: InvoiceLine) => cur + ((item.days * item.dailyRate) * ( 1 + item.taxRate / 100))
		)
	)

	def computeTva(items : List[InvoiceLine]) = roundUpToSecondDecimal(items.foldLeft(0.0)(
			(cur: Double, item: InvoiceLine) => cur + ((item.days * item.dailyRate) * item.taxRate / 100 )
		)
	)

	private def roundUpToSecondDecimal(value : Double) = BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
}