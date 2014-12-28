package domain

import org.joda.time.DateTime
import org.scalatest.FunSuite
import org.scalatest.Matchers
import reactivemongo.bson.BSONObjectID


class InvoiceLinesAnalyzerTest extends FunSuite with Matchers with InvoiceLinesAnalyzer {

	test("should compute total properly") {
		val items = List(InvoiceLine("blabla", 25.0, 450.0))
		computeTotalHT(items) should be (25 * 450)
	}

	test("should compute total properly for multiple lines") {
		val items = List(InvoiceLine("blabla", 25.0, 450.0), InvoiceLine("blabla", 12.0, 350.0))
		computeTotalHT(items) should be (25 * 450 + 12*350)
	}

	test("should compute total properly for multiple lines with taxes") {
		val items = List(InvoiceLine("blabla", 25.0, 450.0), InvoiceLine("blabla", 12.0, 350.0))
		computeTotal(items) should be ((25.0 * 450.0 + 12.0 * 350) * (1.20))
	}

	test("should compute tva properly for multiple lines with rounding on two decimals") {
		val items = List(InvoiceLine("blabla", 25.0, 450.0), InvoiceLine("blabla", 12.0, 350.0))
		computeTva(items) should be (Map(
      ("20.0%" -> 3090.0)
    ))
	}


  test("should compute tva properly for multiple lines with different tvas rounding on two decimals") {
 		val items = List(InvoiceLine("blabla", 25.0, 450.0), InvoiceLine("blabla", 12.0, 350.0, 21.5))
 		computeTva(items) should be (Map(("20.0%" -> 2250.0), ("21.5%" -> 903.0)))
 	}
}

class NextInvoiceNumbersParserTest extends FunSuite with Matchers with NextInvoiceNumbersParser {

  test("should extract next invoice number") {
    extractInvoiceNumber("NEXT_VT123") should be (123, 124)
  }

}

class InvoiceDataTest extends FunSuite with Matchers {

	test("Invoice is a late payment if status is created and payment is delayed") {

		// Given
		val invoice = generateInvoiceData("created", 60)

		// When
		val result = invoice.isDelayed

		// Then
		result should be (true)
	}

	test("Invoice is a late payment if status is allocated and payment is delayed") {

		// Given
		val invoice = generateInvoiceData("allocated", 60)

		// When
		val result = invoice.isDelayed

		// Then
		result should be (true)
	}

	test("Invoice is not a late payment if status is created but payment is not delayed yet") {

		// Given
		val invoice = generateInvoiceData("created", 15)

		// When
		val result = invoice.isDelayed

		// Then
		result should be (false)
	}

	test("Invoice is not a late payment if status is created and it is the last day of payment") {

		// Given
		val invoice = generateInvoiceData("created", 30)

		// When
		val result = invoice.isDelayed

		// Then
		result should be (false)
	}

	test("Invoice is not a late payment if status is payed") {

		// Given
		val invoice = generateInvoiceData("payed", 60)

		// When
		val result = invoice.isDelayed

		// Then
		result should be (false)
	}

	test("Invoice is not a late payment if status is canceled") {

		// Given
		val invoice = generateInvoiceData("canceled", 60)

		// When
		val result = invoice.isDelayed

		// Then
		result should be (false)
	}

	test("Invoice payment delay is the difference between today and last possible payment date ") {

		// Given
		val invoice = generateInvoiceData("created", 60)

		// When
		val result = invoice.retrievePaymentDelayInDays

		// Then
		result should be (30)
	}

	private def generateInvoiceData(status: String,daysSinceInvoiceCreated: Int) = {
		val id = BSONObjectID.generate
		val client = ClientRequest("Client","3 rue test","75000","Paris","France")
		val lastStatus: Status = Status(status,DateTime.now().minusDays(daysSinceInvoiceCreated), "jean.zay@example.com")
		val invoiceRequest = InvoiceRequest("Invoice","VT204",30,true, client, Nil)
		InvoiceData(id, invoiceRequest, List(lastStatus), status)
	}

}