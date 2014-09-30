package controllers.api

import domain.{Human, Account, AccountSerializer, InvoiceSerializer}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}
import util.pdf.GoogleDriveInteraction

class AccountApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with AccountSerializer
  with securesocial.core.SecureSocial[BasicProfile] {

  import scala.concurrent.ExecutionContext.Implicits.global

  val ACCOUNT = "accounts"

  def findAll = SecuredAction.async {
    db
      .collection[JSONCollection](ACCOUNT)
      .find(Json.obj(), Json.obj())
      .cursor[JsObject]
      .collect[List]()
      .map(accounts => Ok(Json.toJson(accounts)))
  }

  def add = SecuredAction.async(parse.json) { implicit request =>
    val name = (request.body \ "name").as[String]
    val account = Account(name, Human(request.user))
    db
      .collection[JSONCollection](ACCOUNT)
      .save(Json.toJson(account))
      .map { lastError =>
        if (lastError.inError) {
          InternalServerError
        } else {
          Ok
        }
      }
  }
}
