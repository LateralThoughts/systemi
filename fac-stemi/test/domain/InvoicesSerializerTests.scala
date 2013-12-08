package domain

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import play.api.libs.json._

class InvoiceRequestSerializerTest extends FunSuite with ShouldMatchers with InvoiceSerializer {

	test("should deserialize properly") {
		val data = """{
			"client" : {
				"name" : "VIDAL",
				"address" : "27 rue camille desmoulins"
			},
			"invoice" : [{
				"days" : 25.0,
				"dailyRate" : 450,
		                "taxRate": 19.6
			}]}"""
		val invoice = InvoiceRequest(ClientDefinition("VIDAL", "27 rue camille desmoulins"), List(InvoiceLine(25.0, 450.0)))
		Json.parse(data).validate(invoiceReads).get should be (invoice)
	}
}