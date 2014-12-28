package repository

import domain.{Attachment, InvoiceData, InvoiceSerializer, Invoice}
import org.joda.time.DateTime
import play.Logger
import play.api.libs.json.{Json, JsObject}
import play.modules.reactivemongo.{ReactiveMongoPlugin, MongoController}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError

import scala.concurrent.Future
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Manage all database interactions related to invoices
 */
class InvoiceRepository extends Repository with InvoiceSerializer {

  import com.softwaremill.macwire.MacwireMacros._

  private lazy val invoiceRequestBuilder = wire[InvoiceRequestBuilder]

  private val selection: JsObject = Json.obj("invoice" -> 1, "statuses" -> 1, "status" -> 1)

  private val invoicesCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](invoicesCollectionName)

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

  def retrievePDF(invoiceId: String): Future[String] = {
    invoicesCollection
      .find(invoiceRequestBuilder.idCriteria(invoiceId), Json.obj("pdfDocument.data.data" -> 1))
      .one[JsObject]
      .map {
      case Some(pdfObj) => (pdfObj \ "pdfDocument" \ "data" \ "data").as[String]
      case None => ""
    }
  }

  def cancelInvoice(invoiceId: String, invoicePDF: Array[Byte], email: String): Future[Boolean] = {
    update(invoiceId, invoiceRequestBuilder.cancelInvoiceUpdateFieldRequest(invoicePDF, email))
  }

  def updateInvoiceStatus(invoiceId: String, status: String, email: String) = {
    update(invoiceId, invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, email))
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

    Json.obj(
      "$push" ->
        Json.obj(
          "statuses" -> lastStatus
        ),
      "$set" -> updateObject)
  }

  def updateStatusUpdateFieldRequest(status: String, email: String) = {
    val lastStatus = Json.toJson(domain.Status(status, DateTime.now(), email))

    val setterObj = status match {
      case "created" => Json.obj("lastStatus" -> lastStatus, "status" -> "created")
      case "allocated" => Json.obj("lastStatus" -> lastStatus, "status" -> "allocated")
      case "reallocated" => Json.obj("lastStatus" -> lastStatus)
      case "paid" => Json.obj("lastStatus" -> lastStatus, "status" -> "paid")
      case "unpaid" => Json.obj("lastStatus" -> lastStatus, "status" -> "allocated")
      case "canceled" => Json.obj("lastStatus" -> lastStatus, "status" -> "canceled")
      case _ =>
        Logger.error(s"status $status unknown, use one of [created, allocated, reallocated, paid, unpaid, canceled] statuses")
        Json.obj()
    }

    Json.obj(
      "$push" ->
        Json.obj(
          "statuses" -> lastStatus
        ),
      "$set" -> setterObj
    )
  }

}
