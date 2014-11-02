package engine

import domain._
import play.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future

trait AffectationEngine
  extends RatioConfigurationSerializer
  with InvoiceSerializer
  with AccountSerializer
  with AffectationSerializer {self: MongoController =>
  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   * Compute affectations from LT's ratio configuration
   *  - x% for common expenses
   *  - x% for timeoff
   *  - etc.
   */
  def computeAffectationsFromConfiguration(invoice: Invoice, account: Account) = db
    .collection[JSONCollection]("configuration")
    .find(Json.obj())
    .one[RatioConfiguration]
    .map {
    case Some(config) =>
      IncomeAffectation(account,                               invoice.totalHT, Some(invoice._id))
  }

  private def createAffectationByRatioAndReturnRemainder(config: RatioConfiguration, invoice: Invoice, account: Account, status: String) = {
    val (remainderAfterExpenses, commonExpensesAffectedValue) = computeAffectedFromValueFromRatio(invoice.totalHT, config.commonExpensesRatio)
    val (remainderAfterBizPoints, bizPointsAffectedValue) = computeAffectedFromValueFromRatio(remainderAfterExpenses, config.bizPointsRatio)
    val (remainderAfterEarnings, earningsAffectedValue) = computeAffectedFromValueFromRatio(remainderAfterBizPoints, config.earningsRatio)
    if (status == "salary") {
      val (remainderAfterIndividuals, individualExpensesAffectedValue) = computeAffectedFromValueFromRatio(remainderAfterEarnings, config.individualExpensesRatio)
      val (remainderAfterAll, timeoffAffectedValue) = computeAffectedFromValueFromRatio(remainderAfterIndividuals, config.timeoffExpensesRatio)
      val ltAccount = LT("LT")
      List(
        IncomeAffectation(account,                                invoice.totalHT - remainderAfterAll, Some(invoice._id)),
        IncomeAffectation(Account("Budget Commun", ltAccount),    commonExpensesAffectedValue, Some(invoice._id)),
        IncomeAffectation(Account("Timeoff", ltAccount),          timeoffAffectedValue, Some(invoice._id)),
        IncomeAffectation(account.copy(name = "Frais persos"),    individualExpensesAffectedValue, Some(invoice._id)),
        IncomeAffectation(Account("Bénéfice", ltAccount),         earningsAffectedValue, Some(invoice._id)),
        IncomeAffectation(Account("Points business", ltAccount),  bizPointsAffectedValue, Some(invoice._id))
      )
    } else {
      val (remainderAfterAll, timeoffAffectedValue) = computeAffectedFromValueFromRatio(remainderAfterEarnings, config.timeoffExpensesRatio)
      val ltAccount = LT("LT")
      List(
        IncomeAffectation(account,                               invoice.totalHT - remainderAfterAll, Some(invoice._id)),
        IncomeAffectation(Account("Budget Commun", ltAccount),    commonExpensesAffectedValue, Some(invoice._id)),
        IncomeAffectation(Account("Timeoff", ltAccount),          timeoffAffectedValue, Some(invoice._id)),
        IncomeAffectation(Account("Bénéfice", ltAccount),         earningsAffectedValue, Some(invoice._id)),
        IncomeAffectation(Account("Points business", ltAccount),  bizPointsAffectedValue, Some(invoice._id))
      )
    }
  }

  private def computeAffectedFromValueFromRatio(initialValue: Double, ratio: Ratio) = {
    ratio.ratioType match {
      case "fixed" =>
        (initialValue - ratio.value, ratio.value)

      case "percentage" =>
        val computedValue = initialValue * ratio.value / 100.0
        (initialValue - computedValue, computedValue)
    }
  }
}
