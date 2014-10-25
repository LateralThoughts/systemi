package securesocial.testkit

import play.api.mvc.Request
import securesocial.core.AuthenticationResult.Authenticated
import securesocial.core._

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AlwaysValidIdentityProvider extends IdentityProvider {
  def authMethod: AuthenticationMethod = AuthenticationMethod("test")

  override def authenticate()(implicit request: Request[play.api.mvc.AnyContent]): Future[AuthenticationResult] = {
    Future(Authenticated(BasicProfile("google", id, Some("jean"), Some("test"), Some("jean test"), Some("jean.test@lateral-thoughts.com"), None, authMethod, None, None, None)))
  }

  val id: String = "test"

}

object AlwaysValidIdentityProvider {

  abstract class RuntimeEnvironment[U] extends RuntimeEnvironment.Default[U] {
    override lazy val providers: ListMap[String, IdentityProvider] = ListMap(include(new AlwaysValidIdentityProvider))
  }

}