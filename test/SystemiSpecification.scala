import play.api.mvc.Cookies
import play.api.test.{FakeApplication, FakeRequest, PlaySpecification}

trait SystemiSpecification extends PlaySpecification {

  def app = FakeApplication(
    additionalConfiguration = Map("mongodb.db" -> "test-invoice@LT"),
    withGlobal = Some(TestGlobal),
    withoutPlugins = Seq("ehcacheplugin")
  )

  def retrieveIdentificationCookie: Cookies = {
    cookies(route(FakeRequest(POST, "/auth/authenticate/test").withTextBody("user")).get)
  }

}
