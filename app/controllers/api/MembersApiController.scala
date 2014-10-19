package controllers.api

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain.User
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

class MembersApiController(override implicit val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator]
  with MongoController
   {

  import scala.concurrent.ExecutionContext.Implicits.global

  def findAll = SecuredAction.async {
    db
      .collection[JSONCollection]("users")
      .find(Json.obj())
      .cursor[JsObject]
      .collect[List]()
      .map(users => Ok(Json.toJson(users)))
  }
}
