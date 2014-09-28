import com.softwaremill.macwire.MacwireMacros._
import controllers.{ClientController, CraController, InvoiceController}
import play.api.data.Form
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.api.templates.Html
import securesocial.controllers._
import securesocial.core._
import securesocial.core.providers._
import service.InMemoryUserService

import scala.collection.immutable.ListMap


class ProductionModule {

  object ProductionRuntimeEnvironment extends RuntimeEnvironment.Default[BasicProfile] {
    override lazy val userService = new InMemoryUserService
    //override lazy val eventListeners = List(new MyEventListener())
    override lazy val providers = ListMap(
      // oauth 2 client providers
      include(new GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google)))
    )
    override lazy val viewTemplates = new ViewTemplates.Default(this){
      override def getLoginPage(form: Form[(String, String)], msg: Option[String])(implicit request: RequestHeader, lang: Lang)= {
        views.html.login(form, msg)(request, lang, env)
      }
    }
  }

  implicit val env = ProductionRuntimeEnvironment

  // secure-social modules
  lazy val loginPage = wire[LoginPage]
  lazy val providerController = wire[ProviderController]

  // application modules
  lazy val clientController = wire[ClientController]
  lazy val craController = wire[CraController]
  lazy val invoiceController = wire[InvoiceController]
}
