package controllers

import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}

class AccountsController (override implicit val env: RuntimeEnvironment[BasicProfile]) extends Controller
                            with MongoController
                            with securesocial.core.SecureSocial[BasicProfile]{

  def collection = db.collection[JSONCollection]("accounts")

}
