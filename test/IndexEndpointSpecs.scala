import org.junit.runner._
import org.specs2.runner._
import play.api.mvc.Cookies
import play.api.test._

/**
 * Full application spec to execute page rendering and check consistency
 */
@RunWith(classOf[JUnitRunner])
class IndexEndpointSpecs extends SystemiSpecification {

  "Application on Index Endpoint" should {

    "send 404 on a bad request" in new WithApplication(app) {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the home page" in new WithApplication(app) {
      val creds1: Cookies = retrieveIdentificationCookie

      val home = route(FakeRequest(GET, "/").withCookies(creds1.get("invoice@lt").get)).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain("System")
    }
  }
}
