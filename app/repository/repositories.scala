package repository

import play.api.libs.json.Json

/**
 * Trait used to set all repositories using macWire. All classes that want to interact with the database should
 * inherit this trait.
 */
trait Repositories {
  import com.softwaremill.macwire.MacwireMacros._

  lazy val invoiceRepository = wire[InvoiceRepository]
  lazy val allocationRepository = wire[AllocationRepository]
  lazy val activityRepository = wire[ActivityRepository]
  lazy val invoiceNumberRepository = wire[InvoiceNumberRepository]
}

trait Repository {

  // collection's name in mongo database
  val invoiceNumberCollectionName = "invoiceNumber"
  val invoicesCollectionName = "invoices"
  val allocationsCollectionName = "affectations"
  val activitiesCollectionName = "activities"

}

trait RequestBuilder {

  def idCriteria(id: String) = Json.obj("_id" -> Json.obj("$oid" -> id))

}
