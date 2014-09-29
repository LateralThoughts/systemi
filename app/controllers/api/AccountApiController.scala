package controllers.api

import domain.InvoiceSerializer
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}
import util.pdf.GoogleDriveInteraction

class AccountApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with securesocial.core.SecureSocial[BasicProfile] {

  import scala.concurrent.ExecutionContext.Implicits.global

  val ACCOUNT = "accounts"

  def findAll = Action.async {
    db
      .collection[JSONCollection](ACCOUNT)
      .find(Json.obj(), Json.obj())
      .cursor[JsObject]
      .collect[List]()
      .map(accounts => Ok(Json.toJson(accounts)))
  }

  def add = Action(parse.json) { implicit request =>
    //val name = (request.body \ "name").as[String]
    db
      .collection[JSONCollection](ACCOUNT)
      .save(request.body)
    Ok
  }
}
