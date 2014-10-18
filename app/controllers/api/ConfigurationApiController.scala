package controllers.api

import auth.WithDomain
import domain.{RatioConfiguration, RatioConfigurationSerializer}
import play.api.libs.json.{JsError, JsSuccess, JsObject, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.Future

class ConfigurationApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with RatioConfigurationSerializer
  with securesocial.core.SecureSocial[BasicProfile] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getCurrentConfiguration = SecuredAction(WithDomain()).async {
    db
      .collection[JSONCollection]("configuration")
      .find(Json.obj())
      .cursor[JsObject]
      .collect[List]()
      .map(configs => Ok(Json.toJson(configs)))
  }

  def agoraCallback = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
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
