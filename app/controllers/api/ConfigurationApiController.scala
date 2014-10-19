package controllers.api

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain.{RatioConfiguration, RatioConfigurationSerializer, User}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future

class ConfigurationApiController(override implicit val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator]
  with MongoController
  with RatioConfigurationSerializer
   {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getCurrentConfiguration = SecuredAction.async {
    db
      .collection[JSONCollection]("configuration")
      .find(Json.obj())
      .cursor[JsObject]
      .collect[List]()
      .map(configs => Ok(Json.toJson(configs)))
  }

  def agoraCallback = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate(ratioConfigFormatter) match {
      case obj: JsSuccess[RatioConfiguration] =>
        db
          .collection[JSONCollection]("configuration")
          .save(Json.toJson(obj.get))
          .map(errors =>
          if (errors.inError) InternalServerError
          else Ok)

      case errors: JsError => Future(BadRequest(errors.toString))
    }
  }
}
