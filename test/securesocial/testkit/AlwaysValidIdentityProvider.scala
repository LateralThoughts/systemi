package securesocial.testkit

import play.api.Plugin
import securesocial.core._
import play.api.mvc.Request
import scala.concurrent.Future
import securesocial.core.AuthenticationResult.Authenticated
import scala.concurrent.ExecutionContext.Implicits.global

class AlwaysValidIdentityProvider(app:play.api.Application) extends IdentityProvider with Plugin {
  def authMethod: AuthenticationMethod = AuthenticationMethod("naive")


  override def authenticate()(implicit request: Request[play.api.mvc.AnyContent]): Future[AuthenticationResult] ={
    Future(Authenticated(BasicProfile("google", id, Some("jean"), Some("test"), Some("jean test"),Some("jean.test@example.com"),None, authMethod,None, None, None)))
  }

  val id: String = "naive"
}