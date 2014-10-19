package controllers

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain.User
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

class AccountsController (override implicit val env: Environment[User, SessionAuthenticator]) extends Silhouette[User, SessionAuthenticator]
                            with MongoController
                            {

  def collection = db.collection[JSONCollection]("accounts")

}
