package repository

import domain.{IncomeAllocation, AllocationSerializer}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

import scala.concurrent.Future

/**
 * Manage all database interaction related to allocations
 */
class AllocationRepository extends Repository with AllocationSerializer {

  import com.softwaremill.macwire.MacwireMacros._

  private lazy val allocationRequestBuilder = wire[AllocationRequestBuilder]

  val allocationsCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](allocationsCollectionName)

  def save(allocation: IncomeAllocation):Future[Boolean] = {
    allocationsCollection
      .save(allocation)
      .map(errors => errors.inError)
  }

  def findByInvoice(invoiceId: String): Future[List[IncomeAllocation]] = {
    allocationsCollection
      .find(allocationRequestBuilder.invoiceCriteria(invoiceId))
      .cursor[IncomeAllocation]
      .collect[List]()
  }

  def findByUser(userId: String): Future[List[IncomeAllocation]] = {
    allocationsCollection
      .find(allocationRequestBuilder.userCriteria(userId))
      .cursor[IncomeAllocation]
      .collect[List]()
  }

  def removeByInvoice(invoiceId: String): Future[Boolean] = {
    allocationsCollection.remove(allocationRequestBuilder.invoiceCriteria(invoiceId)).map(errors => errors.inError)
  }
}

/**
 * Construct all request (JsObject) used in AllocationRepository
 */
class AllocationRequestBuilder extends RequestBuilder with AllocationSerializer {

  def invoiceCriteria(invoiceId: String) = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))

  def userCriteria(userId: String) = Json.obj("account.stakeholder.user.userId" -> userId)

}

