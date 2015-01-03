import org.junit.runner._
import org.specs2.runner._
import play.api.libs.json._
import play.api.test._
import reactivemongo.bson.BSONObjectID

/**
 * Full application spec to execute page rendering and check consistency
 */
@RunWith(classOf[JUnitRunner])
class InvoiceEndpointSpecs extends SystemiSpecification {

  "Application on Invoice Endpoint" should {

    // TODO uncomment test when figured out a way to manage https://github.com/ReactiveMongo/Play-ReactiveMongo/issues/32 (http://stackoverflow.com/questions/18778837/play2-reactivemongo-testing-issue-db-connection-right-after-test-fails)
//    "retrieve invoices" in new WithApplication(app) {
//      val creds1 = retrieveIdentificationCookie
//
//      val invoices = route(FakeRequest(GET, "/api/invoices").withCookies(creds1.get("invoice@lt").get)).get
//
//      status(invoices) must equalTo(OK)
//      contentType(invoices) must beSome.which(_ == "application/json")
//    }

    "generate pdf invoice" in new WithApplication(app) {
      val creds1 = retrieveIdentificationCookie

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
      BSONObjectID.parse(contentAsString(invoiceGenerationPage)).isSuccess shouldEqual true
    }
  }

}
