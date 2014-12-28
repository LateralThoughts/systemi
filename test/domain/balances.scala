package domain

import org.joda.time.DateTime
import org.scalatest.{Matchers, FunSuite}
import reactivemongo.bson.BSONObjectID

class InvoiceTest extends FunSuite with Matchers {

  test("Invoice is a late payment if status is created and payment is delayed") {

    // Given
    val id = BSONObjectID.generate
    val client = ClientRequest("Client","3 rue test","75000","Paris","France")
    val lastStatus: Status = Status("created",DateTime.now().minusDays(60), "jean.zay@example.com")
    val invoiceRequest = InvoiceRequest("Invoice","VT204",30,true, client, Nil)
    val invoice = Invoice(id, invoiceRequest, Attachment("pdf", true, Array()), List(lastStatus), lastStatus, "created")

    // When
    val result = invoice.isDelayed

    // Then
    result should be (true)
  }

  test("Invoice is a late payment if status is allocated and payment is delayed") {

    // Given
    val id = BSONObjectID.generate
    val client = ClientRequest("Client","3 rue test","75000","Paris","France")
    val lastStatus: Status = Status("allocated",DateTime.now().minusDays(60), "jean.zay@example.com")
    val invoiceRequest = InvoiceRequest("Invoice","VT204",30,true, client, Nil)
    val invoice = Invoice(id, invoiceRequest, Attachment("pdf", true, Array()), List(lastStatus), lastStatus, "allocated")

    // When
    val result = invoice.isDelayed

    // Then
    result should be (true)
  }

  test("Invoice is not a late payment if status is created but payment is not delayed yet") {

    // Given
    val id = BSONObjectID.generate
    val client = ClientRequest("Client","3 rue test","75000","Paris","France")
    val lastStatus: Status = Status("created",DateTime.now(), "jean.zay@example.com")
    val invoiceRequest = InvoiceRequest("Invoice","VT204",30,true, client, Nil)
    val invoice = Invoice(id, invoiceRequest, Attachment("pdf", true, Array()), List(lastStatus), lastStatus, "created")

    // When
    val result = invoice.isDelayed

    // Then
    result should be (false)
  }

  test("Invoice is not a late payment if status is created and it is the last day of payment") {

    // Given
    val id = BSONObjectID.generate
    val client = ClientRequest("Client","3 rue test","75000","Paris","France")
    val lastStatus: Status = Status("created",DateTime.now().minusDays(30), "jean.zay@example.com")
    val invoiceRequest = InvoiceRequest("Invoice","VT204",30,true, client, Nil)
    val invoice = Invoice(id, invoiceRequest, Attachment("pdf", true, Array()), List(lastStatus), lastStatus, "created")

    // When
    val result = invoice.isDelayed

    // Then
    result should be (false)
  }

  test("Invoice is not a late payment if status is payed") {

    // Given
    val id = BSONObjectID.generate
    val client = ClientRequest("Client","3 rue test","75000","Paris","France")
    val lastStatus: Status = Status("payed",DateTime.now(), "jean.zay@example.com")
    val invoiceRequest = InvoiceRequest("Invoice","VT204",10,true, client, Nil)
    val invoice = Invoice(id, invoiceRequest, Attachment("pdf", true, Array()), List(lastStatus), lastStatus, "payed")

    // When
    val result = invoice.isDelayed

    // Then
    result should be (false)
  }

  test("Invoice is not a late payment if status is canceled") {

    // Given
    val id = BSONObjectID.generate
    val client = ClientRequest("Client","3 rue test","75000","Paris","France")
    val lastStatus: Status = Status("canceled",DateTime.now(), "jean.zay@example.com")
    val invoiceRequest = InvoiceRequest("Invoice","VT204",10,true, client, Nil)
    val invoice = Invoice(id, invoiceRequest, Attachment("pdf", true, Array()), List(lastStatus), lastStatus, "canceled")

    // When
    val result = invoice.isDelayed

    // Then
    result should be (false)
  }

  test("Invoice payment delay is the difference between today and last possible payment date ") {

    // Given
    val id = BSONObjectID.generate
    val client = ClientRequest("Client","3 rue test","75000","Paris","France")
    val lastStatus: Status = Status("created",DateTime.now().minusDays(60), "jean.zay@example.com")
    val invoiceRequest = InvoiceRequest("Invoice","VT204",30,true, client, Nil)
    val invoice = Invoice(id, invoiceRequest, Attachment("pdf", true, Array()), List(lastStatus), lastStatus, "created")

    // When
    val result = invoice.retrievePaymentDelayInDays

    // Then
    result should be (30)
  }

}
