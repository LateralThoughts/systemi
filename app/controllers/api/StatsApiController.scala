package controllers.api

import auth.WithDomain
import domain.{AllocationSerializer, IncomeAllocation}
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}


class StatsApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with AllocationSerializer
  with securesocial.core.SecureSocial[BasicProfile] {
  import scala.concurrent.ExecutionContext.Implicits.global

  def getCurrentBalanceForUser = SecuredAction(WithDomain()).async { implicit request =>
    db.collection[JSONCollection]("affectations")
      .find(Json.obj("account.stakeholder.user.userId" -> request.user.userId))
      .cursor[IncomeAllocation]
      .collect[List]()
      .map { allocations =>

      Ok(
        Json.toJson(
          allocations.groupBy(_.account).map { case (account, allocations) =>
            Json.obj(
              "account" -> account.name,
              "total" -> allocations.foldLeft(0.0)((sum: Double, item: IncomeAllocation) => sum + item.value)
            )
          }
        )
      )
    }
  }
}
