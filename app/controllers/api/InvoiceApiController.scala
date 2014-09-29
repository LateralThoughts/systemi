package controllers.api

import domain._
import org.bouncycastle.util.encoders.Base64
import org.joda.time.DateTime
import play.Logger
import play.api.libs.json.{JsError, JsObject, JsResult, Json}
import play.api.mvc.{Action, Controller}
import play.libs.Akka
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}
import util.pdf.GoogleDriveInteraction

class InvoiceApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with InvoiceSerializer
  with GoogleDriveInteraction
  with securesocial.core.SecureSocial[BasicProfile] {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  private val akkaSystem = Akka.system
  private lazy val invoiceActor = akkaSystem.actorSelection(akkaSystem / "invoice")

  def createAndPushInvoice = SecuredAction { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate(invoiceReqFormat) match {

        case errors:JsError =>
          Ok(errors.toString).as("application/json")

        case result: JsResult[InvoiceRequest] =>
          Ok(invoiceToPdfBytes(result.get)).as("application/pdf")
      }
      case None => request.body.asFormUrlEncoded match {
        case Some(body) =>
          val invoiceRequest = invoiceFromForm(body)

          val shouldUpload = body.get("shouldUpload").map(_.head).exists(_.equalsIgnoreCase("on"))

          val generatedPdfDocument = invoiceToPdfBytes(invoiceRequest)

          if (shouldUpload) {
            val status = domain.Status("created", DateTime.now(), request.user.email.get)
            invoiceActor ! Invoice(invoiceRequest, Attachment("application/pdf", stub = false, generatedPdfDocument), List(status), status)
          }

          Ok(generatedPdfDocument).as("application/pdf")

        case None => Ok("no go")
      }
    }
  }

  def getLastInvoiceNumber = Action.async {
    db.collection[JSONCollection]("invoiceNumber")
      .find(Json.obj())
      .one[InvoiceNumber]
      .map(mayBeObj => Ok(Json.toJson(mayBeObj.get)))
  }

  def reset(value: Int) = Action {
    Logger.info(s"reset value of invoiceNumber to $value")
    db.collection[JSONCollection]("invoiceNumber")
      .update(Json.obj(), Json.toJson(InvoiceNumber(value)))
    Ok
  }

  def findPendingInvoice = Action.async {
    db
      .collection[JSONCollection]("invoices_tasks")
      .find(Json.obj(), Json.obj("invoice" -> 1, "statuses" -> 1))
      .cursor[JsObject]
      .collect[List]()
      .map(invoices => Ok(Json.toJson(invoices)))
  }

  def findByStatus(status: Option[String], exclude: Option[Boolean]) = Action.async { implicit request =>
    val selector = (status: String) => if (exclude.getOrElse(false )) {
      Json.obj("lastStatus.name" -> Json.obj("$ne" -> status))
    } else {
      Json.obj("lastStatus.name" -> status)
    }

    db
      .collection[JSONCollection]("invoices")
      .find(status.map(selector).getOrElse(Json.obj()), Json.obj("invoice" -> 1, "statuses" -> 1))
      .cursor[JsObject]
      .collect[List]()
      .map(invoices => Ok(Json.toJson(invoices)))
  }

  def addStatusToInvoice(oid: String, status: String) = SecuredAction { implicit request =>
    val selecteur = Json.obj("_id" -> Json.obj("$oid" -> oid))
    val lastStatus = Json.toJson(domain.Status(status, DateTime.now(), request.user.email.get))
    val pushToStatesAndLastStatus = Json.obj(
      "$push" ->
        Json.obj(
          "statuses" -> lastStatus
        ),
      "$set" -> Json.obj("lastStatus" -> lastStatus)
    )

    db
      .collection[JSONCollection]("invoices")
      .update(selecteur, pushToStatesAndLastStatus)

    Ok
  }

  def getPdfByInvoice(oid: String) = Action.async {
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

}
