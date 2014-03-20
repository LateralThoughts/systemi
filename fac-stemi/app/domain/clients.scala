package domain

import reactivemongo.bson.BSONObjectID

case class ClientDefinition(_id: Option[BSONObjectID],
                            name: String,
                            address: String,
                            postalCode : String = "",
                            city: String = "")