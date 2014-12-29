package controllers.api

import auth.WithDomain
import domain.IncomeAllocation
import play.api.libs.json.Json
import play.api.mvc.Controller
import repository.Repositories
import securesocial.core.{BasicProfile, RuntimeEnvironment}


class StatsApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with Repositories
  with securesocial.core.SecureSocial[BasicProfile] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getCurrentBalanceForUser = SecuredAction(WithDomain()).async { implicit request =>
    allocationRepository
      .findByUser(request.user.userId)
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
