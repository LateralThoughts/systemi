package domain

import org.scalatest.FunSuite
import org.scalatest.Matchers


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