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
  def computeAffectationsFromConfiguration(invoice: Invoice, account: Account) = {
    db
      .collection[JSONCollection]("configuration")
      .find(Json.obj())
      .one[RatioConfiguration]
      .flatMap {
      case Some(config) =>
        val futures = createAffectationByRatioAndReturnRemainder(config, invoice, account) . map { affectation =>
          Logger.debug(s"creating affectation of invoice for : $affectation")
          db
            .collection[JSONCollection]("affectations")
            .save(Json.toJson(affectation))
            .map(errors => errors.inError)
        }
        val futureHasAtLeastOneFailure = Future.sequence(futures)
          .map(_.foldLeft(false)((acc, current) => acc || current))

        futureHasAtLeastOneFailure

      case None => Future(true)
    }
  }

  private def createAffectationByRatioAndReturnRemainder(config: RatioConfiguration, invoice: Invoice, account: Account) = {
    val (remainderAfterExpenses, commonExpensesAffectedValue) = computeAffectedFromValueFromRatio(invoice.totalHT, config.commonExpensesRatio)
    val (remainderAfterBizPoints, bizPointsAffectedValue) = computeAffectedFromValueFromRatio(remainderAfterExpenses, config.bizPointsRatio)
    val (remainderAfterEarnings, earningsAffectedValue) = computeAffectedFromValueFromRatio(remainderAfterBizPoints, config.earningsRatio)
    val (remainderAfterIndividuals, individualExpensesAffectedValue) = computeAffectedFromValueFromRatio(remainderAfterEarnings, config.individualExpensesRatio)
    val (remainderAfterAll, timeoffAffectedValue) = computeAffectedFromValueFromRatio(remainderAfterIndividuals, config.timeoffExpensesRatio)
    val ltAccount = LT("LT")
    List(
      IncomeAffectation(invoice, account,                               invoice.totalHT - remainderAfterAll),
      IncomeAffectation(invoice, Account("Budget Commun", ltAccount),    commonExpensesAffectedValue),
      IncomeAffectation(invoice, Account("Timeoff", ltAccount),          timeoffAffectedValue),
      IncomeAffectation(invoice, account.copy(name = "Frais persos"),   individualExpensesAffectedValue),
      IncomeAffectation(invoice, Account("Bénéfice", ltAccount),         earningsAffectedValue),
      IncomeAffectation(invoice, Account("Points business", ltAccount),  bizPointsAffectedValue)
    )
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
