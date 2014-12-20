package domain

import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.{JsObject, JsString}

class InvoiceSearchRequestTest extends FunSuite with Matchers {

  test("Should generate empty search request when having empty invoice search request") {
    // Given
    val invoiceSearchRequest = new InvoiceSearchRequest()

    // When
    val result: JsObject = invoiceSearchRequest.transformToSearchRequest()

    // Then
    result.values should have size 0
  }

  test("Should generate complete search request when having complete invoice search request") {
    // Given
    val invoiceSearchRequest = new InvoiceSearchRequest(Some("aFirmName"), Some("jean.zay@example.com"), Some("affected"), Some("false"), Some("paid"))

    // When
    val result: JsObject = invoiceSearchRequest.transformToSearchRequest()

    // Then
    result.values should have size 5
    result.fields should contain ("invoice.client.name" -> new JsString("aFirmName"))
    result.fields should contain ("statuses.0.email" -> new JsString("jean.zay@example.com"))
    result.fields should contain ("affectationStatus" -> new JsString("affected"))
    result.fields should contain ("canceled" -> new JsString("false"))
    result.fields should contain ("paymentStatus" -> new JsString("paid"))
  }

  test("Should generate incomplete search request when having incomplete invoice search request") {
    // Given
    val invoiceSearchRequest = new InvoiceSearchRequest(Some("aFirmName"), None, Some("affected"), None, Some("paid"))

    // When
    val result: JsObject = invoiceSearchRequest.transformToSearchRequest()

    // Then
    result.values should have size 3
    result.fields should contain ("invoice.client.name" -> new JsString("aFirmName"))
    result.fields should contain ("affectationStatus" -> new JsString("affected"))
    result.fields should contain ("paymentStatus" -> new JsString("paid"))
  }

}
