import actors.{ActivityActor, InvoiceActor}
import akka.actor.Props
import com.mohiva.play.silhouette.api.SecuredSettings
import com.softwaremill.macwire.MacwireMacros._
import controllers.api.ClientApiController
import controllers.routes
import play.Logger
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.{Application, GlobalSettings}
import play.libs.Akka
import search.SimpleSearchEngine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Global extends GlobalSettings with SecuredSettings {
  val wired = wiredInModule(new ProductionModule)

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

  override def onNotAuthenticated(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.AuthenticationController.signIn)))
  }
  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.AuthenticationController.signIn).flashing("error" -> Messages("access.denied"))))
  }
}
