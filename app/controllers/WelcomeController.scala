package controllers

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain.User
import reactivemongo.api.DefaultDB


class WelcomeController(override implicit val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator]
   {

  def index = SecuredAction { implicit request =>
    Ok(views.html.welcome.index(request.identity))
  }

  private def createDedicatedAccounts(profile: User, db: DefaultDB) = {
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
