package controllers.api

import auth.WithDomain
import domain._
import engine.InvoiceEngine
import org.bouncycastle.util.encoders.Base64
import play.Logger
import play.api.libs.json._
import play.api.mvc.{AnyContent, Controller}
import repository.Repositories
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.Future

class InvoiceApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with Repositories
  with InvoiceSerializer
  with AllocationSerializer
  with InvoiceEngine {


  def createAndPushInvoice = SecuredAction(WithDomain()).async { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate(invoiceReqFormat) match {

        case errors: JsError =>
          Future(BadRequest(errors.toString).as("application/json"))

        case result: JsResult[InvoiceRequest] =>
          saveInvoice(result, request).map {
            case Some(invoiceId) => Ok(invoiceId)
            case None => InternalServerError("Unable to save invoice")
          }
      }
      case None => Future(BadRequest("should send data as application/json"))
    }
  }

  def getLastInvoiceNumber = SecuredAction(WithDomain()).async {
    invoiceNumberRepository.getLastInvoiceNumber.map(invoiceNumber => Ok(Json.toJson(invoiceNumber)))
  }

  def incrementInvoiceNumber = SecuredAction(WithDomain()).async {
    invoiceNumberRepository.increment.map {
      case false => Ok
      case true => InternalServerError
    }
  }

  def reset(value: Int) = SecuredAction(WithDomain()).async {
    Logger.info(s"reset value of invoiceNumber to $value")
    invoiceNumberRepository.reset("VT", value).map {
      case false => Ok
      case true => InternalServerError
    }
  }

  def find = SecuredAction(WithDomain()).async { implicit request =>
    invoiceRepository
      .find
      .map(invoices => Ok(Json.toJson(invoices)))
  }

  def findDelayedInvoices = SecuredAction(WithDomain()).async { implicit request =>
    invoiceRepository
      .findInProgress
      .map(invoices => Ok(Json.toJson(invoices.filter(invoice => invoice.isDelayed))))
  }

  def addStatusToInvoice(oid: String, status: String) = SecuredAction(WithDomain()).async { implicit request =>
    moveInvoiceInDrive(oid, status, request)

    invoiceRepository.updateInvoiceStatus(oid, status, request.user.email.get).map {
      case false => Ok
      case true => InternalServerError
    }
  }

  private def moveInvoiceInDrive(oid: String, status: String, request: SecuredRequest[AnyContent]) {
    status match {
      case "paid" =>
        moveInvoice(oid, request, moveInvoiceToPaidFolder)
      case "unpaid" =>
        moveInvoice(oid, request, moveInvoiceToInProgressFolder)
      case "canceled" =>
        moveInvoice(oid, request, moveInvoiceToCanceledFolder)
      case _ =>
        Logger.debug(s"No need to move invoice for status $status")
    }
  }

  private def moveInvoice(oid: String, request: SecuredRequest[AnyContent], movingFunction: (SecuredRequest[AnyContent], Invoice) => Unit) {
    invoiceRepository.find(oid).flatMap {
      case Some(invoice) =>
        movingFunction(request, invoice)
        Future(true)
      case None =>
        Logger.error(s"Unable to find invoice $oid, can't update status")
        Future(false)
    }
  }

  def cancelInvoice(invoiceId: String) = SecuredAction(WithDomain()).async { implicit request =>
    invoiceRepository
      .find(invoiceId)
      .flatMap {
      case (mayBeInvoice: Option[Invoice]) => mayBeInvoice match {
        case Some(invoice) => {
          Logger.info(s"Loaded invoice $invoiceId, canceling...")
          val generatedPdfDocument = addCanceledWatermark(invoice.pdfDocument.data)

          invoiceRepository.cancelInvoice(invoiceId, generatedPdfDocument, request.user.email.get).map(hasErrors =>
            if (hasErrors) Logger.error(s"unable to cancel invoice $invoiceId")
          )

          // delete allocations from this invoice, see issue #36
          Logger.info(s"Remove allocations associated to invoice $invoiceId")
          allocationRepository.removeByInvoice(invoiceId).map(hasErrors =>
            if (hasErrors) Logger.error(s"unable to delete allocations of invoice $invoiceId")
          )

          // move invoice to canceled folder on drive
          moveInvoiceToCanceledFolder(request, invoice)

          // remove invoice id from activity if needed
          Logger.info(s"Unset invoice $invoiceId from associated activity if needed")
          activityRepository.unsetInvoiceFromActivity(invoiceId)
            .map {
            case true =>
              Logger.error(s"unable to unset invoice $invoiceId from associated activity")
              InternalServerError
            case false => Ok
          }

        }
        case None => Future(InternalServerError)
      }
      case _ => Future(BadRequest)
    }

  }


  def allocateToAccount(oid: String) = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
    invoiceRepository
      .find(oid)
      .flatMap {
      case (mayBeInvoice: Option[Invoice]) =>
        (for (invoice <- mayBeInvoice) yield {
          Logger.info("Loaded invoice, creating allocations...")

          allocationRepository.removeByInvoice(invoice._id.stringify) // TODO remove allocations after error checking

          val futures = request.body.as[JsArray].value.map { allocationRequest =>

            allocationRequest.validate(allocationReqFormatter) match {
              case errors: JsError => Future(true)
              case result: JsResult[AllocationRequest] => {
                val allocation = IncomeAllocation(result.get.account, result.get.value, invoice._id)

                Logger.info(allocationRequest.toString())

                allocationRepository.save(allocation)
              }
            }
          }

          checkFailures(futures).map {
            case true => InternalServerError
            case false =>
              val status = if (invoice.isAllocated) "reallocated" else "allocated"
              Logger.info(s"Add status $status to invoice $oid")
              invoiceRepository.updateInvoiceStatus(oid, status, request.user.email.get).map(hasErrors =>
                if (hasErrors) Logger.error(s"unable to add status $status to invoice $oid")
              )
              Ok
          }
        }).getOrElse(Future(InternalServerError))
      case _ => Future(BadRequest)
    }
  }

  private def checkFailures(futures: Seq[Future[Boolean]]): Future[Boolean] = {
    Future.sequence(futures)
      .map(_.foldLeft(false)((acc, current) => acc || current))
  }

  def getPdfByInvoice(oid: String) = SecuredAction(WithDomain()).async {
    invoiceRepository.retrievePDF(oid)
      .map {
      case None => BadRequest
      case Some(doc) => Ok(Base64.decode(doc)).as("application/pdf")
    }
  }

}
