package controllers.api

import auth.WithDomain
import domain.{Contribution, ContributionSerializer, Client}
import play.Logger
import play.api.libs.json.{JsResult, JsError, JsObject, Json}
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.{BasicProfile, RuntimeEnvironment}

import scala.concurrent.Future

class ContributionsApiController(override implicit val env: RuntimeEnvironment[BasicProfile])
  extends Controller
  with MongoController
  with ContributionSerializer
  with securesocial.core.SecureSocial[BasicProfile] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def findByType(mayContributionType: Option[String]) = SecuredAction(WithDomain()).async {
    db
      .collection[JSONCollection]("contributions")
      .find(mayContributionType.map(contributionType => Json.obj("type" -> contributionType)).getOrElse(Json.obj()))
      .cursor[JsObject]
      .collect[List]()
      .map(contributions => Ok(Json.toJson(contributions)))
  }

  def createContribution() = SecuredAction(WithDomain()).async(parse.json) { implicit request =>
    request.body.validate(contributionFormatter) match {
      case errors:JsError =>
        Future(BadRequest(errors.toString).as("application/json"))

      case result: JsResult[Contribution] =>
        saveContribution(result.get) map {
          case true => Ok
          case false => InternalServerError
        }
    }
  }

  private def saveContribution(contribution: Contribution) = {
    val selector = Json.obj()
    db
      .collection[JSONCollection]("contributions")
      .save(contribution)
      .map(errors => if (errors.inError) {

      Logger.error(s"Failed to create contribution $contribution")
      false
    } else {
      Logger.info("Successfully inserted contribution")
      true
    })
  }
}
