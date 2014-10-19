package controllers

import com.mohiva.play.silhouette.api.exceptions.AuthenticationException
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent, Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, SocialProvider}
import com.mohiva.play.silhouette.impl.services.DelegableAuthInfoService
import domain.User
import play.api.mvc.Action
import service.MongoBasedUserService

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationController(override implicit val env: Environment[User, SessionAuthenticator], implicit val executionContext : ExecutionContext)
  extends Silhouette[User, SessionAuthenticator] {

  val userService = new MongoBasedUserService

  val authInfoService = new DelegableAuthInfoService

  def signOut = SecuredAction.async { implicit request =>
    val result = Future.successful(Redirect(routes.Application.index))
    env.eventBus.publish(LogoutEvent(request.identity, request, request2lang))
    env.authenticatorService.discard(request.authenticator, result)
  }

  def signIn() = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.Application.index))
      case None => Future.successful(Ok(views.html.login()))
    }
  }

  def signUp() = play.mvc.Results.TODO

  def authenticate(provider: String) =  Action.async { implicit request =>
    (env.providers.get(provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoService.save(profile.loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(user.loginInfo)
            result <- env.authenticatorService.init(authenticator, Future.successful(
              Redirect(routes.Application.index)
            ))
          } yield {
            env.eventBus.publish(LoginEvent(user, request, request2lang))
            result
          }
        }
      case _ => Future.failed(new AuthenticationException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recoverWith(exceptionHandler)
  }

  def credentials() = play.mvc.Results.TODO
}
