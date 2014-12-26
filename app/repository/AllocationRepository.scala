package repository

import play.api.libs.json.{Json, JsObject}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

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

}
