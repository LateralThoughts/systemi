package controllers.api

import domain._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import securesocial.core.{AuthenticationMethod, RuntimeEnvironment, BasicProfile}
import domain.Account
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.JsObject
import domain.Human
import scala.concurrent.Future
import play.Logger

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

  def add = SecuredAction.async(parse.urlFormEncoded) { implicit request =>

    val form = request.body

    form.get("memberId") match {
        case Some(userId) =>
            db
                .collection[JSONCollection]("users")
                .find(Json.obj("_id" -> Json.obj("$oid" -> userId.head)))
                .one[BasicProfile]
                .flatMap {
                case Some(user) =>
                    val accounts = for {
                            accountName <- form.get("accountName")
                            name = accountName.head
                        } yield Account(name, Human(user))
                    accounts match {
                        case Some(account) => saveAccount(account)
                        case None => Future(BadRequest)
                    }
                case None => Future(BadRequest)
            }
        case None => Future(BadRequest)
    }
  }

  def saveAccount(account: Account) = {
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
