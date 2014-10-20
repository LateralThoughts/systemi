package domain

import org.joda.time.LocalDate
import play.api.libs.json.{Reads, Json}
import org.joda.time.format.ISODateTimeFormat
import util.pdf.PDF
import reactivemongo.bson.BSONObjectID


case class ActivityDay(day: LocalDate, halfUp : Boolean, halfDown: Boolean)

case class ActivityRequest(numberOfDays : Double,
                    client: ClientRequest,
                    contractor: String,
                    title: String,
                    days : List[ActivityDay] = List()) {

}

case class Activity(_id: BSONObjectID, activity: ActivityRequest, pdfDocument: Attachment, invoiceId: Option[BSONObjectID])

trait ActivitySerializer extends InvoiceSerializer {
  import play.modules.reactivemongo.json.BSONFormats._

  implicit val readsJodaLocalDateTime = Reads[LocalDate](js =>
    js.validate[String].map[LocalDate](dtString =>
      LocalDate.parse(dtString, ISODateTimeFormat.dateTime())
    )
  )

  implicit val activityDayReads = Json.reads[ActivityDay]
  implicit val activityReqFormat = Json.reads[ActivityRequest]

  implicit val activityDayWrites = Json.writes[ActivityDay]
  implicit val activityReqWrites = Json.writes[ActivityRequest]

  implicit val activityFormat = Json.format[Activity]

  def activityToPdfBytes(activityRequest : ActivityRequest) :Array[Byte] = {
    val client = activityRequest.client
    val days = activityRequest.days
    val numberOfDays = activityRequest.numberOfDays
    val contractor = activityRequest.contractor
    val subtitle = activityRequest.title
    PDF.toBytes(views.html.activity.template(client, days, numberOfDays, contractor, subtitle))
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