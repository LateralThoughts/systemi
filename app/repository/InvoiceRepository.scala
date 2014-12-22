package repository

import domain.{InvoiceSerializer, Invoice}
import play.api.libs.json.{Json, JsObject}
import play.modules.reactivemongo.{ReactiveMongoPlugin, MongoController}
import play.modules.reactivemongo.json.collection.JSONCollection
import sun.awt.ModalExclude

import scala.concurrent.Future
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Manage all database interactions related to invoices
 */
class InvoiceRepository() extends Repository with InvoiceSerializer {

  val selection: JsObject = Json.obj("invoice" -> 1, "statuses" -> 1)

  val invoicesCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](invoiceCollection)

  def find(criteria : JsObject = Json.obj()): Future[List[JsObject]] = {
    invoicesCollection
      .find(criteria, selection)
      .cursor[JsObject]
      .collect[List]()
  }

  def buildStatusCriteria(status: String, exclude: Boolean):JsObject = {

    val criteriaField =
      if (List("paid", "unpaid") contains status)
        "paymentStatus"
      else if (List("unaffected", "affected") contains status)
        "affectationStatus"
      else
        "lastStatus.name"

    if (exclude) {
      Json.obj(criteriaField -> Json.obj("$ne" -> status), "canceled" -> Json.obj("$ne" -> true))
    } else {
      Json.obj(criteriaField -> status, "canceled" -> Json.obj("$ne" -> true))
    }
  }

  def find(invoiceId: String) = {

    val criteria = Json.obj("_id" -> Json.obj("$oid" -> invoiceId))

    invoicesCollection
      .find(criteria)
      .one[Invoice]

  }

  def update(invoiceId: String, update: JsObject) = {
    val criteria = Json.obj("_id" -> Json.obj("$oid" -> invoiceId))

    invoicesCollection.update(criteria, update)

  }

}
