package domain

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import play.api.libs.json._

class InvoiceRequestSerializerTest extends FunSuite
                                   with ShouldMatchers
                                   with InvoiceSerializer {

	test("should deserialize invoices properly from json") {
		val data = """{
			"title": "facture",
			"invoiceNumber":"VT055",
      "paymentDelay": 25,
			"client" : {
        "id" : "0",
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

		val invoice = InvoiceRequest("facture",
                                 "VT055",
                                 25,
                                 ClientDefinition("0", "VIDAL", "27 rue camille desmoulins", "94550", "chevilly"),
                                 List(InvoiceLine("blabla", 25.0, 450.0, 19.6)))
		Json.parse(data).validate(invoiceReads).get should be (invoice)
	}


  test("should deserialize invoices properly from multipart form data") {
    val data = Map(
      "title" -> Seq("facture"),
			"invoiceNumber" -> Seq("VT055"),
      "paymentDelay" -> Seq("25"),
      "clientId" -> Seq("1"),
			"clientName" -> Seq("VIDAL"),
			"clientAddress" -> Seq("27 rue camille desmoulins"),
			"clientPostalCode" -> Seq("94550"),
			"clientCity" -> Seq("chevilly"),
			"invoiceDescription" -> Seq("blabla", "blabla2"),
			"invoiceDays" -> Seq("25.0", "24.0"),
			"invoiceDailyRate" -> Seq("450", "451"),
      "invoiceTaxRate" -> Seq("19.6", "20.6")
    )

    val invoice = InvoiceRequest("facture",
      "VT055",
      25,
      ClientDefinition("1", "VIDAL", "27 rue camille desmoulins", "94550", "chevilly"),
      List(
        InvoiceLine("blabla", 25.0, 450.0, 19.6),
        InvoiceLine("blabla2", 24.0, 451.0, 20.6)
      )
    )
    invoiceFromForm(data) should be (invoice)
  }

}