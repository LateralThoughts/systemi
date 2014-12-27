package repository

import play.Logger
import play.api.libs.json.{Json, JsObject}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

import scala.concurrent.Future

/**
 * Manage all database interaction related to allocations
 */
class AllocationRepository extends Repository {

  val allocationsCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](allocationsCollectionName)

  def findByInvoice(invoiceId: String) = {
    val criteria = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))

    allocationsCollection
      .find(criteria)
      .cursor[JsObject]
      .collect[List]()
  }

  def removeByInvoice(invoiceId: String): Future[LastError] = {
    Logger.info(s"Remove affectation associated to invoice $invoiceId")
    val invoiceSelector = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))

    allocationsCollection.remove(invoiceSelector)
  }
}
