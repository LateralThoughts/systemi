import com.typesafe.config.ConfigFactory
import org.junit.runner._
import org.specs2.runner._
import play.api.test._
import play.api.libs.json._
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

      val invoice = """{
			"title": "facture",
			"invoiceNumber":"VT055",
      "paymentDelay": 25,
      "withTaxes": true,
			"client" : {
        "_id" : {
          "$oid": "532afca061ce6a2db986839f"
        },
				"name" : "VIDAL",
				"address" : "27 rue camille desmoulins",
				"postalCode" : "94550",
				"city": "chevilly",
				"country": "France"
			},
			"invoice" : [{
				"description" : "blabla",
				"days" : 25.0,
				"dailyRate" : 450,
		        "taxRate": 19.6
			}]}"""

      val invoiceGenerationPage = route(FakeRequest(POST, "/api/invoice").withCookies(creds1.get("invoice@lt").get).withJsonBody(Json.parse(invoice))).get

      status(invoiceGenerationPage) must equalTo(OK)
      ConsoleLogger.info(contentAsString(invoiceGenerationPage))
    }
  }

}
