package controllers.api

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain.{Account, Human, _}
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future

class AccountApiController(override implicit val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator]
  with MongoController
  with AccountSerializer
   {

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
                .one[User]
                .flatMap {
                case Some(user) =>
                    val accounts = for {
                            accountName <- form.get("accountName")
                            name = accountName.head
                        } yield Account(name, Human(user))
                    accounts match {
                        case Some(account) => {
                          saveAccount(account)
                          Future(Redirect(controllers.routes.MembersController.index()))
                        }
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
