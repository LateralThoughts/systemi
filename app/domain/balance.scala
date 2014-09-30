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
                   lastStatus: Status) extends AccountOperation(Plus) {

  def totalHT = InvoiceLinesAnalyzer.computeTotalHT(invoice.invoice)
}

abstract class Expense(value: Double) extends AccountOperation(Minus)

case class CommonExpenseType(name: String)

case class CommonExpense(name: String,
                         description: String,
                         createdAt: DateTime,
                         expenseType: CommonExpenseType,
                         value: Double) extends Expense(value) // TODO add file

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
                  salaryType: SalaryType) extends Expense(value) // TODO add file

case class MemberExpense(description: String,
                         createdAt: DateTime,
                         recipient: Member,
                         value: Double) extends Expense(value) // TODO add file


// une facture/un revenu se fait subdivisé entre le membre LT, la structure etc..
case class Affectation(incomeOperation: AccountOperation,
                       account: Account,
                       value: Double)

trait AffectationSerializer extends AccountSerializer {
  implicit val accountOperationFormatter = Variants.format[AccountOperation]
  implicit val affectationFormatter = Json.format[Affectation]
}