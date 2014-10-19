import com.mohiva.play.silhouette.api.services.{AuthenticatorService, IdentityService}
import com.mohiva.play.silhouette.api.util.{Clock, FingerprintGenerator, IDGenerator, PlayHTTPLayer}
import com.mohiva.play.silhouette.api.{Environment, EventBus, Provider}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{CookieStateProvider, CookieStateSettings}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Settings, OAuth2StateProvider}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.softwaremill.macwire.MacwireMacros._
import controllers._
import controllers.api._
import domain.User
import play.api.Play
import play.api.Play.current
import search.SimpleSearchEngine
import service.MongoBasedUserService


class ProductionModule {

  object ProductionRuntimeEnvironment extends Environment[User, SessionAuthenticator] {
    override def identityService: IdentityService[User] = new MongoBasedUserService

    override def authenticatorService: AuthenticatorService[SessionAuthenticator] = {
      provideAuthenticatorService(new DefaultFingerprintGenerator(false))
    }

    def provideAuthenticatorService(
                                     fingerprintGenerator: FingerprintGenerator): SessionAuthenticatorService = {
      new SessionAuthenticatorService(SessionAuthenticatorSettings(
        sessionKey = Play.configuration.getString("silhouette.authenticator.sessionKey").get,
        encryptAuthenticator = Play.configuration.getBoolean("silhouette.authenticator.encryptAuthenticator").get,
        useFingerprinting = Play.configuration.getBoolean("silhouette.authenticator.useFingerprinting").get,
        authenticatorIdleTimeout = Play.configuration.getInt("silhouette.authenticator.authenticatorIdleTimeout"),
        authenticatorExpiry = Play.configuration.getInt("silhouette.authenticator.authenticatorExpiry").get
      ), fingerprintGenerator, Clock())
    }

    override def providers: Map[String, Provider] = {
      val googleProvider =  GoogleProvider(new PlayHTTPLayer(), provideOAuth2StateProvider(new SecureRandomIDGenerator()), OAuth2Settings(
        authorizationURL = Play.configuration.getString("silhouette.google.authorizationURL").get,
        accessTokenURL = Play.configuration.getString("silhouette.google.accessTokenURL").get,
        redirectURL = Play.configuration.getString("silhouette.google.redirectURL").get,
        clientID = Play.configuration.getString("silhouette.google.clientID").get,
        clientSecret = Play.configuration.getString("silhouette.google.clientSecret").get,
        scope = Play.configuration.getString("silhouette.google.scope")))
      Map(googleProvider.id -> googleProvider)
    }

    def provideOAuth2StateProvider(idGenerator: IDGenerator): OAuth2StateProvider = {
      new CookieStateProvider(CookieStateSettings(
        cookieName = Play.configuration.getString("silhouette.oauth2StateProvider.cookieName").get,
        cookiePath = Play.configuration.getString("silhouette.oauth2StateProvider.cookiePath").get,
        cookieDomain = Play.configuration.getString("silhouette.oauth2StateProvider.cookieDomain"),
        secureCookie = Play.configuration.getBoolean("silhouette.oauth2StateProvider.secureCookie").get,
        httpOnlyCookie = Play.configuration.getBoolean("silhouette.oauth2StateProvider.httpOnlyCookie").get,
        expirationTime = Play.configuration.getInt("silhouette.oauth2StateProvider.expirationTime").get
      ), idGenerator, Clock())
    }

    override def eventBus: EventBus = EventBus()
  }

  implicit val env = ProductionRuntimeEnvironment

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
  lazy val authenticationController = wire[AuthenticationController]
  lazy val authenticationApiController = wire[AuthenticationApiController]
}
