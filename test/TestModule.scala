import com.softwaremill.macwire.MacwireMacros._
import controllers._
import controllers.api._
import search.SimpleSearchEngine
import securesocial.controllers._
import securesocial.core._
import securesocial.core.services.UserService
import securesocial.testkit.AlwaysValidIdentityProvider
import service.MongoBasedUserService


class TestModule {

  implicit val env = new AlwaysValidIdentityProvider.RuntimeEnvironment[BasicProfile] {
    override val userService: UserService[BasicProfile] = new MongoBasedUserService()
  }

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
