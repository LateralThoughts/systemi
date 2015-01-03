package engine

import domain.{Attachment, Invoice, InvoiceRequest, InvoiceSerializer}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.JsResult
import play.api.mvc.AnyContent
import play.libs.Akka
import reactivemongo.bson.BSONObjectID
import repository.Repositories
import securesocial.core.BasicProfile

import scala.concurrent.Future

trait InvoiceEngine extends securesocial.core.SecureSocial[BasicProfile] with Repositories with InvoiceSerializer {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  private val akkaSystem = Akka.system
  private lazy val invoiceActor = akkaSystem.actorSelection(akkaSystem / "invoice")

  protected def saveInvoice(result: JsResult[InvoiceRequest], request: SecuredRequest[AnyContent]): Future[Option[String]] = {

    val invoice: Invoice = createInvoice(result, request)

    invoiceRepository.save(invoice).map {
      case false =>
        Logger.info(s"Saved invoice $invoice")
        invoiceNumberRepository.increment.map(hasErrors => if (hasErrors) Logger.error("Unable to increment invoice number"))
        sendMessageToInvoiceActor(request, invoice, "created")
        Some(invoice._id.stringify)
      case true =>
        Logger.error(s"Unable to save invoice $invoice")
        None
    }
  }

  private def createInvoice(result: JsResult[InvoiceRequest], request: SecuredRequest[AnyContent]): Invoice = {
    val generatedPdfDocument = invoiceRequestToPdfBytes(result.get)
    val status = domain.Status("created", DateTime.now(), request.user.email.get)
    val invoiceId = BSONObjectID.generate

    Invoice(invoiceId, result.get, Attachment("application/pdf", stub = false, generatedPdfDocument), List(status), status)
  }

  protected def moveInvoiceToCanceledFolder(request: SecuredRequest[AnyContent], invoice: Invoice) = {
    sendMessageToInvoiceActor(request, invoice, "canceled")
  }

  protected def moveInvoiceToPaidFolder(request: SecuredRequest[AnyContent], invoice: Invoice) = {
    sendMessageToInvoiceActor(request, invoice, "paid")
  }

  protected def moveInvoiceToInProgressFolder(request: SecuredRequest[AnyContent], invoice: Invoice) = {
    sendMessageToInvoiceActor(request, invoice, "inProgress")
  }

  def sendMessageToInvoiceActor(request: SecuredRequest[AnyContent], invoice: Invoice, status: String) {
    invoiceActor !(
      status,
      invoice,
      retrieveAccessToken(request)
      )
  }

  def retrieveAccessToken(request: SecuredRequest[AnyContent]): String = {
    request.user.oAuth2Info.map(_.accessToken).get
  }

}
