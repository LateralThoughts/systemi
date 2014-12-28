package repository

import domain.{IncomeAffectation, AffectationSerializer}
import play.Logger
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

  val allocationsCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](allocationsCollectionName)

  def findByInvoice(invoiceId: String): Future[List[IncomeAffectation]] = {
    val criteria = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))

    allocationsCollection
      .find(criteria)
      .cursor[IncomeAffectation]
      .collect[List]()
  }

  def removeByInvoice(invoiceId: String): Future[Boolean] = {
    Logger.info(s"Remove affectation associated to invoice $invoiceId")
    val invoiceSelector = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))

    allocationsCollection.remove(invoiceSelector).map(errors => errors.inError)
  }
}
