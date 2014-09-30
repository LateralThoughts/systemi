package domain

import org.joda.time.LocalDate
import play.api.libs.json.{Reads, Json}
import org.joda.time.format.ISODateTimeFormat


case class ActivityDay(day: LocalDate, halfUp : Boolean, halfDown: Boolean)

case class ActivityRequest(tjm : Double,
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

  implicit val readsJodaLocalDateTime = Reads[LocalDate](js =>
    js.validate[String].map[LocalDate](dtString =>
      LocalDate.parse(dtString, ISODateTimeFormat.dateTime())
    )
  )

  implicit val activityDayReads = Json.reads[ActivityDay]
  implicit val activityReqFormat = Json.reads[ActivityRequest]

  implicit val activityDayWrites = Json.writes[ActivityDay]
  implicit val activityWrites = Json.writes[ActivityRequest]

  def activityToPdfBytes(activityRequest : ActivityRequest) :Array[Byte] = {
     new Array[Byte](0)
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