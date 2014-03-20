package domain

import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag


case class ClientDefinition(id: Option[Long], name: String, address: String, postalCode : String = "", city: String = "")  {

  def this(id: Option[Long], c: NewClientDefinition) = this(id, c.name, c.address, c.postalCode, c.city)
}

class Clients(tag: Tag) extends Table[ClientDefinition](tag, "client") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name", O.NotNull)

  def address = column[String]("address", O.NotNull)

  def postalCode = column[String]("postalCode", O.NotNull)

  def city = column[String]("city", O.NotNull)

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id.?, name, address, postalCode, city) <> (ClientDefinition.tupled, ClientDefinition.unapply _)
}

object Clients {

  val clients = TableQuery[Clients]

  /**
   * Retrieve a computer from the id
   * @param id
   */
  def findById(id: Long)(implicit s: Session): Option[ClientDefinition] =
    clients.where(_.id === id).firstOption


  def list()(implicit s: Session) = clients.list()

  /**
   * Insert a new client
   * @param client
   */
  def insert(client: ClientDefinition)(implicit s: Session) {
    clients.insert(client)
  }

  /**
   * Update a computer
   * @param id
   * @param client
   */
  def update(id: Long, client: ClientDefinition)(implicit s: Session) {
    val clientToUpdate: ClientDefinition = client.copy(id = Some(id))
    clients.where(_.id === id).update(clientToUpdate)
  }

  /**
   * Delete a computer
   * @param id
   */
  def delete(id: Long)(implicit s: Session) {
    clients.where(_.id === id).delete
  }

}

case class NewClientDefinition(name: String, address: String, postalCode : String = "", city: String = "")
