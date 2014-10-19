package controllers

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain.User

class ConfigurationController(override implicit val env: Environment[User, SessionAuthenticator]) extends Silhouette[User, SessionAuthenticator]
                                       {

  def index = SecuredAction {
    implicit request =>
      Ok(views.html.configurations.index(request.identity))
  }

}