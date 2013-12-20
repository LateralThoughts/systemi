package controllers

import util.pdf._
import views.html._
import play.api._
import play.api.mvc._
import domain._
import util.DrivePersistence
import play.api.libs.json._

object Application extends Controller with InvoiceSerializer with InvoiceLinesAnalyzer with DrivePersistence{

  def index = Action {
    Ok(views.html.index())
  }

  def showInvoice = Action { implicit request => {
      request.body.asJson match {
        case Some(json) => json.validate(invoiceReads) match {
          case errors:JsError => Ok(errors.toString).as("application/json")
          case result: JsResult[InvoiceRequest] => Ok(invoiceToPdf(result.get)).as("application/pdf")
        }
        case None => request.body.asFormUrlEncoded match {
          case Some(body) => Ok(invoiceFromForm(body.map({ case(k,v) => (k, v.headOption.get)}))).as("application/pdf")
          case None => Ok("no go")
        }
      }
    }
  }

  def invoiceHtml = Action {
    val invoiceRequest = InvoiceRequest("facture", "VT055", 
      ClientDefinition("VIDAL", "27 rue camille desmoulins", "94550", "chevilly"), 
        List(InvoiceLine("blabla", 25.0, 450.0, 19.6)))

    val client = invoiceRequest.client
    val title = invoiceRequest.title
    val id = invoiceRequest.invoiceNumber
    val invoiceLines = invoiceRequest.invoice

    Ok(views.html.invoice(title, id, client, invoiceLines))
  }

  private def invoiceFromForm(body : Map[String, String]) :Array[Byte]= {
    val invoiceRequest = InvoiceRequest(
      body.get("title").get,
      body.get("invoiceNumber").get,
      ClientDefinition(body.get("clientName").get, body.get("clientAddress").get, body.get("clientCity").get, body.get("clientPostalCode").get),
      List(InvoiceLine(body.get("invoiceDescription").get, 
        body.get("invoiceDays").get.toDouble, 
        body.get("invoiceDailyRate").get.toDouble,
        body.get("invoiceTaxRate").get.toDouble)
      )
    )
    invoiceToPdf(invoiceRequest)
  }

  private def invoiceToPdf(invoiceRequest : InvoiceRequest) :Array[Byte] = {
    val client = invoiceRequest.client
    val title = invoiceRequest.title
    val id = invoiceRequest.invoiceNumber
    val invoiceLines = invoiceRequest.invoice

    PDF.toBytes(invoice.render(title, id, client, invoiceLines))
  } 
}