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


  test("should deserialize invoices properly from multipart form data") {
    val data = Map(
      "title" -> Seq("facture"),
			"invoiceNumber" -> Seq("VT055"),
      "paymentDelay" -> Seq("25"),
      "clientId" -> Seq("532afca061ce6a2db986839f"),
			"clientName" -> Seq("VIDAL"),
			"clientAddress" -> Seq("27 rue camille desmoulins"),
			"clientPostalCode" -> Seq("94550"),
			"clientCity" -> Seq("chevilly"),
			"clientCountry" -> Seq("France"),
			"invoiceDescription" -> Seq("blabla", "blabla2"),
			"invoiceDays" -> Seq("25.0", "24.0"),
			"invoiceDailyRate" -> Seq("450", "451"),
      "invoiceTaxRate" -> Seq("19.6", "20.6")
    )

    val withTaxes = true
    val invoice = InvoiceRequest("facture",
      "VT055",
      25,
    withTaxes,
      ClientRequest("VIDAL", "27 rue camille desmoulins", "94550", "chevilly", "France"),
      List(
        InvoiceLine("blabla", 25.0, 450.0, 19.6),
        InvoiceLine("blabla2", 24.0, 451.0, 20.6)
      )
    )
    invoiceFromForm(data) should be (invoice)
  }

  test("should generate invoice request with taxes if there is sequence with head set to \"true\"") {
    // Given
    val data = createPostDataGivenTaxesStatus(Seq("true"))

    // When
    val result = invoiceFromForm(data)

    // Then
    assert(result.withTaxes)
  }

  test("should generate invoice request without taxes if there is sequence with head set to \"false\"") {
    // Given
    val data = createPostDataGivenTaxesStatus(Seq("false"))

    // When
    val result = invoiceFromForm(data)

    // Then
    assert(!result.withTaxes)
  }

  test("should generate invoice request with taxes if there is no paymentTaxesIncluded field") {
    // Given
    val data = Map(
      "title" -> Seq("facture"),
      "invoiceNumber" -> Seq("VT055"),
      "paymentDelay" -> Seq("25"),
      "clientId" -> Seq("532afca061ce6a2db986839f"),
      "clientName" -> Seq("VIDAL"),
      "clientAddress" -> Seq("27 rue camille desmoulins"),
      "clientPostalCode" -> Seq("94550"),
      "clientCity" -> Seq("chevilly"),
      "clientCountry" -> Seq("France"),
      "invoiceDescription" -> Seq("blabla", "blabla2"),
      "invoiceDays" -> Seq("25.0", "24.0"),
      "invoiceDailyRate" -> Seq("450", "451"),
      "invoiceTaxRate" -> Seq("19.6", "20.6")
    )

    // When
    val result = invoiceFromForm(data)

    // Then
    assert(result.withTaxes)
  }

  private def createPostDataGivenTaxesStatus(taxes: Seq[String]): Map[String, Seq[String]] = {
    Map(
      "title" -> Seq("facture"),
      "invoiceNumber" -> Seq("VT055"),
      "paymentDelay" -> Seq("25"),
      "paymentTaxesIncluded" -> taxes,
      "clientId" -> Seq("532afca061ce6a2db986839f"),
      "clientName" -> Seq("VIDAL"),
      "clientAddress" -> Seq("27 rue camille desmoulins"),
      "clientPostalCode" -> Seq("94550"),
      "clientCity" -> Seq("chevilly"),
      "clientCountry" -> Seq("France"),
      "invoiceDescription" -> Seq("blabla", "blabla2"),
      "invoiceDays" -> Seq("25.0", "24.0"),
      "invoiceDailyRate" -> Seq("450", "451"),
      "invoiceTaxRate" -> Seq("19.6", "20.6")
    )
  }
}