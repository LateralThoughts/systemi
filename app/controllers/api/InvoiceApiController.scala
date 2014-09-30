package controllers.api

import domain._
import org.bouncycastle.util.encoders.Base64
import org.joda.time.DateTime
import play.Logger
import play.api.libs.concurrent.Promise
import play.api.libs.json.{JsError, JsObject, JsResult, Json}
import play.api.mvc.{AnyContent, Action, Controller}
import play.libs.Akka
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}
import util.pdf.GoogleDriveInteraction

import scala.concurrent.Future

class InvoiceApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with InvoiceSerializer
  with AccountSerializer
  with AffectationSerializer
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
    setStatusToInvoice(oid, status, request)

    Ok
  }

  def affectToAccount(oid: String, account: String) = SecuredAction.async { implicit request =>
    val futureMayBeInvoice = db
      .collection[JSONCollection]("invoices")
      .find(Json.obj("_id" -> Json.obj("$oid" -> oid)))
      .one[Invoice]

    val futureMayBeAccount = db
      .collection[JSONCollection]("accounts")
      .find(Json.obj("_id" -> Json.obj("$oid" -> account)))
      .one[Account]

    Logger.info(s"Going to load invoice $oid and account $account, saving affectation")
    val future = for {
      mayBeInvoice: Option[Invoice] <- futureMayBeInvoice
      mayBeAccount: Option[Account] <- futureMayBeAccount
    } yield (mayBeAccount, mayBeInvoice)

    future.map {
      case (mayBeAccount: Option[Account], mayBeInvoice: Option[Invoice]) =>
        (for (account <- mayBeAccount; invoice <- mayBeInvoice) yield {
          val affectation = IncomeAffectation(invoice, account, invoice.totalHT)
          Logger.info(s"Loaded invoice and account, saving affectation $affectation")
          db
            .collection[JSONCollection]("affectations")
            .save(Json.toJson(affectation))

          setStatusToInvoice(oid, "affected", request)

          Ok
        }).getOrElse(InternalServerError)
      case _ => BadRequest
    }
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


  private def setStatusToInvoice(oid: String, status: String, request: SecuredRequest[AnyContent]) = {
    val selector = Json.obj("_id" -> Json.obj("$oid" -> oid))
    val lastStatus = Json.toJson(domain.Status(status, DateTime.now(), request.user.email.get))
    val pushToStatesAndLastStatus = Json.obj(
      "$push" ->
        Json.obj(
          "statuses" -> lastStatus
        ),
      "$set" -> Json.obj("lastStatus" -> lastStatus)
    )
    Logger.info(s"Add status $status to invoice $oid")
    db
      .collection[JSONCollection]("invoices")
      .update(selector, pushToStatesAndLastStatus)
  }
}
