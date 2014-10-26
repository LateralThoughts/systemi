package domain

import org.joda.time.DateTime
import play.api.libs.json.Json

case class Movement(description: Option[String] = None, from: Account, to: Account, value: Double, createdAt: DateTime = DateTime.now())

case class MovementRequest(description: Option[String] = None, from: Account, to: Account, value: Double)

trait MovementsSerializer extends AccountSerializer{
  implicit val movementsFormatter = Json.format[Movement]
  implicit val movementRequestFormatter = Json.format[MovementRequest]
}