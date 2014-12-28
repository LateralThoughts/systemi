package repository

import domain.ActivitySerializer
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Manage all database interactions related to activities
 */
class ActivityRepository extends Repository {

  import com.softwaremill.macwire.MacwireMacros._

  private lazy val activityRequestBuilder = wire[ActivityRequestBuilder]

  private val activitiesCollection: JSONCollection = ReactiveMongoPlugin.db
    .collection[JSONCollection](activitiesCollectionName)

  def unsetInvoiceFromActivity(invoiceId: String): Future[Boolean] = {
    activitiesCollection
      .update(activityRequestBuilder.invoiceCriteria(invoiceId), activityRequestBuilder.removeInvoiceUpdateField)
      .map(errors => errors.inError)
  }

}

/**
 * Construct all request (JsObject) used in ActivityRepository
 */
class ActivityRequestBuilder extends RequestBuilder with ActivitySerializer {

  val removeInvoiceUpdateField = Json.obj("$unset" -> Json.obj("invoiceId" -> 1))

  def invoiceCriteria(invoiceId: String) = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))

}
