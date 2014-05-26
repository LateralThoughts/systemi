package domain

import reactivemongo.bson.BSONObjectID

case class Client(_id: Option[BSONObjectID],
                  name: String,
                  address: String,
                  postalCode : String = "",
                  city: String = "",
                  country: String = "",
                  extraInfo: Option[String] = None)