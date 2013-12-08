package controllers

import play.api._
import play.api.mvc._
import domain._
import play.api.libs.json._

object Application extends Controller with InvoiceSerializer {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def createInvoice = Action(parse.json) {request => {
  	println(request.body.validate(invoiceReads))
	  		Ok("lol")
	}
  }
}