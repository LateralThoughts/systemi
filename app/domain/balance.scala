package domain

import julienrf.variants.Variants
import org.joda.time.DateTime
import play.api.libs.json.Json

sealed trait Way
case object Plus extends Way
case object Minus extends Way

sealed abstract class AccountOperation(way: Way)

case class Invoice(invoice: InvoiceRequest,
                   pdfDocument: Attachment,
                   statuses: List[Status],
                   lastStatus: Status,
                   paymentStatus: String = "unpaid",
                   affectationStatus: String = "unaffected") extends AccountOperation(Plus) {

  def totalHT = InvoiceLinesAnalyzer.computeTotalHT(invoice.invoice)
}

case class CommonExpenseType(name: String)

case class CommonExpense(name: String,
                         description: String,
                         createdAt: DateTime,
                         expenseType: CommonExpenseType,
                         value: Double) extends AccountOperation(Minus) // TODO add file

sealed trait SalaryType
case object MonthlySalary extends SalaryType
case object VariableBonus extends SalaryType
case object PointBonus extends SalaryType
case object NonChargedSalary extends SalaryType

case class Salary(name: String,
                  description: String,
                  createdAt: DateTime,
                  recipient: Member,
                  value: Double,
                  salaryType: SalaryType) extends AccountOperation(Minus) // TODO add file

case class MemberExpense(description: String,
                         createdAt: DateTime,
                         recipient: Member,
                         value: Double) extends AccountOperation(Minus) // TODO add file


sealed trait Affectation

// une facture/un revenu se fait subdivisé entre le membre LT, la structure etc..
case class IncomeAffectation(incomeOperation: Invoice,
                       account: Account,
                       value: Double) extends Affectation

trait ExpenseSerializer extends MemberSerializer {
  implicit val salaryType = Variants.format[SalaryType]
  implicit val commonExpenseType = Json.format[CommonExpenseType]
  implicit val common = Json.format[CommonExpense]
  implicit val salary = Json.format[Salary]
  implicit val member = Json.format[MemberExpense]
}

trait AffectationSerializer extends AccountSerializer with InvoiceSerializer with ExpenseSerializer {
  implicit val wayFormatter = Variants.format[Way]
  implicit val affectationFormatter = Json.format[IncomeAffectation]
}