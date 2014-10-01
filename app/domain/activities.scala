package domain

import org.joda.time.LocalDate
import play.api.libs.json.{Reads, Json}
import org.joda.time.format.ISODateTimeFormat
import util.pdf.PDF


case class ActivityDay(day: LocalDate, halfUp : Boolean, halfDown: Boolean)

case class ActivityRequest(tjm : Double,
                    numberOfDays : Double,
                    client: Client,
                    contractor: String,
                    days : List[ActivityDay] = List()) {
  
  def toInvoice(invoiceNumber: String) = {
    InvoiceRequest(s"${invoiceNumber} - facture", invoiceNumber, 30, client,
      List(
        InvoiceLine("Prestation de développement", numberOfDays, tjm)
      )
    )
  }
}

case class Activity(activity: ActivityRequest, pdfDocument: Attachment)

trait ActivitySerializer extends InvoiceSerializer {

  implicit val readsJodaLocalDateTime = Reads[LocalDate](js =>
    js.validate[String].map[LocalDate](dtString =>
      LocalDate.parse(dtString, ISODateTimeFormat.dateTime())
    )
  )

  implicit val activityDayReads = Json.reads[ActivityDay]
  implicit val activityReqFormat = Json.reads[ActivityRequest]

  implicit val activityDayWrites = Json.writes[ActivityDay]
  implicit val activityWrites = Json.writes[ActivityRequest]

  implicit val activityFormat = Json.format[Activity]

  def activityToPdfBytes(activityRequest : ActivityRequest) :Array[Byte] = {
    val client = activityRequest.client
    val days = activityRequest.days
    val numberOfDays = activityRequest.numberOfDays
    val tjm = activityRequest.tjm
    val contractor = activityRequest.contractor
    PDF.toBytes(views.html.cra.template(client, days, numberOfDays, tjm, contractor))
   }
}

trait NextInvoiceNumbersParser {
  
  val extractor = "NEXT_VT([0-9]+)".r
  
  def extractInvoiceNumber(value: String) =  {
    val extractor(number) = value
    val numericValue: Int = number.toInt
    (numericValue, numericValue+1)
  }

}