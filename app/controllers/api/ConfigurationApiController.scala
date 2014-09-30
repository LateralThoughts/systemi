package controllers.api

import domain.RatioConfigurationSerializer
import play.api.libs.json.{JsError, JsSuccess, JsObject, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}

class ConfigurationApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with RatioConfigurationSerializer
  with securesocial.core.SecureSocial[BasicProfile] {

  def getCurrentConfiguration = SecuredAction.async {
    db
      .collection[JSONCollection]("configuration")
      .find(Json.obj())
      .cursor[JsObject]
      .collect[List]()
      .map(configs => Ok(Json.toJson(configs)))
  }

  def agoraCallback = Action(parse.json) { implicit request =>
    request.body.validate(ratioFormatter) match {
      case JsSuccess(config) => Ok
      case JsError(errors) => BadRequest(Json.toJson(errors))
    }
  }
}
