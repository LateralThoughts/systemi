package controllers

import auth.WithDomain
import domain.{LT, Human, Account}
import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DefaultDB
import securesocial.core.{BasicProfile, RuntimeEnvironment}


class WelcomeController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with securesocial.core.SecureSocial[BasicProfile] {

  def index = SecuredAction(WithDomain()) { implicit request =>
    Ok(views.html.welcome.index(request.user))
  }

  private def createDedicatedAccounts(profile: BasicProfile, db: DefaultDB) = {
    /*val account = Account("Revenue", Human(profile), affectable = true)
    val ltAccount = LT("LT")

    List(account,
      Account("Budget Commun", ltAccount),
      Account("Timeoff", ltAccount),
      account.copy(name = "Frais persos"),
      Account("Bénéfice", ltAccount),
      Account("Points business", ltAccount)).map { currentAccount =>
      db
        .collection[JSONCollection]("accounts")
        .update(Json.obj("name" -> currentAccount.name, "stakeholder."))
    }*/
  }

}
