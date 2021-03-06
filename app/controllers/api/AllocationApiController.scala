package controllers.api

import auth.WithDomain
import domain._
import play.api.libs.json._
import play.api.mvc.Controller
import repository.Repositories
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.ExecutionContext.Implicits.global

class AllocationApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with Repositories
  with AccountSerializer
  with AllocationSerializer
  with securesocial.core.SecureSocial[BasicProfile] {

  def findByInvoice(invoiceId : String) = SecuredAction(WithDomain()).async { implicit request =>
    allocationRepository
      .findByInvoice(invoiceId)
      .map(allocations => Ok(Json.toJson(allocations)))
  }

}
