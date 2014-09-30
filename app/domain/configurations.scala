package domain

import org.joda.time.DateTime
import play.api.libs.json.Json

// bénéfice, revenu, frais communs, points business
case class RatioConfiguration(earningsPercentage: Double,
                              revenuePercentage: Double,
                              commonExpensesPercentage: Double,
                              bizPointsPercentage: Double,
                              lastDecided: DateTime,
                              agoraLink: String)

trait RatioConfigurationSerializer {
  implicit val ratioFormatter = Json.format[RatioConfiguration]
}