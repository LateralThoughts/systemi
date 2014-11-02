package controllers.api

import auth.WithDomain
import domain._
import engine.{AffectationEngine, InvoiceEngine}
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
  with MongoController
  with InvoiceSerializer
  with AccountSerializer
  with AffectationSerializer
  with AffectationReqSerializer
  with AffectationEngine
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

  // TODO put body in Invoice Engine
  def getLastInvoiceNumber = SecuredAction(WithDomain()).async {
    db.collection[JSONCollection]("invoiceNumber")
      .find(Json.obj())
      .one[InvoiceNumber]
      .map(mayBeObj => Ok(Json.toJson(mayBeObj.get)))
  }

  // TODO put body in Invoice Engine
  def reset(value: Int) = SecuredAction(WithDomain()) {
    Logger.info(s"reset value of invoiceNumber to $value")
    db.collection[JSONCollection]("invoiceNumber")
      .update(Json.obj(), Json.toJson(InvoiceNumber(value)))
    Ok
  }

  def getCanceledInvoices = SecuredAction(WithDomain()).async { implicit request =>
    db
      .collection[JSONCollection]("invoices")
      .find(Json.obj("canceled" -> true), Json.obj("invoice" -> 1, "statuses" -> 1))
      .cursor[JsObject]
      .collect[List]()
      .map(invoices => Ok(Json.toJson(invoices)))
  }

  def findByStatus(status: Option[String], exclude: Option[Boolean]) = SecuredAction(WithDomain()).async { implicit request =>
    val selector = (status: String) => {
      val selectorField =
        if (List("paid", "unpaid") contains status)
          "paymentStatus"
        else if (List("unaffected", "affected") contains status)
          "affectationStatus"
        else
          "lastStatus.name"

      if (exclude.getOrElse(false)) {
        Json.obj(selectorField -> Json.obj("$ne" -> status), "canceled" -> Json.obj("$ne" -> true))
      } else {
        Json.obj(selectorField -> status, "canceled" -> Json.obj("$ne" -> true))
      }
    }

    db
      .collection[JSONCollection]("invoices")
      .find(status.map(selector).getOrElse(Json.obj()), Json.obj("invoice" -> 1, "statuses" -> 1))
      .cursor[JsObject]
      .collect[List]()
      .map(invoices => Ok(Json.toJson(invoices)))
  }

  def addStatusToInvoice(oid: String, status: String) = SecuredAction(WithDomain()) { implicit request =>
    setStatusToInvoice(oid, status, request.user.email.get)

    Ok
  }

  def cancelInvoice(oid: String) = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
    val selector = Json.obj("_id" -> Json.obj("$oid" -> oid))
    db
      .collection[JSONCollection]("invoices")
      .find(selector)
      .one[Invoice]
      .flatMap {
      case (mayBeInvoice: Option[Invoice]) => mayBeInvoice match {
        case Some(invoice) => {
          Logger.info("Loaded invoice, canceling...")
          val lastStatus = Json.toJson(domain.Status("canceled", DateTime.now(), request.user.email.get))

          val generatedPdfDocument = addCanceledWatermark(invoice.pdfDocument.data)

          val generatedPdfJson = Json.toJson(Attachment("application/pdf", stub = false, generatedPdfDocument))
          val updateObject = Json.obj("pdfDocument" -> generatedPdfJson, "lastStatus" -> lastStatus, "canceled" -> true)
          val updateFieldRequest = Json.obj(
            "$push" ->
              Json.obj(
                "statuses" -> lastStatus
              ),
            "$set" -> updateObject)

          db
            .collection[JSONCollection]("invoices")
            .update(selector, updateFieldRequest)

          // delete affectations from this invoice, see issue #36
          Logger.info(s"Remove affectation associated to invoice $oid if needed")
          val invoiceSelector = Json.obj("invoiceId" -> Json.obj("$oid" -> oid))

          db
            .collection[JSONCollection]("affectations")
            .remove(invoiceSelector)

          // remove invoice id from activity if needed
          Logger.info(s"Unset invoice $oid from associated activity if needed")
          val activityUpdateRequest = Json.obj(
           "$unset" -> Json.obj("invoiceId" -> 1)
          )

          db
            .collection[JSONCollection]("activities")
            .update(invoiceSelector, activityUpdateRequest)

          Future(Ok)
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

          val futures = request.body.as[JsArray].value.map { affectationRequest =>

            affectationRequest.validate(affectationReqFormatter) match {
              case errors: JsError => Future(true)
              case result: JsResult[AffectationRequest] => {
                val affectation = IncomeAffectation(result.get.account, result.get.value, Some(invoice._id))

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
              setStatusToInvoice(oid, "affected", request.user.email.get)
              Ok
          }
        }).getOrElse(Future(InternalServerError))
      case _ => Future(BadRequest)
    }
  }

  def getPdfByInvoice(oid: String) = SecuredAction(WithDomain()).async {
    db
      .collection[JSONCollection]("invoices")
      .find(Json.obj("_id" -> Json.obj("$oid" -> oid)), Json.obj("pdfDocument" -> 1))
      .one[JsObject]
      .map {
      case Some(pdfObj) =>
        val doc = (pdfObj \ "pdfDocument" \ "data" \ "data").as[String]
        Ok(Base64.decode(doc)).as("application/pdf")

      case None => BadRequest
    }
  }


  private def setStatusToInvoice(oid: String, status: String, email: String) = {
    val selector = Json.obj("_id" -> Json.obj("$oid" -> oid))
    val lastStatus = Json.toJson(domain.Status(status, DateTime.now(), email))

    val setterObj = if (List("paid", "unpaid") contains status)
      Json.obj("lastStatus" -> lastStatus, "paymentStatus" -> status)
    else if (List("unaffected", "affected") contains status)
      Json.obj("lastStatus" -> lastStatus, "affectationStatus" -> status)
    else
      Json.obj("lastStatus" -> lastStatus)

    val pushToStatesAndLastStatus = Json.obj(
      "$push" ->
        Json.obj(
          "statuses" -> lastStatus
        ),
      "$set" -> setterObj
    )
    Logger.info(s"Add status $status to invoice $oid")
    db
      .collection[JSONCollection]("invoices")
      .update(selector, pushToStatesAndLastStatus)
  }

}
