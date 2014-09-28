package domain

import org.joda.time.LocalDate
import play.api.libs.json.{Reads, Json}
import reactivemongo.bson.BSONObjectID
import org.joda.time.format.ISODateTimeFormat


case class ActivityDay(day: LocalDate, halfUp : Boolean, halfDown: Boolean)

case class Activity(_id: Option[BSONObjectID],
                    tjm : Double,
                    numberOfDays : Double,
                    client: Client,
                    days : List[ActivityDay] = List()) {
  
  def toInvoice(invoiceNumber: String) = {
    InvoiceRequest(s"${invoiceNumber} - facture", invoiceNumber, 30, client,
      List(
        InvoiceLine("Prestation de développement", numberOfDays, tjm)
      )
    )
  }
}

trait ActivitySerializer extends InvoiceSerializer {
  import play.modules.reactivemongo.json.BSONFormats._

  implicit val readsJodaLocalDateTime = Reads[LocalDate](js =>
    js.validate[String].map[LocalDate](dtString =>
      LocalDate.parse(dtString, ISODateTimeFormat.dateTime())
    )
  )

  implicit val activityDayReads = Json.reads[ActivityDay]
  implicit val activityReads = Json.reads[Activity]

  implicit val activityDayWrites = Json.writes[ActivityDay]
  implicit val activityWrites = Json.writes[Activity]
}

trait NextInvoiceNumbersParser {
  
  val extractor = "NEXT_VT([0-9]+)".r
  
  def extractInvoiceNumber(value: String) =  {
    val extractor(number) = value
    val numericValue: Int = number.toInt
    (numericValue, numericValue+1)
  }

}