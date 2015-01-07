package controllers.api

import auth.WithDomain
import domain.{ActivityRequest, _}
import engine.InvoiceEngine
import org.bouncycastle.util.encoders.Base64
import play.api.Logger
import play.api.libs.json.{JsError, JsObject, JsResult, Json}
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import repository.Repositories
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.Future

class ActivityApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with Repositories
  with ActivitySerializer
  with InvoiceEngine {

  def createAndPushCRA = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
    request.body.validate(activityReqFormat) match {

      case errors: JsError =>
        val errorMessage = "La requête ne peut être traitée à cause de données non valides, merci de corriger le formulaire"
        Future(Ok(controllers.routes.ActivityController.index().absoluteURL()).flashing(("error",errorMessage)))

      case result: JsResult[ActivityRequest] =>
        val generatedPdfDocument = activityToPdfBytes(result.get)
        val activityId = BSONObjectID.generate
        val activity = Activity(activityId, result.get, Attachment("application/pdf", stub = false, generatedPdfDocument), None)

        activityRepository.save(activity).map {
          case false =>
            Logger.info(s"Saved activity $activity")
            Ok(activityId.stringify)
          case true =>
            Logger.error(s"Unable to save activity $activity")
            InternalServerError
        }
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
      .find(Json.obj(), Json.obj("activity" -> 1, "invoiceId" -> 1))
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
