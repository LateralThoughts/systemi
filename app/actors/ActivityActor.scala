package actors

import akka.actor.Actor
import domain.{Activity, ActivitySerializer}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.Json
import play.Logger

case class ActivityActor() extends Actor with ActivitySerializer {

  import play.api.Play.current

  import scala.concurrent.ExecutionContext.Implicits.global

  def receive = {

    case activity: Activity =>
      val db = ReactiveMongoPlugin.db
      db.
        collection[JSONCollection]("activities")
        .save(Json.toJson(activity))
      Logger.info(s"Saved activity $activity")
  }


}
