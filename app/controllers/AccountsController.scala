package controllers

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.ExecutionContext

class AccountsController (override implicit val env: RuntimeEnvironment[BasicProfile]) extends Controller
                            with MongoController
                            with securesocial.core.SecureSocial[BasicProfile]{

  import play.modules.reactivemongo.json.BSONFormats._
  import ExecutionContext.Implicits.global

  def collection = db.collection[JSONCollection]("accounts")

}
