import domain.{InvoiceLine, Client}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID

/**
 * Full application spec to execute page rendering and check consistency
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the invoice form page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Invoice")
    }

    "should generate pdf invoice" in new WithApplication{
      val invoiceGenerationPage = route(FakeRequest(POST, "/api/invoice").withFormUrlEncodedBody(
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
      //contentAsString(invoiceGenerationPage) must contain ("Invoice")
    }
  }
}
