import controllers.{ApplicationT, ClientController}
import com.softwaremill.macwire.MacwireMacros._
import securesocial.controllers._
import service.InMemoryUserService
import securesocial.core._
import securesocial.core.providers._

import scala.collection.immutable.ListMap
import scala.util.{Failure, Success, Try}


class ProductionModule {
  /**
   * The runtime environment for this sample app.
   */
  object MyRuntimeEnvironment extends RuntimeEnvironment.Default[BasicProfile] {
    override lazy val userService = new InMemoryUserService
    //override lazy val eventListeners = List(new MyEventListener())
    override lazy val providers = ListMap(
      // oauth 2 client providers
      include(new GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google)))
    )

  }

  implicit val env = MyRuntimeEnvironment
  lazy val clientController = wire[ClientController]
  lazy val application = wire[ApplicationT]
  lazy val loginPage = wire[LoginPage]
  lazy val providerController = wire[ProviderController]
}
