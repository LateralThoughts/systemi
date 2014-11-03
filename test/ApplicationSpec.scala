import com.typesafe.config.ConfigFactory
import org.junit.runner._
import org.specs2.runner._
import play.api.test._
import reactivemongo.bson.BSONObjectID

/**
 * Full application spec to execute page rendering and check consistency
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends PlaySpecification {

  def app = FakeApplication(
    additionalConfiguration = Map("mongodb.db" -> "test-invoice@LT"),
    withGlobal = Some(TestGlobal),
    withoutPlugins = Seq("ehcacheplugin")
  )

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

}
