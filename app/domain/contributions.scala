package domain

import org.joda.time.DateTime
import play.api.libs.json.Json

case class Contribution (points: Int,
                    member : Member,
                    title: String,
                    createdAt: DateTime)

trait ContributionSerializer extends MemberSerializer {
  implicit val contributionFormatter = Json.format[Contribution]
}
