import actors.InvoiceActor
import akka.actor.Props
import play.api.Play.current
import play.api._
import play.api.libs.concurrent._
import com.softwaremill.macwire.MacwireMacros._


object Global extends GlobalSettings {
  val logger = Logger("Global")
  val wired = wiredInModule(new ProductionModule)

  override def onStart(app: Application) = {
    super.onStart(app)
    Akka.system.actorOf(Props[InvoiceActor], name="invoice")
  }

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    wired.lookupSingleOrThrow(controllerClass)
  }
}
