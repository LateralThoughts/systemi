package domain

import java.awt.Color

import _root_.util.pdf.PDF
import org.bouncycastle.util.encoders.Base64
import org.joda.time.DateTime
import play.Logger
import play.api.libs.json._

case class InvoiceNumber(value: Int) {
  def increment = this.copy(value + 1)
}

case class InvoiceLine(description: String, days: Double, dailyRate: Double, taxRate: Double = 20)

case class InvoiceRequest(title: String,
                          invoiceNumber : String,
                          paymentDelay: Int,
                          withTaxes: Boolean,
                          client: ClientRequest,
                          invoice: List[InvoiceLine])


case class Attachment(contentType: String,
                      stub: Boolean,
                      data: Array[Byte])


case class Status(name: String, createdAt: DateTime, email: String)


object InvoiceLinesAnalyzer extends InvoiceLinesAnalyzer

trait InvoiceLinesAnalyzer {
  def computeTotalHT(items : List[InvoiceLine]) = roundUpToSecondDecimal(items.foldLeft(0.0)(
    (cur: Double, item: InvoiceLine) => cur + (item.days * item.dailyRate)
  )
  )

  def computeTotal(items : List[InvoiceLine]) = roundUpToSecondDecimal(items.foldLeft(0.0)(
    (cur: Double, item: InvoiceLine) => cur + ((item.days * item.dailyRate) * ( 1 + item.taxRate / 100))
  )
  )

  def computeTva(items : List[InvoiceLine]) =
    items
      .groupBy( _.taxRate)
      .map{ case (label: Double, invoiceLines: List[InvoiceLine]) => (s"$label%", computeTvaByTaxRate(invoiceLines)) }


  private def computeTvaByTaxRate(invoiceLines : List[InvoiceLine]) = roundUpToSecondDecimal(
    invoiceLines.foldLeft(0.0)(
      (cur: Double, item: InvoiceLine) => cur + ((item.days * item.dailyRate) * item.taxRate / 100 ))
  )
  private def roundUpToSecondDecimal(value : Double) = BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
}

trait AttachmentSerializer {
  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val rds: Reads[Array[Byte]] = (__ \ "data").read[String].map{ arr: String => Base64.decode(arr) }
  implicit val wrs: Writes[Array[Byte]] = (__ \ "data").write[String].contramap{ (a: Array[Byte]) => new String(Base64.encode(a)) }
  implicit val fmt: Format[Array[Byte]] = Format(rds, wrs)
  implicit val attachmentFormatter = Json.format[Attachment]
}

trait InvoiceSerializer extends AttachmentSerializer with ClientSerializer {
  import play.modules.reactivemongo.json.BSONFormats._

  implicit val invoiceStatus = Json.format[Status]
  implicit val invoiceLineFormat = Json.format[InvoiceLine]

  implicit val invoiceReqFormat = Json.format[InvoiceRequest]
  implicit val invoiceNumberFormat = Json.format[InvoiceNumber]
  implicit val invoiceFormat = Json.format[Invoice]

  def invoiceFromForm(body : Map[String, Seq[String]]) = {
    val descriptions = body.get("invoiceDescription").get
    val days = body.get("invoiceDays").get
    val dailyRates = body.get("invoiceDailyRate").get
    val rates = body.get("invoiceTaxRate").get

    val lines = (for {
      (((description, day), dailyRate), rate) <- descriptions zip days zip dailyRates zip rates
    } yield InvoiceLine(description, day.toDouble, dailyRate.toDouble, rate.toDouble)).toList

    val withTaxes = body.get("paymentTaxesIncluded").map(_.head) match {
      case Some(element) => element.equalsIgnoreCase("true")
      case None => true
    }

    val invoiceRequest = InvoiceRequest(
      body.get("title").get.headOption.get,
      body.get("invoiceNumber").get.headOption.get,
      body.get("paymentDelay").get.headOption.get.toInt,
      withTaxes,
      ClientRequest(
        body.get("clientName").get.headOption.get,
        body.get("clientAddress").get.headOption.get,
        body.get("clientPostalCode").get.headOption.get,
        body.get("clientCity").get.headOption.get,
        body.get("clientCountry").get.headOption.get,
        body.getOrElse("clientExtraInfo", Seq("")).headOption.get match {
          case ""|null => None
          case x => Some(x)
        }),
      lines)
    invoiceRequest
  }

  def invoiceRequestToPdfBytes(invoiceRequest : InvoiceRequest) :Array[Byte] = {
    val client = invoiceRequest.client
    val title = invoiceRequest.title
    val id = invoiceRequest.invoiceNumber
    val delay = invoiceRequest.paymentDelay
    val invoiceLines = invoiceRequest.invoice
    val date = DateTime.now()

    PDF.toBytes(views.html.invoice.template(title, id, delay, client, invoiceLines, date, invoiceRequest.withTaxes))
  }

  def addCanceledWatermark(pdfBytes: Array[Byte]): Array[Byte] = {

    val watermark = "ANNULÉE"
    val watermarkColor = Color.RED

    PDF.addWatermarkToPdf(pdfBytes: Array[Byte], watermark, watermarkColor)
  }
}
