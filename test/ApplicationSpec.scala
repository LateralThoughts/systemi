import org.specs2.runner._
import org.junit.runner._

import java.lang.reflect.Constructor

import service.MongoBasedUserService
import play.api.test._
import reactivemongo.bson.BSONObjectID
import play.api.GlobalSettings
import securesocial.core.services.UserService
import securesocial.core.{RuntimeEnvironment, BasicProfile}
import securesocial.testkit.AlwaysValidIdentityProvider

/**
 * Full application spec to execute page rendering and check consistency
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends PlaySpecification {

  def app = FakeApplication(withGlobal = Some(global(env)), withoutPlugins = Seq("ehcacheplugin"))

  "Application" should {

    "send 404 on a bad request" in new WithApplication(app) {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the invoice form page" in new WithApplication(app) {
      val creds1 = cookies(route(FakeRequest(POST, "/auth/authenticate/test").withTextBody("user")).get)

      val home = route(FakeRequest(GET, "/").withCookies(creds1.get("invoice@lt").get)).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain("System")
    }

    "should generate pdf invoice" in new WithApplication(app) {
      val creds1 = cookies(route(FakeRequest(POST, "/auth/authenticate/test").withTextBody("user")).get)

      val invoiceGenerationPage = route(FakeRequest(POST, "/api/invoice").withCookies(creds1.get("invoice@lt").get).withFormUrlEncodedBody(
        ("title", "faux titre"),
        ("invoiceNumber", "VT500"),
        ("paymentDelay", "50"),
        ("clientId", BSONObjectID.generate.stringify),
        ("clientName", "TestClient"),
        ("clientAddress", "35 rue inconnue"),
        ("clientCity", "Issy les moulineaux"),
        ("clientCountry", "Zimbabwe"),
        ("clientPostalCode", "94550"),
        ("invoiceDescription", "développement"),
        ("invoiceDays", "125"),
        ("invoiceDailyRate", "500"),
        ("invoiceTaxRate", "20.0")
      )).get

      status(invoiceGenerationPage) must equalTo(OK)
      contentType(invoiceGenerationPage) must beSome.which(_ == "application/pdf")
    }
  }

  val env = new AlwaysValidIdentityProvider.RuntimeEnvironment[BasicProfile] {
    override val userService: UserService[BasicProfile] = new MongoBasedUserService()
  }

  private def global[A](env: RuntimeEnvironment[A]): GlobalSettings =
    new play.api.GlobalSettings {
      override def getControllerInstance[A](controllerClass: Class[A]): A = {
        val instance = controllerClass.getConstructors.find { c =>
          val params = c.getParameterTypes
          params.length == 1 && params(0) == classOf[RuntimeEnvironment[A]]
        }.map {
          _.asInstanceOf[Constructor[A]].newInstance(env)
        }
        instance.getOrElse(super.getControllerInstance(controllerClass))
      }
    }
}
