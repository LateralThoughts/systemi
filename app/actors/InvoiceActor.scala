package actors

import akka.actor.Actor
import domain.{Invoice, InvoiceSerializer}
import play.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

case class InvoiceActor() extends Actor with InvoiceSerializer {
  import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

  def receive = {

    case invoice: Invoice =>
      val db = ReactiveMongoPlugin.db
      db.
        collection[JSONCollection]("invoices")
        .save(Json.toJson(invoice))
      Logger.info(s"Saved invoice $invoice")

      db.
        collection[JSONCollection]("invoices_tasks")
        .save(Json.toJson(invoice))
      Logger.info(s"Created pending invoice $invoice")
  }


}
