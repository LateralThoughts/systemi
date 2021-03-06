package repository

import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.{JsObject, JsUndefined}

class InvoiceRequestBuilderTest extends FunSuite with Matchers {

  val invoiceRequestBuilder = new InvoiceRequestBuilder

  ///////////////////////////////////////////////////
  //
  // check method updateStatusUpdateFieldRequest
  //
  ///////////////////////////////////////////////////

  test("Should return None if status doesn't exist") {
    // Given
    val status = "Inexistant status"

    // When
    val request: Option[JsObject] = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    request should be (None)
  }

  test("Should return created for status update if status is created") {
    // Given
    val status = "created"

    // When
    val request: Option[JsObject] = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request.get \ "$set" \ "status").as[String] should be ("created")
  }

  test("Should return allocated for status update if status is unpaid") {
    // Given
    val status = "unpaid"

    // When
    val request: Option[JsObject] = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request.get \ "$set" \ "status").as[String] should be ("allocated")
  }

  test("Should return allocated for status update if status is allocated") {
    // Given
    val status = "allocated"

    // When
    val request: Option[JsObject] = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request.get \ "$set" \ "status").as[String] should be ("allocated")
  }

  test("Should return paid for status update if status is paid") {
    // Given
    val status = "paid"

    // When
    val request: Option[JsObject] = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request.get \ "$set" \ "status").as[String] should be ("paid")
  }

  test("Should return None if status is canceled") {
    // Given
    val status = "canceled"

    // When
    val request: Option[JsObject] = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    request should be (None)
  }

  test("Should not have status update if status is reallocated") {
    // Given
    val status = "reallocated"

    // When
    val request: Option[JsObject] = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request.get \ "$set" \ "status") shouldBe a [JsUndefined]
  }

  ///////////////////////////////////////////////////
  //
  // check method cancelInvoiceUpdateFieldRequest
  //
  ///////////////////////////////////////////////////

  test("Should return cancel update fields") {
    // Given
    val email = "fake.email@example.com"
    val pdfData = Array[Byte]()

    // When
    val request: JsObject = invoiceRequestBuilder.cancelInvoiceUpdateFieldRequest(pdfData, email)

    // Then
    (request \ "$set" \ "status").as[String] should be ("canceled")
    (request \ "$set" \ "pdfDocument" \ "contentType").as[String] should be ("application/pdf")
  }

}
