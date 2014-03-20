package domain

import org.joda.time.LocalDate
import play.api.libs.json.Json


case class ActivityDay(day: LocalDate, halfUp : Boolean, halfDown: Boolean)

case class Activity(id: Option[Long],
                    numberOfDays : Long,
                    days : List[ActivityDay],
                    client: Client)

trait ActivitySerializer extends InvoiceSerializer {
  implicit val activityDayReads = Json.reads[ActivityDay]
  implicit val activityReads = Json.reads[Activity]

  implicit val activityDayWrites = Json.writes[ActivityDay]
  implicit val activityWrites = Json.writes[Activity]
}

object Activities extends ActivitySerializer {


}