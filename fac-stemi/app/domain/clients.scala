package domain

import reactivemongo.bson.BSONObjectID

case class ClientDefinition(_id : String,
                            name: String,
                            address: String,
                            postalCode : String = "",
                            city: String = "")  {

  def this(id: Option[BSONObjectID], c: NewClientDefinition) =
     this(id.getOrElse(BSONObjectID.generate).toString(), c.name, c.address, c.postalCode, c.city)
}

case class NewClientDefinition(name: String, address: String, postalCode : String = "", city: String = "")
