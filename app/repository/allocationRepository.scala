package repository

import domain.{IncomeAffectation, AffectationSerializer}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

import scala.concurrent.Future

/**
 * Manage all database interaction related to allocations
 */
class AllocationRepository extends Repository with AffectationSerializer {

  import com.softwaremill.macwire.MacwireMacros._

  private lazy val allocationRequestBuilder = wire[AllocationRequestBuilder]

  val allocationsCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](allocationsCollectionName)

  def save(allocation: IncomeAffectation):Future[Boolean] = {
    allocationsCollection
      .save(allocation)
      .map(errors => errors.inError)
  }

  def findByInvoice(invoiceId: String): Future[List[IncomeAffectation]] = {
    allocationsCollection
      .find(allocationRequestBuilder.invoiceCriteria(invoiceId))
      .cursor[IncomeAffectation]
      .collect[List]()
  }

  def removeByInvoice(invoiceId: String): Future[Boolean] = {
    allocationsCollection.remove(allocationRequestBuilder.invoiceCriteria(invoiceId)).map(errors => errors.inError)
  }
}

/**
 * Construct all request (JsObject) used in AllocationRepository
 */
class AllocationRequestBuilder extends RequestBuilder with AffectationSerializer {

  def invoiceCriteria(invoiceId: String) = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))

}

