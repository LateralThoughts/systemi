package domain

case class InvoiceLine(description: String, days: Double, dailyRate: Double, taxRate: Double = 19.6)

case class InvoiceRequest(title: String,
                          invoiceNumber : String,
                          paymentDelay: Int,
                          client: ClientDefinition,
                          invoice: List[InvoiceLine])

import _root_.util.pdf.PDF
import play.api.libs.json._
import views.html.invoice
import reactivemongo.bson.BSONObjectID

trait InvoiceSerializer {
  import play.modules.reactivemongo.json.BSONFormats._
  implicit val invoiceClientReads = Json.reads[ClientDefinition]
  implicit val invoiceLineReads = Json.reads[InvoiceLine]
  implicit val invoiceReads = Json.reads[InvoiceRequest]

  implicit val invoiceClientWrites = Json.writes[ClientDefinition]

  def invoiceFromForm(body : Map[String, Seq[String]]) = {
    val descriptions = body.get("invoiceDescription").get
    val days = body.get("invoiceDays").get
    val dailyRates = body.get("invoiceDailyRate").get
    val rates = body.get("invoiceTaxRate").get

    val lines = (for {
      (((description, day), dailyRate), rate) <- descriptions zip days zip dailyRates zip rates
    } yield InvoiceLine(description, day.toDouble, dailyRate.toDouble, rate.toDouble)).toList

    val invoiceRequest = InvoiceRequest(
      body.get("title").get.headOption.get,
      body.get("invoiceNumber").get.headOption.get,
      body.get("paymentDelay").get.headOption.get.toInt,
      ClientDefinition(body.get("clientId").get.headOption.map(new BSONObjectID(_)),
        body.get("clientName").get.headOption.get,
        body.get("clientAddress").get.headOption.get,
        body.get("clientPostalCode").get.headOption.get,
        body.get("clientCity").get.headOption.get),
      lines)
    invoiceRequest
  }

  def invoiceToPdfBytes(invoiceRequest : InvoiceRequest) :Array[Byte] = {
    val client = invoiceRequest.client
    val title = invoiceRequest.title
    val id = invoiceRequest.invoiceNumber
    val delay = invoiceRequest.paymentDelay
    val invoiceLines = invoiceRequest.invoice

    PDF.toBytes(invoice.render(title, id, delay, client, invoiceLines))
  }
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