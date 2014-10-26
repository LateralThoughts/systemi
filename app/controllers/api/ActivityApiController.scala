package controllers.api

import auth.WithDomain
import engine.InvoiceEngine
import play.Logger
import securesocial.core.RuntimeEnvironment
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import domain._
import util.pdf.GoogleDriveInteraction
import play.api.libs.json.{JsObject, Json, JsResult, JsError}
import securesocial.core.BasicProfile
import domain.ActivityRequest
import play.modules.reactivemongo.json.collection.JSONCollection
import org.bouncycastle.util.encoders.Base64
import scala.concurrent.Future
import reactivemongo.bson.BSONObjectID
import play.libs.Akka

class ActivityApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with ActivitySerializer
  with GoogleDriveInteraction
  with InvoiceEngine {

  import play.modules.reactivemongo.json.BSONFormats._

  private val akkaSystem = Akka.system
  private lazy val activityActor = akkaSystem.actorSelection(akkaSystem / "activity")

  def createAndPushCRA = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
    request.body.validate(activityReqFormat) match {

      case errors: JsError =>
        val errorMessage = "La requête ne peut être traitée à cause de données non valides, merci de corriger le formulaire"
        Future(Ok(controllers.routes.ActivityController.index().absoluteURL()).flashing(("error",errorMessage)))

      case result: JsResult[ActivityRequest] =>
        val generatedPdfDocument = activityToPdfBytes(result.get)
        val activityId = BSONObjectID.generate

        activityActor ! Activity(activityId, result.get, Attachment("application/pdf", stub = false, generatedPdfDocument), None)

        Future(Ok(routes.ActivityApiController.getPdfByCRA(activityId.stringify).absoluteURL()))
    }
  }


  def getPdfByCRA(oid: String) = SecuredAction(WithDomain()).async {
    db
      .collection[JSONCollection]("activities")
      .find(Json.obj("_id" -> Json.obj("$oid" -> oid)), Json.obj("pdfDocument" -> 1))
      .one[JsObject]
      .map {
      case Some(pdfObj) =>
        val doc = (pdfObj \ "pdfDocument" \ "data" \ "data").as[String]
        Ok(Base64.decode(doc)).as("application/pdf")

      case None => BadRequest
    }

  }

  def updateActivityWithInvoiceId(activityId: String, invoiceId: BSONObjectID) = {
    val selector = Json.obj("_id" -> Json.obj("$oid" -> activityId))

    val setterObj = Json.obj("invoiceId" -> Json.toJson(invoiceId))

    val pushToStatesAndLastStatus = Json.obj(
      "$set" -> setterObj
    )
    Logger.info(s"Add invoice $invoiceId to activity $activityId")
    db
      .collection[JSONCollection]("activities")
      .update(selector, pushToStatesAndLastStatus)

  }

  def createAndPushInvoice(oid: String) = SecuredAction(WithDomain()) { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate(invoiceReqFormat) match {

        case errors: JsError =>
          BadRequest(errors.toString).as("application/json")

        case result: JsResult[InvoiceRequest] =>
          val generatedPdfDocument = invoiceRequestToPdfBytes(result.get)

          val invoiceId = insertInvoice(request, result.get, generatedPdfDocument)

          updateActivityWithInvoiceId(oid, invoiceId)
          Ok(routes.InvoiceApiController.getPdfByInvoice(invoiceId.stringify).absoluteURL())
      }
      case None => BadRequest
    }

  }

  def findAll = SecuredAction(WithDomain()).async {
    db
      .collection[JSONCollection]("activities")
      .find(Json.obj(), Json.obj("activity" -> 1, "id" -> 1, "invoiceId" -> 1))
      .cursor[JsObject]
      .collect[List]()
      .map(users => Ok(Json.toJson(users)))
  }

  def delete(oid: String) = SecuredAction(WithDomain()).async {
    db
    .collection[JSONCollection]("activities")
    .remove(Json.obj("_id" -> Json.obj("$oid" -> oid)))
    .map(x => Ok(x.stringify))
  }

}
