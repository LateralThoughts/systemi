package domain

import org.joda.time.LocalDate
import play.api.libs.json.{Reads, Json}
import reactivemongo.bson.BSONObjectID
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

  def activityFromForm(body : Map[String, Seq[String]]) = {
    val days = body.get("days").get

    val lines = Nil

    val activityRequest = ActivityRequest(
      body.get("tjm").get.headOption.get.toDouble, // TODO change name to activityTjm
      body.get("numberOfDays").get.headOption.get.toDouble, // TODO change name to activityNumberOfDays
      Client(
        body.get("clientId").flatMap(_.headOption.map(BSONObjectID(_))),
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
    activityRequest
  }

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