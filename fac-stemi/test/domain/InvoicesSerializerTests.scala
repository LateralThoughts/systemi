package domain

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import play.api.libs.json._

class InvoiceRequestSerializerTest extends FunSuite with ShouldMatchers with InvoiceSerializer {

	test("should deserialize invoices properly") {
		val data = """{
			"title": "facture",
			"invoiceNumber":"VT055",
			"client" : {
				"name" : "VIDAL",
				"address" : "27 rue camille desmoulins",
				"postalCode" : "94550",
				"city": "chevilly"
			},
			"invoice" : [{
				"description" : "blabla",
				"days" : 25.0,
				"dailyRate" : 450,
		        "taxRate": 19.6
			}]}"""
		val invoice = InvoiceRequest("facture", "VT055", 
			ClientDefinition("VIDAL", "27 rue camille desmoulins", "94550", "chevilly"), 
			List(InvoiceLine("blabla", 25.0, 450.0, 19.6)))
		Json.parse(data).validate(invoiceReads).get should be (invoice)
	}
}