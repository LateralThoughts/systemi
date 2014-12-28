package controllers.api

import auth.WithDomain
import domain._
import engine.InvoiceEngine
import org.bouncycastle.util.encoders.Base64
import org.joda.time.DateTime
import play.Logger
import play.api.libs.json._
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.Future

class InvoiceApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with ApiController
  with MongoController
  with InvoiceSerializer
  with AccountSerializer
  with AffectationSerializer
  with AffectationReqSerializer
  with InvoiceEngine {


  def createAndPushInvoice = SecuredAction(WithDomain()) { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate(invoiceReqFormat) match {

        case errors: JsError =>
          BadRequest(errors.toString).as("application/json")

        case result: JsResult[InvoiceRequest] =>
          val generatedPdfDocument = invoiceRequestToPdfBytes(result.get)

          val invoiceId = insertInvoice(request, result.get, generatedPdfDocument)

          Ok(routes.InvoiceApiController.getPdfByInvoice(invoiceId.stringify).absoluteURL())
      }
      case None => request.body.asFormUrlEncoded match {
        case Some(body) =>
          val invoiceRequest = invoiceFromForm(body)

          val shouldUpload = body.get("shouldUpload").map(_.head).exists(_.equalsIgnoreCase("on"))

          val generatedPdfDocument = invoiceRequestToPdfBytes(invoiceRequest)

          if (shouldUpload) {
            val invoiceId = insertInvoice(request, invoiceRequest, generatedPdfDocument)
          }
          Ok(generatedPdfDocument).as("application/pdf")

        case None => Ok("no go")
      }
    }
  }

  // TODO put body in Invoice Repository
  def getLastInvoiceNumber = SecuredAction(WithDomain()).async {
    db.collection[JSONCollection]("invoiceNumber")
      .find(Json.obj())
      .one[InvoiceNumber]
      .map(mayBeObj => Ok(Json.toJson(mayBeObj.get)))
  }

  // TODO put body in Invoice Repository
  def reset(value: Int) = SecuredAction(WithDomain()) {
    Logger.info(s"reset value of invoiceNumber to $value")
    db.collection[JSONCollection]("invoiceNumber")
      .update(Json.obj(), Json.toJson(InvoiceNumber(value)))
    Ok
  }

  def find = SecuredAction(WithDomain()).async { implicit request =>
    invoiceRepository
      .find()
      .map(invoices => Ok(Json.toJson(invoices)))
  }

  def addStatusToInvoice(oid: String, status: String) = SecuredAction(WithDomain()).async { implicit request =>
    setStatusToInvoice(oid, status, request.user.email.get).map {
      case true => InternalServerError
      case false => Ok
    }
  }

  def cancelInvoice(invoiceId: String) = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
    invoiceRepository
      .find(invoiceId)
      .flatMap {
      case (mayBeInvoice: Option[Invoice]) => mayBeInvoice match {
        case Some(invoice) => {
          Logger.info("Loaded invoice, canceling...")
          val lastStatus = Json.toJson(domain.Status("canceled", DateTime.now(), request.user.email.get))

          val generatedPdfDocument = addCanceledWatermark(invoice.pdfDocument.data)

          val generatedPdfJson = Json.toJson(Attachment("application/pdf", stub = false, generatedPdfDocument))
          val updateObject = Json.obj("pdfDocument" -> generatedPdfJson, "lastStatus" -> lastStatus, "status" -> "canceled")
          val updateFieldRequest = Json.obj(
            "$push" ->
              Json.obj(
                "statuses" -> lastStatus
              ),
            "$set" -> updateObject)

          invoiceRepository.update(invoiceId, updateFieldRequest)

          // delete affectations from this invoice, see issue #36
          allocationRepository.removeByInvoice(invoiceId).map( hasErrors =>
            if (hasErrors) Logger.error(s"unable to delete allocations of invoice $invoiceId")
          )

          // remove invoice id from activity if needed
          Logger.info(s"Unset invoice $invoiceId from associated activity if needed")
          val invoiceSelector = Json.obj("invoiceId" -> Json.obj("$oid" -> invoiceId))
          val activityUpdateRequest = Json.obj(
           "$unset" -> Json.obj("invoiceId" -> 1)
          )

          db
            .collection[JSONCollection]("activities")
            .update(invoiceSelector, activityUpdateRequest)
          .map(errors =>
            if (errors.inError) {
              Logger.error(s"unable to unset invoice $invoiceId from associated activity")
              InternalServerError
            } else {
              Ok
            })
        }
        case None => Future(InternalServerError)
      }
      case _ => Future(BadRequest)
    }
  }


  def affectToAccount(oid: String) = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
    db
      .collection[JSONCollection]("invoices")
      .find(Json.obj("_id" -> Json.obj("$oid" -> oid)))
      .one[Invoice]
      .flatMap {
      case (mayBeInvoice: Option[Invoice]) =>
        (for (invoice <- mayBeInvoice) yield {
          Logger.info("Loaded invoice, creating affectations...")

          allocationRepository.removeByInvoice(invoice._id.stringify) // TODO remove allocations after error checking

          val futures = request.body.as[JsArray].value.map { affectationRequest =>

            affectationRequest.validate(affectationReqFormatter) match {
              case errors: JsError => Future(true)
              case result: JsResult[AffectationRequest] => {
                val affectation = IncomeAffectation(result.get.account, result.get.value, invoice._id)

                Logger.info(affectationRequest.toString())

                db
                  .collection[JSONCollection]("affectations")
                  .save(affectation)
                  .map(errors => errors.inError)
              }
            }
          }

          val futureHasAtLeastOneFailure = Future.sequence(futures)
            .map(_.foldLeft(false)((acc, current) => acc || current))

          futureHasAtLeastOneFailure.map {
            case true => InternalServerError
            case false =>
              if (invoice.isAllocated) {
                setStatusToInvoice(oid, "reallocated", request.user.email.get)
              } else {
                setStatusToInvoice(oid, "allocated", request.user.email.get)
              }
              Ok
          }
        }).getOrElse(Future(InternalServerError))
      case _ => Future(BadRequest)
    }
  }

  def getPdfByInvoice(oid: String) = SecuredAction(WithDomain()).async {
    invoiceRepository.invoicesCollection
      .find(Json.obj("_id" -> Json.obj("$oid" -> oid)), Json.obj("pdfDocument" -> 1))
      .one[JsObject]
      .map {
      case Some(pdfObj) =>
        val doc = (pdfObj \ "pdfDocument" \ "data" \ "data").as[String]
        Ok(Base64.decode(doc)).as("application/pdf")

      case None => BadRequest
    }
  }

  private def setStatusToInvoice(oid: String, status: String, email: String): Future[Boolean] = {
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
        return Future(true)
    }

    val pushToStatesAndLastStatus = Json.obj(
      "$push" ->
        Json.obj(
          "statuses" -> lastStatus
        ),
      "$set" -> setterObj
    )
    Logger.info(s"Add status $status to invoice $oid")
    invoiceRepository
      .update(oid, pushToStatesAndLastStatus)
    .map(errors => errors.inError)
  }

}
