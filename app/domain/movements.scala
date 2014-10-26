package domain

import org.joda.time.DateTime
import play.api.libs.json.Json

case class Movement(description: String, from: Account, to: Account, value: Double, createdAt: DateTime = DateTime.now())

trait MovementsSerializer extends AccountSerializer{
  implicit val movementsFormatter = Json.format[Movement]
}