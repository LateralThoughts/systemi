package domain

import org.joda.time.DateTime
import play.api.libs.json.Json

// fixed or percentage
case class Ratio(ratioType: String, value: Double)

// bénéfice, revenu, frais communs, points business
case class RatioConfiguration(earningsRatio: Ratio,
                              commonExpensesRatio : Ratio,
                              bizPointsRatio: Ratio,
                              individualExpensesRatio: Ratio,
                              timeoffExpensesRatio: Ratio,
                              lastDecided: DateTime,
                              agoraLink: String)

trait RatioConfigurationSerializer {
  implicit val ratioFormatter = Json.format[Ratio]
  implicit val ratioConfigFormatter = Json.format[RatioConfiguration]
}