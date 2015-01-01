package engine

import domain.{Attachment, Invoice, InvoiceRequest}
import org.joda.time.DateTime
import play.api.mvc.AnyContent
import play.libs.Akka
import reactivemongo.bson.BSONObjectID
import securesocial.core.BasicProfile

trait InvoiceEngine extends securesocial.core.SecureSocial[BasicProfile] {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  private val akkaSystem = Akka.system
  private lazy val invoiceActor = akkaSystem.actorSelection(akkaSystem / "invoice")

  protected def insertInvoice(request: SecuredRequest[AnyContent], invoiceRequest: InvoiceRequest, generatedPdfDocument: Array[Byte]): BSONObjectID = {
    val status = domain.Status("created", DateTime.now(), request.user.email.get)
    val invoiceId = BSONObjectID.generate
    invoiceActor !(
      "created",
      Invoice(invoiceId, invoiceRequest, Attachment("application/pdf", stub = false, generatedPdfDocument), List(status), status),
      retrieveAccessToken(request)
      )
    invoiceId
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
