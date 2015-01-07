package repository

import domain.{Activity, ActivitySerializer}
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import play.api.Play.current
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Manage all database interactions related to activities
 */
class ActivityRepository extends Repository with ActivitySerializer {

  import com.softwaremill.macwire.MacwireMacros._

  private lazy val activityRequestBuilder = wire[ActivityRequestBuilder]

  private val activitiesCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](activitiesCollectionName)

  def save(activity: Activity):Future[Boolean] = {
    activitiesCollection
      .save(Json.toJson(activity))
      .map(errors => errors.inError)
  }

  def unsetInvoiceFromActivity(invoiceId: String): Future[Boolean] = {
    activitiesCollection
      .update(activityRequestBuilder.invoiceCriteria(invoiceId), activityRequestBuilder.removeInvoiceUpdateField)
      .map(errors => errors.inError)
  }

  def retrievePDF(invoiceId: String): Future[Option[String]] = {
    if (BSONObjectID.parse(invoiceId).isSuccess) {
      activitiesCollection
        .find(activityRequestBuilder.idCriteria(invoiceId), Json.obj("pdfDocument.data.data" -> 1))
        .one[JsObject]
        .map(_.map(jsValue => (jsValue \ "pdfDocument" \ "data" \ "data").as[String]))
    } else {
      Future(None)
    }
  }

}

/**
 * Construct all request (JsObject) used in ActivityRepository
 */
class ActivityRequestBuilder extends RequestBuilder with ActivitySerializer {

  val removeInvoiceUpdateField = Json.obj("$unset" -> Json.obj("invoiceId" -> 1))

  def invoiceCriteria(invoiceId: String) = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))

}
