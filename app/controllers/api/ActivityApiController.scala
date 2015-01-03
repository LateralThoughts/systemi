package controllers.api

import auth.WithDomain
import engine.InvoiceEngine
import play.api.Logger
import repository.Repositories
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
  with Repositories
  with ActivitySerializer
  with GoogleDriveInteraction
  with InvoiceEngine {

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

        Future(Ok(activityId.stringify))
    }
  }

  def getPdfByCRA(oid: String) = SecuredAction(WithDomain()).async {
    activityRepository.retrievePDF(oid)
      .map {
      case None => BadRequest
      case Some(doc) => Ok(Base64.decode(doc)).as("application/pdf")
    }
  }

  def updateActivityWithInvoiceId(activityId: String, invoiceId: String) = {
    val selector = Json.obj("_id" -> Json.obj("$oid" -> activityId))

    val setterObj = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))

    val pushToStatesAndLastStatus = Json.obj(
      "$set" -> setterObj
    )
    Logger.info(s"Add invoice $invoiceId to activity $activityId")
    db
      .collection[JSONCollection]("activities")
      .update(selector, pushToStatesAndLastStatus)
      .map(errors => errors.inError)

  }

  def createAndPushInvoice(oid: String) = SecuredAction(WithDomain()).async { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate(invoiceReqFormat) match {

        case errors: JsError =>
          Future(BadRequest(errors.toString).as("application/json"))

        case result: JsResult[InvoiceRequest] =>

          saveInvoice(result, request).flatMap {

            case Some(invoiceId) =>
              updateActivityWithInvoiceId(oid, invoiceId).map {
                case true =>
                  val message = s"Unable to update activity $oid with invoice $invoiceId"
                  Logger.error(message)
                  InternalServerError(message)
                case false =>
                  Ok(routes.InvoiceApiController.getPdfByInvoice(invoiceId).absoluteURL())
              }

            case None =>
              Future(InternalServerError("Unable to save invoice"))
          }
      }
      case None => Future(BadRequest)
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
