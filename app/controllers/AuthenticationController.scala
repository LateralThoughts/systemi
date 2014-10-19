package controllers

import com.mohiva.play.silhouette.api.{LogoutEvent, Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain.User

import scala.concurrent.Future

class AuthenticationController(override implicit val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator] {

  def signOut = SecuredAction.async { implicit request =>
    val result = Future.successful(Redirect(routes.Application.index))
    env.eventBus.publish(LogoutEvent(request.identity, request, request2lang))
    env.authenticatorService.discard(request.authenticator, result)
  }

  def signIn() = play.mvc.Results.TODO

  def signUp() = play.mvc.Results.TODO

  def authenticate(provider: String) = play.mvc.Results.TODO

  def credentials() = play.mvc.Results.TODO
}
