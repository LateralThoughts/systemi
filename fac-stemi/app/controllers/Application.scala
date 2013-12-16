package controllers

import util.pdf._
import views.html._
import play.api._
import play.api.mvc._
import domain._
import play.api.libs.json._

object Application extends Controller with InvoiceSerializer with InvoiceLinesAnalyzer {

  def index = Action {
    Ok(PDF.toBytes(invoice.render("Facture de novembre", "VT0777", ClientDefinition("VIDAL", "37 rue des mathurins"), List(InvoiceLine("desc", 25.0, 450.0, 19.6))))).as("application/pdf")
  }

  def twice = Action {
    Ok(invoice.render("Facture de novembre", "VT0777", ClientDefinition("VIDAL", "37 rue des mathurins", "92330", "Issy-Les-Moulineaux"), List(InvoiceLine("desc", 25.0, 450.0, 19.6))))
  }

  def createInvoice = Action(parse.json) {request => {
    	println(request.body.validate(invoiceReads))
    	 Ok("lol")
  	}
  }
}