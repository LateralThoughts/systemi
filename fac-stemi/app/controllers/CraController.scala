package controllers

import _root_.util.pdf.GoogleDriveInteraction
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.mvc._
import play.api.libs.json._
import domain._
import scala.concurrent.ExecutionContext


object CraController extends Controller
                     with ActivitySerializer
                     with MongoController
                     with GoogleDriveInteraction {
  import ExecutionContext.Implicits.global

  def collection = db.collection[JSONCollection]("cras")


  def add = Action(parse.json) { implicit request =>
    val json = request.body
    json.validate(activityReads) match {
      case errors:JsError => Ok(errors.toString).as("application/json")
      case result: JsResult[Activity] => {
        val activity = result.get
        collection.insert(activity)
        // and generate corresponding invoice :
        val invoiceRequest = activity.toInvoice
        val generatedPdfDocument = invoiceToPdfBytes(invoiceRequest)
        pushToGoogleDrive(invoiceRequest, generatedPdfDocument)
        Ok
      }
    }
  }
}
