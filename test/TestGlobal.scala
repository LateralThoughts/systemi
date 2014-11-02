import actors.{ActivityActor, InvoiceActor}
import akka.actor.Props
import com.softwaremill.macwire.MacwireMacros._
import controllers.api.ClientApiController
import play.Logger
import play.api.{Application, GlobalSettings}
import play.libs.Akka
import search.SimpleSearchEngine

import scala.concurrent.ExecutionContext.Implicits.global

object TestGlobal extends GlobalSettings {
  val wired = wiredInModule(new TestModule)

  override def onStart(app: Application) = {
    super.onStart(app)
    Akka.system.actorOf(Props[InvoiceActor], name="invoice")
    Akka.system.actorOf(Props[ActivityActor], name="activity")
    initSearchEngine
  }

  private def initSearchEngine = {
    Logger.info("Initiating search engine...")
    val engine = wired.lookupSingleOrThrow(classOf[SimpleSearchEngine])
    val clients = wired.lookupSingleOrThrow(classOf[ClientApiController])

    clients.findAll().map(clients => {
      engine.initWithDocuments(clients)
      Logger.info(s"Initiated search engine with ${clients.size} Clients.")
    })
  }

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    wired.lookupSingleOrThrow(controllerClass)
  }
}
