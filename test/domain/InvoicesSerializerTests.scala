package domain

import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json._

class InvoiceRequestSerializerTest extends FunSuite
                                   with Matchers
                                   with InvoiceSerializer {

	test("should deserialize invoices properly from json") {
		val data = """{
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

    val withTaxes = true
		val invoice = InvoiceRequest("facture",
                                 "VT055",
                                 25,
                                 withTaxes,
                                 ClientRequest("VIDAL", "27 rue camille desmoulins", "94550", "chevilly", "France"),
                                 List(InvoiceLine("blabla", 25.0, 450.0, 19.6)))
		Json.parse(data).validate(invoiceReqFormat).get should be (invoice)
	}
}