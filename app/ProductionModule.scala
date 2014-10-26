import com.softwaremill.macwire.MacwireMacros._
import controllers._
import controllers.api._
import play.api.data.Form
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import search.SimpleSearchEngine
import securesocial.controllers._
import securesocial.core._
import securesocial.core.providers._
import service.MongoBasedUserService

import scala.collection.immutable.ListMap


class ProductionModule {

  object ProductionRuntimeEnvironment extends RuntimeEnvironment.Default[BasicProfile] {
    override lazy val userService = new MongoBasedUserService
    //override lazy val eventListeners = List(new MyEventListener())
    override lazy val providers = ListMap(
      // oauth 2 client providers
      include(new GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google)))
    )
    override lazy val viewTemplates = new ViewTemplates.Default(this){
      override def getLoginPage(form: Form[(String, String)], msg: Option[String])(implicit request: RequestHeader, lang: Lang)= {
        views.html.login(form, msg)(request, lang, env, request.flash)
      }

      override def getNotAuthorizedPage(implicit request : RequestHeader, lang : Lang) = {
        views.html.unauthorized()(request, lang, env, request.flash)
      }
    }
  }

  implicit val env = ProductionRuntimeEnvironment

  // secure-social modules
  lazy val loginPage = wire[LoginPage]
  lazy val providerController = wire[ProviderController]

  // application modules
  lazy val app = wire[Application]
  lazy val clientController = wire[ClientController]
  lazy val invoiceController = wire[InvoiceController]
  lazy val expenseController = wire[ExpenseController]
  lazy val expenseApiController = wire[ExpenseApiController]
  lazy val contributionsController = wire[ContributionsController]
  lazy val configController = wire[ConfigurationController]
  lazy val movementsController = wire[MovementsController]
  lazy val movementsApiController = wire[MovementsApiController]
  lazy val membersController = wire[MembersController]
  lazy val accountsController = wire[AccountsController]
  lazy val searchEngine = wire[SimpleSearchEngine]
  lazy val invoiceApiController = wire[InvoiceApiController]
  lazy val clientApiController = wire[ClientApiController]
  lazy val activityController = wire[ActivityController]
  lazy val activityApiController = wire[ActivityApiController]
  lazy val accountApiController = wire[AccountApiController]
  lazy val configurationApiController = wire[ConfigurationApiController]
  lazy val membersApiController = wire[MembersApiController]
  lazy val contributionsApiController = wire[ContributionsApiController]
}
