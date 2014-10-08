package domain

import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID

case class Client(_id: BSONObjectID,
                  name: String,
                  address: String,
                  postalCode: String = "",
                  city: String = "",
                  country: String = "",
                  extraInfo: Option[String] = None)

case class ClientRequest(name: String,
                         address: String,
                         postalCode: String = "",
                         city: String = "",
                         country: String = "",
                         extraInfo: Option[String] = None)

trait ClientSerializer {
  import play.modules.reactivemongo.json.BSONFormats._

  implicit val clientRequestFormat = Json.format[ClientRequest]
  implicit val clientFormat = Json.format[Client]
}
