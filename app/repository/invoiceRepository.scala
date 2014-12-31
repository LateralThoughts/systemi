package repository

import domain._
import org.joda.time.DateTime
import play.Logger
import play.api.libs.json.{JsValue, Json, JsObject}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Manage all database interactions related to invoice Number
 */
class InvoiceNumberRepository extends Repository with InvoiceSerializer {

  private val invoiceNumberCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](invoiceNumberCollectionName)

  def getLastInvoiceNumber: Future[InvoiceNumber] = {
    invoiceNumberCollection
      .find(Json.obj())
      .one[InvoiceNumber]
      .map {
      case None =>
        save("VT", 1)
        InvoiceNumber("VT", 1)
      case Some(invoiceNumber) => invoiceNumber
    }
  }

  def reset(prefix: String, value: Int) = {
    invoiceNumberCollection
      .update(Json.obj(), Json.toJson(InvoiceNumber(prefix, value)))
      .map(errors => errors.inError)
  }

  def save(prefix: String, value: Int) = {
    invoiceNumberCollection
      .save(Json.toJson(InvoiceNumber(prefix, value)))
      .map(errors => errors.inError)
  }

  def increment = {
    invoiceNumberCollection
    .update(Json.obj(), Json.obj("$inc" -> Json.obj("value" -> 1)))
    .map(errors => errors.inError)
  }


}

/**
 * Manage all database interactions related to invoices
 */
class InvoiceRepository extends Repository with InvoiceSerializer {

  import com.softwaremill.macwire.MacwireMacros._

  private lazy val invoiceRequestBuilder = wire[InvoiceRequestBuilder]

  private val selection: JsObject = Json.obj("invoice" -> 1, "statuses" -> 1, "status" -> 1)

  private val invoicesCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](invoicesCollectionName)

  def save(invoice: Invoice):Future[Boolean] = {
    invoicesCollection
      .save(Json.toJson(invoice))
      .map(errors => errors.inError)
  }

  def find: Future[List[InvoiceData]] = {
    invoicesCollection
      .find(Json.obj(), selection)
      .cursor[InvoiceData]
      .collect[List]()
  }

  def find(invoiceId: String): Future[Option[Invoice]] = {
    invoicesCollection
      .find(invoiceRequestBuilder.idCriteria(invoiceId))
      .one[Invoice]
  }

  def findInProgress: Future[List[InvoiceData]] = {
    invoicesCollection
      .find(invoiceRequestBuilder.inProgressCriteria, selection)
      .cursor[InvoiceData]
      .collect[List]()
  }

  def retrievePDF(invoiceId: String): Future[Option[String]] = {
    if (BSONObjectID.parse(invoiceId).isSuccess) {
      invoicesCollection
        .find(invoiceRequestBuilder.idCriteria(invoiceId), Json.obj("pdfDocument.data.data" -> 1))
        .one[JsObject]
        .map(_.map(jsValue => (jsValue \ "pdfDocument" \ "data" \ "data").as[String]))
    } else {
      Future(None)
    }
  }

  def cancelInvoice(invoiceId: String, invoicePDF: Array[Byte], email: String): Future[Boolean] = {
    update(invoiceId, invoiceRequestBuilder.cancelInvoiceUpdateFieldRequest(invoicePDF, email))
  }

  def updateInvoiceStatus(invoiceId: String, status: String, email: String) = {
    invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, email) match {
      case Some(updateFields) => update(invoiceId, updateFields)
      case None => Future(true)
    }
  }

  private def update(invoiceId: String, update: JsObject): Future[Boolean] = {
    invoicesCollection.update(invoiceRequestBuilder.idCriteria(invoiceId), update).map(errors => errors.inError)
  }

}

/**
 * Construct all request (JsObject) used in InvoiceRepository
 */
class InvoiceRequestBuilder extends RequestBuilder with InvoiceSerializer {

  val inProgressCriteria = Json.obj("$or" -> List(Json.obj("status" -> "created"), Json.obj("status" -> "allocated")))

  def cancelInvoiceUpdateFieldRequest(invoicePDF: Array[Byte], email: String) = {
    val generatedPdfJson = Json.toJson(Attachment("application/pdf", stub = false, invoicePDF))

    val lastStatus = Json.toJson(domain.Status("canceled", DateTime.now(), email))
    val updateObject = Json.obj("pdfDocument" -> generatedPdfJson, "lastStatus" -> lastStatus, "status" -> "canceled")

    updateStatusRequest(lastStatus, updateObject)
  }

  def updateStatusUpdateFieldRequest(status: String, email: String): Option[JsObject] = {
    val lastStatus = Json.toJson(domain.Status(status, DateTime.now(), email))

    val setterObj = status match {
      case "created" => Some(Json.obj("lastStatus" -> lastStatus, "status" -> "created"))
      case "allocated" => Some(Json.obj("lastStatus" -> lastStatus, "status" -> "allocated"))
      case "reallocated" => Some(Json.obj("lastStatus" -> lastStatus))
      case "paid" => Some(Json.obj("lastStatus" -> lastStatus, "status" -> "paid"))
      case "unpaid" => Some(Json.obj("lastStatus" -> lastStatus, "status" -> "allocated"))
      case "canceled" =>
        Logger.error("should use cancel endpoint to update to canceled status")
        None
      case _ =>
        Logger.error(s"status $status unknown, use one of [created, allocated, reallocated, paid, unpaid] statuses")
        None
    }

    setterObj.map(setter => updateStatusRequest(lastStatus, setter))
  }

  private def updateStatusRequest(lastStatus: JsValue, updateObject: JsObject): JsObject = {
    Json.obj(
      "$push" ->
        Json.obj(
          "statuses" -> lastStatus
        ),
      "$set" -> updateObject)
  }

}
