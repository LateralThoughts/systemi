package repository

import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.{JsObject, JsUndefined, JsValue, Json}

class InvoiceRequestBuilderTest extends FunSuite with Matchers {

  val invoiceRequestBuilder = new InvoiceRequestBuilder

  test("Should return empty Json object if status doesn't exist") {
    // Given
    val status = "Inexistant status"

    // When
    val request: JsObject = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    request should be (Json.obj())
  }

  test("Should return created for status update if status is created") {
    // Given
    val status = "created"

    // When
    val request: JsObject = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request \ "$set" \ "status").as[String] should be ("created")
  }

  test("Should return allocated for status update if status is unpaid") {
    // Given
    val status = "unpaid"

    // When
    val request: JsObject = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request \ "$set" \ "status").as[String] should be ("allocated")
  }

  test("Should return allocated for status update if status is allocated") {
    // Given
    val status = "allocated"

    // When
    val request: JsObject = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request \ "$set" \ "status").as[String] should be ("allocated")
  }

  test("Should return paid for status update if status is paid") {
    // Given
    val status = "paid"

    // When
    val request: JsObject = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request \ "$set" \ "status").as[String] should be ("paid")
  }

  test("Should return canceled for status update if status is canceled") {
    // Given
    val status = "canceled"

    // When
    val request: JsObject = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    (request \ "$set" \ "status").as[String] should be ("canceled")
  }

  test("Should not have status update if status is reallocated") {
    // Given
    val status = "reallocated"

    // When
    val request: JsObject = invoiceRequestBuilder.updateStatusUpdateFieldRequest(status, "fake.email@example.com")

    // Then
    val value: JsValue = request \ "$set" \ "status"
    value shouldBe a [JsUndefined]
  }

}
