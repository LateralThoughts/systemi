package controllers.api

import domain._
import org.bouncycastle.util.encoders.Base64
import play.Logger
import play.api.libs.json.{JsObject, Json, JsResult, JsError}
import play.api.mvc.{Action, Controller}
import play.libs.Akka
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
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

          val shouldUpload = body.get("shouldUpload").map(_.head).exists(_.toBoolean)

          val generatedPdfDocument = invoiceToPdfBytes(invoiceRequest)

          if (shouldUpload)
            invoiceActor ! Invoice(invoiceRequest, Attachment("application/pdf", stub = false, generatedPdfDocument))

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

  def findAll = Action.async {
    db
      .collection[JSONCollection]("invoices")
      .find(Json.obj(), Json.obj("invoice" -> 1))
      .cursor[JsObject]
      .collect[List]()
      .map(invoices => Ok(Json.toJson(invoices)))
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
